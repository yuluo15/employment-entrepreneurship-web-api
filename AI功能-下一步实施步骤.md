# AI功能 - 下一步实施步骤

## 当前状态

✅ Spring AI 已配置  
✅ pgvector 扩展已安装  
⏳ 数据库迁移（下一步）  
⏳ 后端服务实现  
⏳ 前端页面实现

---

## 第一步：执行数据库迁移 ⭐

### 1.1 执行迁移脚本

```bash
# 连接到PostgreSQL数据库
psql -U your_username -d your_database

# 执行迁移脚本
\i docs/db/ai_vector_migration.sql
```

### 1.2 验证迁移结果

```sql
-- 1. 确认pgvector扩展已启用
SELECT * FROM pg_extension WHERE extname = 'vector';

-- 2. 确认向量字段已添加
\d biz_job
\d biz_student_resume

-- 3. 确认索引已创建
\di idx_biz_job_embedding
\di idx_biz_student_resume_embedding

-- 4. 查看数据统计
SELECT 
    '职位' as 类型,
    COUNT(*) as 总数,
    COUNT(embedding) as 已生成向量数
FROM biz_job;
```

### 1.3 需要添加的向量字段总结

| 表名 | 字段名 | 类型 | 用途 |
|------|--------|------|------|
| biz_job | embedding | vector(1536) | 职位描述向量，用于职位推荐 |
| biz_student_resume | embedding | vector(1536) | 简历向量，用于简历匹配 |
| biz_project | embedding | vector(1536) | 项目描述向量（可选） |

---

## 第二步：实现后端服务 ⭐⭐⭐

### 2.1 创建实体类映射

```java
// 1. Job实体添加向量字段
@Entity
@Table(name = "biz_job")
public class Job {
    
    @Id
    @Column(name = "job_id")
    private String jobId;
    
    // ... 其他字段
    
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;
    
    // getter/setter
}

// 2. StudentResume实体添加向量字段
@Entity
@Table(name = "biz_student_resume")
public class StudentResume {
    
    @Id
    @Column(name = "resume_id")
    private String resumeId;
    
    // ... 其他字段
    
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;
    
    // getter/setter
}
```

### 2.2 创建Repository

```java
@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    
    /**
     * 向量相似度搜索 - 推荐职位
     */
    @Query(value = """
        SELECT 
            j.*,
            1 - (j.embedding <=> CAST(:resumeVector AS vector)) as similarity
        FROM biz_job j
        WHERE j.status = 1 
          AND j.audit = 1
          AND j.embedding IS NOT NULL
        ORDER BY j.embedding <=> CAST(:resumeVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Job> findSimilarJobs(
        @Param("resumeVector") float[] resumeVector,
        @Param("limit") int limit
    );
    
    /**
     * 查找没有向量的职位
     */
    @Query("SELECT j FROM Job j WHERE j.embedding IS NULL AND j.status = 1 AND j.audit = 1")
    List<Job> findJobsWithoutEmbedding();
}

@Repository
public interface StudentResumeRepository extends JpaRepository<StudentResume, String> {
    
    /**
     * 根据学生ID查找简历
     */
    StudentResume findByStudentId(String studentId);
    
    /**
     * 向量相似度搜索 - 筛选简历
     */
    @Query(value = """
        SELECT 
            r.*,
            1 - (r.embedding <=> CAST(:jobVector AS vector)) as similarity
        FROM biz_student_resume r
        WHERE r.is_public = 1
          AND r.embedding IS NOT NULL
        ORDER BY r.embedding <=> CAST(:jobVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<StudentResume> findSimilarResumes(
        @Param("jobVector") float[] jobVector,
        @Param("limit") int limit
    );
    
    /**
     * 查找没有向量的简历
     */
    @Query("SELECT r FROM StudentResume r WHERE r.embedding IS NULL AND r.isPublic = 1")
    List<StudentResume> findResumesWithoutEmbedding();
}
```

### 2.3 创建AI推荐服务

创建文件：`src/main/java/com/example/service/AIRecommendationService.java`

```java
@Service
@Slf4j
public class AIRecommendationService {
    
    @Autowired
    private EmbeddingClient embeddingClient;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private StudentResumeRepository resumeRepository;
    
    /**
     * 为学生推荐职位
     */
    public List<JobRecommendationVO> recommendJobs(String studentId, int limit) {
        // 1. 获取学生简历
        StudentResume resume = resumeRepository.findByStudentId(studentId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        
        // 2. 如果没有向量，先生成
        if (resume.getEmbedding() == null) {
            generateResumeEmbedding(studentId);
            resume = resumeRepository.findByStudentId(studentId);
        }
        
        // 3. 向量相似度搜索
        List<Job> similarJobs = jobRepository.findSimilarJobs(
            resume.getEmbedding(), 
            limit
        );
        
        // 4. 构建推荐结果
        return similarJobs.stream()
            .map(job -> buildRecommendation(job, resume))
            .collect(Collectors.toList());
    }
    
    /**
     * 生成简历向量
     */
    public void generateResumeEmbedding(String studentId) {
        StudentResume resume = resumeRepository.findByStudentId(studentId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        
        // 构建简历文本
        String resumeText = buildResumeText(resume);
        
        // 生成向量
        EmbeddingResponse response = embeddingClient.embedForResponse(
            List.of(resumeText)
        );
        
        float[] embedding = response.getResult().getOutput();
        
        // 保存向量
        resume.setEmbedding(embedding);
        resumeRepository.save(resume);
        
        log.info("生成简历向量成功: studentId={}", studentId);
    }
    
    /**
     * 生成职位向量
     */
    public void generateJobEmbedding(String jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException("职位不存在"));
        
        // 构建职位文本
        String jobText = buildJobText(job);
        
        // 生成向量
        EmbeddingResponse response = embeddingClient.embedForResponse(
            List.of(jobText)
        );
        
        float[] embedding = response.getResult().getOutput();
        
        // 保存向量
        job.setEmbedding(embedding);
        jobRepository.save(job);
        
        log.info("生成职位向量成功: jobId={}", jobId);
    }
    
    /**
     * 构建简历文本
     */
    private String buildResumeText(StudentResume resume) {
        StringBuilder sb = new StringBuilder();
        
        // 基本信息
        if (resume.getExpectedPosition() != null) {
            sb.append("求职意向：").append(resume.getExpectedPosition()).append("\n");
        }
        if (resume.getExpectedSalary() != null) {
            sb.append("期望薪资：").append(resume.getExpectedSalary()).append("\n");
        }
        if (resume.getTargetCity() != null) {
            sb.append("期望城市：").append(resume.getTargetCity()).append("\n");
        }
        
        // 个人总结
        if (resume.getPersonalSummary() != null) {
            sb.append("个人简介：").append(resume.getPersonalSummary()).append("\n");
        }
        
        // 技能
        if (resume.getSkills() != null) {
            sb.append("技能：").append(resume.getSkills()).append("\n");
        }
        
        // 教育经历（从JSON解析）
        if (resume.getEducationHistory() != null) {
            sb.append("教育经历：").append(resume.getEducationHistory()).append("\n");
        }
        
        // 实习经历
        if (resume.getInternshipExp() != null) {
            sb.append("实习经历：").append(resume.getInternshipExp()).append("\n");
        }
        
        // 项目经验
        if (resume.getProjectExp() != null) {
            sb.append("项目经验：").append(resume.getProjectExp()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 构建职位文本
     */
    private String buildJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("职位名称：").append(job.getJobName()).append("\n");
        sb.append("薪资范围：").append(job.getSalaryRange()).append("\n");
        sb.append("工作城市：").append(job.getCity()).append("\n");
        
        if (job.getEducation() != null) {
            sb.append("学历要求：").append(job.getEducation()).append("\n");
        }
        if (job.getExperience() != null) {
            sb.append("经验要求：").append(job.getExperience()).append("\n");
        }
        if (job.getTags() != null) {
            sb.append("技能标签：").append(job.getTags()).append("\n");
        }
        if (job.getDescription() != null) {
            sb.append("职位描述：").append(job.getDescription()).append("\n");
        }
        if (job.getRequirement() != null) {
            sb.append("任职要求：").append(job.getRequirement()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 构建推荐结果
     */
    private JobRecommendationVO buildRecommendation(Job job, StudentResume resume) {
        // 计算匹配度
        float similarity = calculateSimilarity(job.getEmbedding(), resume.getEmbedding());
        int matchScore = (int) (similarity * 100);
        
        // 生成推荐理由
        String reason = generateRecommendationReason(matchScore);
        
        return JobRecommendationVO.builder()
            .jobId(job.getJobId())
            .title(job.getJobName())
            .companyName(getCompanyName(job.getCompanyId()))
            .salaryRange(job.getSalaryRange())
            .location(job.getCity())
            .matchScore(matchScore)
            .recommendReason(reason)
            .build();
    }
    
    /**
     * 计算余弦相似度
     */
    private float calculateSimilarity(float[] vec1, float[] vec2) {
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        return dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(int matchScore) {
        if (matchScore >= 90) {
            return "你的技能和经验与该职位高度匹配";
        } else if (matchScore >= 80) {
            return "该职位与你的专业背景相符";
        } else if (matchScore >= 70) {
            return "该职位适合你的职业发展方向";
        } else {
            return "该职位可能适合你";
        }
    }
}
```

### 2.4 创建Controller

```java
@RestController
@RequestMapping("/api/mobile/ai")
public class AIRecommendationController {
    
    @Autowired
    private AIRecommendationService recommendationService;
    
    /**
     * 获取AI推荐职位
     */
    @GetMapping("/recommend/jobs")
    public Result<List<JobRecommendationVO>> recommendJobs(
        @RequestParam(defaultValue = "10") int limit
    ) {
        String studentId = SecurityUtils.getStudentId();
        List<JobRecommendationVO> recommendations = 
            recommendationService.recommendJobs(studentId, limit);
        return Result.success(recommendations);
    }
    
    /**
     * 刷新简历向量（简历更新后调用）
     */
    @PostMapping("/refresh/resume")
    public Result<Void> refreshResumeEmbedding() {
        String studentId = SecurityUtils.getStudentId();
        recommendationService.generateResumeEmbedding(studentId);
        return Result.success();
    }
}
```

### 2.5 创建VO类

```java
@Data
@Builder
public class JobRecommendationVO {
    private String jobId;
    private String title;
    private String companyName;
    private String salaryRange;
    private String location;
    private Integer matchScore;  // 匹配度（0-100）
    private String recommendReason;  // 推荐理由
}
```

---

## 第三步：批量生成向量（初始化数据）

### 3.1 创建定时任务

```java
@Component
@Slf4j
public class EmbeddingGenerationTask {
    
    @Autowired
    private AIRecommendationService recommendationService;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private StudentResumeRepository resumeRepository;
    
    /**
     * 批量生成职位向量
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    public void generateJobEmbeddings() {
        List<Job> jobs = jobRepository.findJobsWithoutEmbedding();
        log.info("开始生成职位向量，数量：{}", jobs.size());
        
        for (Job job : jobs) {
            try {
                recommendationService.generateJobEmbedding(job.getJobId());
            } catch (Exception e) {
                log.error("生成职位向量失败: jobId={}", job.getJobId(), e);
            }
        }
        
        log.info("职位向量生成完成");
    }
    
    /**
     * 批量生成简历向量
     */
    @Scheduled(cron = "0 30 2 * * ?")  // 每天凌晨2:30执行
    public void generateResumeEmbeddings() {
        List<StudentResume> resumes = resumeRepository.findResumesWithoutEmbedding();
        log.info("开始生成简历向量，数量：{}", resumes.size());
        
        for (StudentResume resume : resumes) {
            try {
                recommendationService.generateResumeEmbedding(resume.getStudentId());
            } catch (Exception e) {
                log.error("生成简历向量失败: studentId={}", resume.getStudentId(), e);
            }
        }
        
        log.info("简历向量生成完成");
    }
}
```

### 3.2 手动触发生成（用于测试）

```java
@RestController
@RequestMapping("/api/admin/ai")
public class AIAdminController {
    
    @Autowired
    private EmbeddingGenerationTask embeddingTask;
    
    /**
     * 手动触发生成所有职位向量
     */
    @PostMapping("/generate/jobs")
    public Result<Void> generateJobEmbeddings() {
        embeddingTask.generateJobEmbeddings();
        return Result.success();
    }
    
    /**
     * 手动触发生成所有简历向量
     */
    @PostMapping("/generate/resumes")
    public Result<Void> generateResumeEmbeddings() {
        embeddingTask.generateResumeEmbeddings();
        return Result.success();
    }
}
```

---

## 第四步：前端实现

参考文档：`docs/AI功能设计方案-智能推荐.md`

---

## 第五步：测试验证

### 5.1 单元测试

```java
@SpringBootTest
class AIRecommendationServiceTest {
    
    @Autowired
    private AIRecommendationService recommendationService;
    
    @Test
    void testGenerateJobEmbedding() {
        recommendationService.generateJobEmbedding("test-job-id");
        // 验证向量已生成
    }
    
    @Test
    void testRecommendJobs() {
        List<JobRecommendationVO> recommendations = 
            recommendationService.recommendJobs("test-student-id", 10);
        
        assertNotNull(recommendations);
        assertTrue(recommendations.size() <= 10);
        
        // 验证匹配度降序排列
        for (int i = 0; i < recommendations.size() - 1; i++) {
            assertTrue(recommendations.get(i).getMatchScore() >= 
                      recommendations.get(i + 1).getMatchScore());
        }
    }
}
```

---

## 检查清单

- [ ] 执行数据库迁移脚本
- [ ] 验证向量字段已添加
- [ ] 验证向量索引已创建
- [ ] 实现Job实体向量字段映射
- [ ] 实现StudentResume实体向量字段映射
- [ ] 实现JobRepository向量查询
- [ ] 实现StudentResumeRepository向量查询
- [ ] 实现AIRecommendationService
- [ ] 实现AIRecommendationController
- [ ] 创建JobRecommendationVO
- [ ] 实现定时任务（批量生成向量）
- [ ] 手动触发生成测试数据向量
- [ ] 测试向量相似度查询
- [ ] 实现前端推荐模块
- [ ] 端到端测试

---

## 预计时间

- 数据库迁移：0.5小时
- 后端实现：1天
- 向量生成：0.5小时
- 前端实现：0.5天
- 测试优化：0.5天

**总计**：2-3天

---

## 下一步行动

1. **立即执行**：运行 `docs/db/ai_vector_migration.sql`
2. **验证结果**：确认向量字段和索引已创建
3. **开始编码**：按照上面的代码示例实现后端服务
4. **生成测试数据**：手动触发向量生成
5. **测试查询**：验证推荐功能是否正常

---

## 需要帮助？

如果遇到问题，可以：
1. 查看 `docs/AI功能设计方案-总体架构.md`
2. 查看 `docs/AI功能设计方案-智能推荐.md`
3. 查看 `docs/AI功能-快速实施指南.md`
