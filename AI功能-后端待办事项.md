# AI功能 - 后端待办事项清单

## 📋 当前状态

✅ 数据库迁移完成  
✅ 接口定义完成  
⏳ 数据初始化待完成  
⏳ 测试验证待完成

---

## 🔧 必须完成的事项

### 1. 生成测试数据向量 ⭐⭐⭐

**重要性**: 极高 - 没有向量数据,推荐功能无法工作

#### 1.1 手动触发生成(推荐)

使用Postman或其他API工具调用管理员接口:

```bash
# 1. 生成所有职位向量
POST http://localhost:8080/api/admin/ai/generate/jobs
Headers: Authorization: Bearer {admin_token}

# 2. 生成所有简历向量
POST http://localhost:8080/api/admin/ai/generate/resumes
Headers: Authorization: Bearer {admin_token}
```

**预计时间**: 
- 100个职位: 约5-10分钟
- 100份简历: 约5-10分钟

#### 1.2 验证生成结果

```sql
-- 查看向量生成进度
SELECT 
    '职位' as 类型,
    COUNT(*) as 总数,
    COUNT(embedding) as 已生成向量数,
    ROUND(COUNT(embedding)::numeric / COUNT(*) * 100, 2) as 完成率
FROM biz_job
WHERE status = 1 AND audit = 1

UNION ALL

SELECT 
    '简历' as 类型,
    COUNT(*) as 总数,
    COUNT(embedding) as 已生成向量数,
    ROUND(COUNT(embedding)::numeric / COUNT(*) * 100, 2) as 完成率
FROM biz_student_resume
WHERE is_public = 1;
```

**预期结果**: 完成率应该接近100%

---

### 2. 测试推荐接口 ⭐⭐⭐

**重要性**: 极高 - 验证功能是否正常工作

#### 2.1 测试学生端推荐接口

```bash
# 获取AI推荐职位
GET http://localhost:8080/api/mobile/ai/recommend/jobs?limit=10
Headers: Authorization: Bearer {student_token}
```

**预期返回**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "jobId": "xxx",
      "title": "Java后端开发工程师",
      "companyName": "某科技公司",
      "salaryRange": "12-20K",
      "location": "成都",
      "matchScore": 95,
      "recommendReason": "你的技能和经验与该职位高度匹配"
    }
  ]
}
```

#### 2.2 测试刷新简历向量接口

```bash
# 刷新简历向量
POST http://localhost:8080/api/mobile/ai/refresh/resume
Headers: Authorization: Bearer {student_token}
```

**预期返回**:
```json
{
  "code": 200,
  "message": "success"
}
```

---

### 3. 验证匹配度计算 ⭐⭐

**重要性**: 高 - 确保推荐质量

#### 3.1 创建测试场景

**场景1**: Java后端开发学生
- 简历: Java、Spring Boot、MySQL、Redis
- 预期: 推荐Java后端职位,匹配度>80%

**场景2**: 前端开发学生
- 简历: Vue、React、TypeScript、CSS
- 预期: 推荐前端职位,匹配度>80%

#### 3.2 验证推荐结果

检查推荐的职位是否与学生简历相关:
- 匹配度分数是否合理(70-100分)
- 推荐理由是否准确
- 推荐职位是否相关

---

## 🔍 可选优化事项

### 4. 性能优化 ⭐

**重要性**: 中 - 提升用户体验

#### 4.1 添加Redis缓存

```java
@Cacheable(value = "ai-recommendations", key = "#studentId")
public List<JobRecommendationVO> recommendJobs(String studentId, int limit) {
    // 现有逻辑
}
```

#### 4.2 批量生成向量

```java
public void batchGenerateEmbeddings(List<Job> jobs) {
    // 批量调用Embedding API
    List<String> texts = jobs.stream()
        .map(this::buildJobText)
        .collect(Collectors.toList());
    
    EmbeddingResponse response = embeddingClient.embedForResponse(texts);
    
    // 批量更新数据库
}
```

---

### 5. 异常处理优化 ⭐

**重要性**: 中 - 提升系统稳定性

#### 5.1 向量生成失败处理

```java
public void generateJobEmbedding(String jobId) {
    try {
        // 现有逻辑
    } catch (Exception e) {
        log.error("生成职位向量失败: jobId={}", jobId, e);
        // 记录到失败队列,稍后重试
        saveToRetryQueue(jobId, "JOB");
    }
}
```

#### 5.2 推荐接口降级

```java
public List<JobRecommendationVO> recommendJobs(String studentId, int limit) {
    try {
        // AI推荐逻辑
    } catch (Exception e) {
        log.error("AI推荐失败,降级为普通推荐", e);
        // 降级为基于标签的推荐
        return fallbackRecommend(studentId, limit);
    }
}
```

---

### 6. 监控和日志 ⭐

**重要性**: 中 - 便于问题排查

#### 6.1 添加关键日志

```java
@Slf4j
public class AIRecommendationService {
    
    public List<JobRecommendationVO> recommendJobs(String studentId, int limit) {
        log.info("开始AI推荐: studentId={}, limit={}", studentId, limit);
        
        long startTime = System.currentTimeMillis();
        
        // 推荐逻辑
        
        long endTime = System.currentTimeMillis();
        log.info("AI推荐完成: studentId={}, 耗时={}ms, 结果数={}", 
            studentId, endTime - startTime, recommendations.size());
        
        return recommendations;
    }
}
```

#### 6.2 添加性能监控

```java
@Aspect
@Component
public class AIPerformanceMonitor {
    
    @Around("execution(* com.example.service.AIRecommendationService.*(..))")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long end = System.currentTimeMillis();
        
        log.info("方法: {}, 耗时: {}ms", pjp.getSignature().getName(), end - start);
        
        return result;
    }
}
```

---

### 7. 推荐效果分析 ⭐

**重要性**: 低 - 用于后续优化

#### 7.1 记录推荐结果

```java
public void recordRecommendation(String studentId, List<JobRecommendationVO> recommendations) {
    for (JobRecommendationVO rec : recommendations) {
        AIRecommendation record = new AIRecommendation();
        record.setStudentId(studentId);
        record.setJobId(rec.getJobId());
        record.setMatchScore(rec.getMatchScore());
        record.setRecommendReason(rec.getRecommendReason());
        
        recommendationRepository.save(record);
    }
}
```

#### 7.2 分析推荐效果

```sql
-- 推荐点击率
SELECT 
    COUNT(*) as 推荐总数,
    SUM(is_clicked) as 点击数,
    ROUND(SUM(is_clicked)::numeric / COUNT(*) * 100, 2) as 点击率
FROM biz_ai_recommendation;

-- 推荐投递率
SELECT 
    COUNT(*) as 推荐总数,
    SUM(is_delivered) as 投递数,
    ROUND(SUM(is_delivered)::numeric / COUNT(*) * 100, 2) as 投递率
FROM biz_ai_recommendation;

-- 平均匹配度
SELECT 
    AVG(match_score) as 平均匹配度,
    MIN(match_score) as 最低匹配度,
    MAX(match_score) as 最高匹配度
FROM biz_ai_recommendation;
```

---

## 🐛 常见问题排查

### 问题1: 推荐接口返回空数组

**可能原因**:
1. 学生没有简历
2. 简历没有向量
3. 职位没有向量
4. 数据库查询条件过滤了所有结果

**排查步骤**:
```sql
-- 1. 检查学生是否有简历
SELECT * FROM biz_student_resume WHERE student_id = 'xxx';

-- 2. 检查简历是否有向量
SELECT student_id, embedding IS NOT NULL as has_embedding 
FROM biz_student_resume 
WHERE student_id = 'xxx';

-- 3. 检查职位向量数量
SELECT COUNT(*) FROM biz_job 
WHERE status = 1 AND audit = 1 AND embedding IS NOT NULL;
```

---

### 问题2: 向量生成失败

**可能原因**:
1. Spring AI配置错误
2. API Key无效
3. 网络连接问题
4. 文本内容为空

**排查步骤**:
1. 检查application.yml配置
2. 检查API Key是否有效
3. 检查网络连接
4. 查看后端日志

---

### 问题3: 匹配度都很低(<70分)

**可能原因**:
1. 简历文本构建不完整
2. 职位文本构建不完整
3. 向量生成质量问题

**解决方案**:
1. 检查buildResumeText方法,确保包含关键信息
2. 检查buildJobText方法,确保包含关键信息
3. 重新生成向量

---

## ✅ 完成检查清单

### 必须完成
- [ ] 生成职位向量(至少10个)
- [ ] 生成简历向量(至少5个)
- [ ] 测试推荐接口返回正常
- [ ] 验证匹配度计算合理
- [ ] 验证推荐理由准确

### 可选完成
- [ ] 添加Redis缓存
- [ ] 优化异常处理
- [ ] 添加性能监控
- [ ] 记录推荐效果
- [ ] 批量生成优化

---

## 📞 需要帮助?

如果遇到问题,可以:
1. 查看后端日志
2. 查看数据库数据
3. 使用验证脚本检查
4. 参考相关文档

**相关文档**:
- [AI功能-下一步实施步骤](./AI功能-下一步实施步骤.md)
- [AI功能-快速实施指南](./AI功能-快速实施指南.md)
- [AI智能推荐功能-实现总结](./AI智能推荐功能-实现总结.md)

---

**更新日期**: 2026年2月  
**状态**: 待执行
