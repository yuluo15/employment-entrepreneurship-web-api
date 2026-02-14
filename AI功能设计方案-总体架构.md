# AI功能设计方案 - 总体架构

## 技术栈

- **Spring AI**: 调用大模型、生成Embedding
- **PostgreSQL**: 存储结构化数据
- **pgvector**: 存储和检索向量数据
- **Embedding模型**: text-embedding-ada-002 (OpenAI) 或国产模型

---

## 核心AI功能列表

### 1. 智能职位推荐（学生端）⭐⭐⭐⭐⭐
**优先级**: 最高  
**技术难度**: 中等  
**用户价值**: 极高

基于学生简历和职位描述的向量相似度，推荐最匹配的职位。

### 2. AI简历筛选（企业端）⭐⭐⭐⭐
**优先级**: 高  
**技术难度**: 中等  
**用户价值**: 高

帮助企业快速筛选匹配的简历，提高招聘效率。

### 3. 简历优化建议（学生端）⭐⭐⭐
**优先级**: 中  
**技术难度**: 低  
**用户价值**: 中

AI分析简历质量，给出优化建议。

### 4. 项目描述生成（学生端）⭐⭐
**优先级**: 低  
**技术难度**: 低  
**用户价值**: 中

帮助学生生成创业项目描述。

---

## 推荐实现顺序

1. **第一阶段**：智能职位推荐（核心功能）
2. **第二阶段**：AI简历筛选（企业端价值）
3. **第三阶段**：简历优化建议（锦上添花）
4. **第四阶段**：项目描述生成（可选）

---

## 数据流架构

```
┌─────────────────────────────────────────────────────────┐
│                     前端应用层                            │
│  学生端 | 企业端 | 学校端 | 教师端 | 管理员端              │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot 后端                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ AI推荐服务   │  │ AI筛选服务   │  │ AI优化服务   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Spring AI                            │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │ Embedding    │  │ Chat Client  │                    │
│  │ Client       │  │              │                    │
│  └──────────────┘  └──────────────┘                    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                  PostgreSQL + pgvector                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 职位表       │  │ 简历表       │  │ 向量索引     │  │
│  │ + embedding  │  │ + embedding  │  │ (ivfflat)    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 向量数据库设计

### 职位表（biz_job）

```sql
ALTER TABLE biz_job ADD COLUMN embedding vector(1536);
CREATE INDEX ON biz_job USING ivfflat (embedding vector_cosine_ops);
```

### 简历表（biz_resume）

```sql
CREATE TABLE biz_resume (
  resume_id varchar(64) PRIMARY KEY,
  student_id varchar(64) NOT NULL,
  major varchar(100),
  skills text,
  experience text,
  job_intent text,
  embedding vector(1536),  -- 向量字段
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ON biz_resume USING ivfflat (embedding vector_cosine_ops);
```

---

## Spring AI 配置

### application.yml

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-ada-002
```

### 依赖配置

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>com.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.1.4</version>
</dependency>
```

---

## 成本估算

### OpenAI API 成本

- **Embedding**: $0.0001 / 1K tokens
  - 每个职位/简历约500 tokens
  - 1000个职位 = $0.05
  - 10000个学生 = $0.50

- **Chat**: $0.002 / 1K tokens
  - 每次简历优化约1000 tokens
  - 1000次优化 = $2.00

**月成本估算**: $10-50（取决于使用量）

### 国产替代方案

- 通义千问
- 文心一言
- 智谱AI

成本更低，甚至有免费额度。

---

## 性能优化

### 1. 向量缓存

```java
@Cacheable(value = "job-embeddings", key = "#jobId")
public float[] getJobEmbedding(String jobId) {
    // 从数据库读取或生成
}
```

### 2. 批量生成向量

```java
public void batchGenerateEmbeddings(List<Job> jobs) {
    List<String> texts = jobs.stream()
        .map(this::buildJobText)
        .collect(Collectors.toList());
    
    EmbeddingResponse response = embeddingClient.embedForResponse(texts);
    
    // 批量更新数据库
}
```

### 3. 异步处理

```java
@Async
public CompletableFuture<List<Job>> recommendJobsAsync(String studentId) {
    return CompletableFuture.completedFuture(
        recommendJobs(studentId, 10)
    );
}
```

---

## 数据隐私和安全

1. **向量数据脱敏**: 向量本身不包含原始文本
2. **权限控制**: 学生只能看到自己的推荐
3. **数据加密**: 敏感信息加密存储
4. **API密钥管理**: 使用环境变量，不提交代码

---

## 毕业设计亮点

1. **技术前沿**: Spring AI + pgvector 是最新的AI技术栈
2. **实用价值**: 真正解决就业匹配问题
3. **可演示性**: 推荐结果直观可见
4. **可扩展性**: 可以继续添加更多AI功能
5. **论文素材**: 向量检索、相似度算法、AI应用

---

## 相关文档

- [智能职位推荐详细设计](./AI功能设计方案-智能推荐.md)
- [AI简历筛选详细设计](./AI功能设计方案-简历筛选.md)
- [简历优化建议详细设计](./AI功能设计方案-简历优化.md)
