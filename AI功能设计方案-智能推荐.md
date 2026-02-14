# AI智能职位推荐 - 详细设计

## 功能概述

基于学生简历和职位描述的语义相似度，使用向量检索技术推荐最匹配的职位。

---

## 用户场景

### 学生端首页

```
学生登录后，首页显示：
┌─────────────────────────────────────┐
│  🤖 AI为你推荐                       │
│  ─────────────────────────────────  │
│  📌 Java后端开发工程师               │
│  💰 12-20K  📍 成都                  │
│  🎯 匹配度: 95%                      │
│  ✨ 推荐理由: 你的Java技能和项目经验  │
│     与该职位高度匹配                 │
│  ─────────────────────────────────  │
│  📌 前端开发工程师                   │
│  💰 10-15K  📍 成都                  │
│  🎯 匹配度: 88%                      │
│  ─────────────────────────────────  │
│  查看更多推荐 →                      │
└─────────────────────────────────────┘
```

---

## 技术实现

### 1. 后端服务

```java
@Service
public class AIJobRecommendationService {
    
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
        if (resume == null || resume.getEmbedding() == null) {
            // 如果没有向量，先生成
            generateResumeEmbedding(studentId);
            resume = resumeRepository.findByStudentId(studentId);
        }
        
        // 2. 向量相似度搜索
        List<Job> similarJobs = jobRepository.findSimilarJobs(
            resume.getEmbedding(), 
            limit
        );
        
        // 3. 构建推荐结果
        return similarJobs.stream()
            .map(job -> buildRecommendation(job, resume))
            .collect(Collectors.toList());
    }
    
    /**
     * 生成简历向量
     */
    public void generateResumeEmbedding(String studentId) {
        StudentResume resume = resumeRepository.findByStudentId(studentId);
        
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
    }
    
    /**
     * 构建简历文本（用于生成向量）
     */
    private String buildResumeText(StudentResume resume) {
        return String.format("""
            专业：%s
            学历：%s
            技能：%s
            项目经验：%s
            实习经历：%s
            求职意向：%s
            期望薪资：%s
            期望城市：%s
            """,
            resume.getMajor(),
            resume.getEducation(),
            resume.getSkills(),
            resume.getProjectExperience(),
            resume.getInternshipExperience(),
            resume.getJobIntent(),
            resume.getExpectedSalary(),
            resume.getExpectedCity()
        );
    }
    
    /**
     * 生成职位向量
     */
    public void generateJobEmbedding(String jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        
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
    }
    
    /**
     * 构建职位文本（用于生成向量）
     */
    private String buildJobText(Job job) {
        return String.format("""
            职位名称：%s
            职位描述：%s
            任职要求：%s
            技能要求：%s
            学历要求：%s
            工作经验：%s
            薪资范围：%s
            工作地点：%s
            """,
            job.getTitle(),
            job.getDescription(),
            job.getRequirements(),
            job.getSkills(),
            job.getEducation(),
            job.getExperience(),
            job.getSalaryRange(),
            job.getLocation()
        );
    }
    
    /**
     * 构建推荐结果
     */
    private JobRecommendationVO buildRecommendation(Job job, StudentResume resume) {
        // 计算匹配度（余弦相似度转换为百分比）
        float similarity = calculateSimilarity(
            job.getEmbedding(), 
            resume.getEmbedding()
        );
        int matchScore = (int) (similarity * 100);
        
        // 生成推荐理由
        String reason = generateRecommendationReason(job, resume, matchScore);
        
        return JobRecommendationVO.builder()
            .jobId(job.getJobId())
            .title(job.getTitle())
            .companyName(job.getCompanyName())
            .salaryRange(job.getSalaryRange())
            .location(job.getLocation())
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
    private String generateRecommendationReason(Job job, StudentResume resume, int matchScore) {
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

### 2. Repository层

```java
@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    
    /**
     * 向量相似度搜索
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
}
```

### 3. Controller层

```java
@RestController
@RequestMapping("/api/mobile/ai")
public class AIRecommendationController {
    
    @Autowired
    private AIJobRecommendationService recommendationService;
    
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

---

## 前端实现

### 学生端首页添加AI推荐模块

```vue
<template>
  <div class="ai-recommendation-section mt-3 px-3">
    <div class="flex justify-between items-center mb-3">
      <h3 class="text-base font-bold text-gray-800 flex items-center">
        <van-icon name="fire" color="#ee0a24" class="mr-1" />
        AI为你推荐
      </h3>
      <span class="text-xs text-gray-400" @click="toRecommendList">
        更多 <van-icon name="arrow" />
      </span>
    </div>

    <van-loading v-if="loading" class="text-center py-4" />

    <div v-else class="space-y-2">
      <div
        v-for="job in recommendedJobs"
        :key="job.jobId"
        class="bg-white p-4 rounded-lg shadow-sm active:bg-gray-50"
        @click="toJobDetail(job.jobId)"
      >
        <!-- 匹配度标签 -->
        <div class="flex items-center justify-between mb-2">
          <van-tag 
            :type="getMatchType(job.matchScore)" 
            size="medium"
          >
            🎯 匹配度 {{ job.matchScore }}%
          </van-tag>
          <van-tag plain type="primary" size="small">AI推荐</van-tag>
        </div>

        <!-- 职位信息 -->
        <div class="font-bold text-base text-gray-800 mb-2">
          {{ job.title }}
        </div>

        <div class="flex items-center text-sm text-gray-600 mb-2">
          <span class="text-blue-600 font-medium mr-3">{{ job.salaryRange }}</span>
          <span class="mr-3">{{ job.location }}</span>
          <span>{{ job.companyName }}</span>
        </div>

        <!-- 推荐理由 -->
        <div class="flex items-start gap-1 text-xs text-gray-500 bg-blue-50 p-2 rounded">
          <van-icon name="info-o" size="14" color="#3b82f6" class="mt-0.5" />
          <span>{{ job.recommendReason }}</span>
        </div>
      </div>
    </div>

    <van-empty 
      v-if="!loading && recommendedJobs.length === 0" 
      description="暂无推荐职位，完善简历后获得更多推荐"
      image-size="80"
    >
      <van-button type="primary" size="small" @click="toResume">
        完善简历
      </van-button>
    </van-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAIRecommendJobs } from '@/api/mobile/ai'

const router = useRouter()
const loading = ref(true)
const recommendedJobs = ref<any[]>([])

// 加载AI推荐
const loadRecommendations = async () => {
  try {
    const res = await getAIRecommendJobs(5)
    recommendedJobs.value = res.data || []
  } catch (error) {
    console.error('加载AI推荐失败', error)
  } finally {
    loading.value = false
  }
}

// 获取匹配度标签类型
const getMatchType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'default'
}

// 跳转职位详情
const toJobDetail = (jobId: string) => {
  router.push(`/student/job/${jobId}`)
}

// 跳转推荐列表
const toRecommendList = () => {
  router.push('/student/ai/recommend')
}

// 跳转简历
const toResume = () => {
  router.push('/student/resume/edit')
}

onMounted(() => {
  loadRecommendations()
})
</script>
```

### API接口

```typescript
// src/api/mobile/ai.ts
import request from '@/utils/request'

export interface JobRecommendationVO {
  jobId: string
  title: string
  companyName: string
  salaryRange: string
  location: string
  matchScore: number
  recommendReason: string
}

// 获取AI推荐职位
export function getAIRecommendJobs(limit: number = 10) {
  return request.get<any, { data: JobRecommendationVO[] }>(
    '/mobile/ai/recommend/jobs',
    { params: { limit } }
  )
}

// 刷新简历向量
export function refreshResumeEmbedding() {
  return request.post('/mobile/ai/refresh/resume')
}
```

---

## 数据库迁移

```sql
-- 1. 添加向量字段
ALTER TABLE biz_job ADD COLUMN IF NOT EXISTS embedding vector(1536);
ALTER TABLE biz_resume ADD COLUMN IF NOT EXISTS embedding vector(1536);

-- 2. 创建向量索引
CREATE INDEX IF NOT EXISTS idx_job_embedding 
ON biz_job USING ivfflat (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_resume_embedding 
ON biz_resume USING ivfflat (embedding vector_cosine_ops);

-- 3. 添加更新时间触发器（向量需要重新生成）
CREATE OR REPLACE FUNCTION update_embedding_flag()
RETURNS TRIGGER AS $$
BEGIN
  NEW.embedding = NULL;  -- 清空向量，触发重新生成
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER resume_update_trigger
BEFORE UPDATE ON biz_resume
FOR EACH ROW
WHEN (OLD.skills IS DISTINCT FROM NEW.skills 
   OR OLD.experience IS DISTINCT FROM NEW.experience)
EXECUTE FUNCTION update_embedding_flag();
```

---

## 定时任务

```java
@Component
public class EmbeddingGenerationTask {
    
    @Autowired
    private AIJobRecommendationService recommendationService;
    
    @Autowired
    private JobRepository jobRepository;
    
    /**
     * 每天凌晨2点生成新职位的向量
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateJobEmbeddings() {
        List<Job> jobsWithoutEmbedding = jobRepository
            .findByEmbeddingIsNull();
        
        for (Job job : jobsWithoutEmbedding) {
            try {
                recommendationService.generateJobEmbedding(job.getJobId());
            } catch (Exception e) {
                log.error("生成职位向量失败: {}", job.getJobId(), e);
            }
        }
    }
}
```

---

## 性能优化

1. **向量缓存**: 使用Redis缓存常用向量
2. **批量生成**: 批量调用Embedding API
3. **异步处理**: 简历更新后异步生成向量
4. **索引优化**: 使用ivfflat索引加速检索

---

## 测试要点

1. 简历向量生成是否正确
2. 职位向量生成是否正确
3. 相似度计算是否准确
4. 推荐结果是否合理
5. 性能是否满足要求（<1秒）

---

## 后续优化

1. 加入协同过滤（用户行为）
2. 加入热度权重（浏览、投递）
3. 加入时间衰减（新职位优先）
4. 个性化调整（用户反馈）
