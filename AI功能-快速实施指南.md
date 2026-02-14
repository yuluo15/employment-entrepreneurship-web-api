# AI功能快速实施指南

## 实施步骤（按优先级）

### 阶段一：智能职位推荐（核心功能）⭐⭐⭐⭐⭐

**预计时间**: 2-3天  
**技术难度**: ⭐⭐⭐  
**用户价值**: ⭐⭐⭐⭐⭐

#### 后端实现（1.5天）

1. **配置Spring AI**
   - 添加依赖
   - 配置OpenAI API Key
   - 测试Embedding生成

2. **数据库改造**
   - 添加vector字段
   - 创建向量索引
   - 测试向量查询

3. **实现推荐服务**
   - 简历向量生成
   - 职位向量生成
   - 相似度搜索
   - 推荐结果构建

4. **实现API接口**
   - GET /api/mobile/ai/recommend/jobs
   - POST /api/mobile/ai/refresh/resume

#### 前端实现（0.5天）

1. **首页添加AI推荐模块**
   - 创建推荐卡片组件
   - 调用API获取推荐
   - 展示匹配度和理由

2. **创建API文件**
   - src/api/mobile/ai.ts

#### 测试验证（1天）

1. 生成测试数据
2. 验证推荐准确性
3. 性能测试
4. 用户体验测试

---

### 阶段二：AI简历筛选（企业端）⭐⭐⭐⭐

**预计时间**: 1-2天  
**技术难度**: ⭐⭐  
**用户价值**: ⭐⭐⭐⭐

#### 实现要点

1. 企业查看简历时自动计算匹配度
2. 按匹配度排序简历列表
3. 显示匹配分数和理由

---

### 阶段三：简历优化建议（可选）⭐⭐⭐

**预计时间**: 1天  
**技术难度**: ⭐⭐  
**用户价值**: ⭐⭐⭐

#### 实现要点

1. 调用Chat API分析简历
2. 生成优化建议
3. 前端展示建议列表

---

## 最小可行方案（MVP）

如果时间紧张，只实现**智能职位推荐**即可：

1. 后端：推荐服务 + API接口
2. 前端：首页推荐模块
3. 数据库：向量字段 + 索引

**核心代码量**: 约500行  
**实施时间**: 2天  
**演示效果**: 非常直观

---

## 技术选型建议

### 方案A：OpenAI（推荐）

**优点**:
- 效果最好
- 文档完善
- Spring AI原生支持

**缺点**:
- 需要科学上网
- 有API成本（但很低）

**成本**: 约$10/月

### 方案B：国产模型

**推荐模型**:
- 通义千问（阿里）
- 文心一言（百度）
- 智谱AI

**优点**:
- 国内访问快
- 有免费额度
- 支持私有化部署

**缺点**:
- Spring AI支持较少
- 需要额外适配

---

## 演示准备

### 1. 准备演示数据

```sql
-- 插入测试职位（带向量）
INSERT INTO biz_job (job_id, title, description, skills, embedding, ...)
VALUES (...);

-- 插入测试简历（带向量）
INSERT INTO biz_resume (resume_id, student_id, skills, experience, embedding, ...)
VALUES (...);
```

### 2. 演示流程

1. **登录学生端**
2. **查看首页** → 显示AI推荐职位
3. **查看匹配度** → 95%、88%、82%...
4. **查看推荐理由** → "你的Java技能与该职位高度匹配"
5. **点击职位** → 跳转详情页
6. **修改简历** → 推荐结果实时更新

### 3. 演示话术

```
"这是我们系统的核心AI功能 - 智能职位推荐。

系统使用了最新的向量检索技术，将学生的简历和职位描述
转换为高维向量，通过计算语义相似度，为每个学生推荐
最匹配的职位。

你看，这里显示了匹配度95%的职位，系统还给出了推荐理由。
这不是简单的关键词匹配，而是真正理解了简历和职位的语义。

我们使用了Spring AI框架和PostgreSQL的pgvector扩展，
实现了高效的向量检索，查询速度在100ms以内。"
```

---

## 常见问题

### Q1: OpenAI API访问不了怎么办？

**方案1**: 使用代理  
**方案2**: 使用国产模型（通义千问、文心一言）  
**方案3**: 使用本地模型（Ollama + Llama）

### Q2: 向量生成太慢怎么办？

**方案1**: 批量生成（一次生成多个）  
**方案2**: 异步生成（后台任务）  
**方案3**: 缓存向量（Redis）

### Q3: 推荐结果不准确怎么办？

**方案1**: 优化文本构建（增加关键信息）  
**方案2**: 调整相似度阈值  
**方案3**: 加入其他因素（薪资、地点）

### Q4: 成本太高怎么办？

**方案1**: 使用国产模型（更便宜）  
**方案2**: 缓存向量（减少API调用）  
**方案3**: 批量处理（降低单次成本）

---

## 代码模板

### Spring AI配置

```java
@Configuration
public class AIConfig {
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Bean
    public OpenAiEmbeddingClient embeddingClient() {
        return new OpenAiEmbeddingClient(
            new OpenAiApi(apiKey)
        );
    }
    
    @Bean
    public OpenAiChatClient chatClient() {
        return new OpenAiChatClient(
            new OpenAiApi(apiKey)
        );
    }
}
```

### 向量类型映射

```java
@Entity
@Table(name = "biz_job")
public class Job {
    
    @Id
    private String jobId;
    
    // 其他字段...
    
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;
    
    // getter/setter
}
```

### Repository查询

```java
@Query(value = """
    SELECT * FROM biz_job
    WHERE embedding IS NOT NULL
    ORDER BY embedding <=> CAST(:vector AS vector)
    LIMIT :limit
    """, nativeQuery = true)
List<Job> findSimilarJobs(
    @Param("vector") float[] vector,
    @Param("limit") int limit
);
```

---

## 参考资料

- [Spring AI官方文档](https://docs.spring.io/spring-ai/reference/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [OpenAI Embeddings API](https://platform.openai.com/docs/guides/embeddings)
- [向量数据库原理](https://www.pinecone.io/learn/vector-database/)

---

## 总结

### 推荐实施方案

**最小方案**（2天）:
- 智能职位推荐（学生端首页）

**完整方案**（4-5天）:
- 智能职位推荐
- AI简历筛选
- 简历优化建议

### 技术亮点

1. ✅ Spring AI + pgvector（最新技术栈）
2. ✅ 向量检索（高效、准确）
3. ✅ 语义理解（不是简单关键词匹配）
4. ✅ 实时推荐（查询速度<100ms）
5. ✅ 可扩展性（可以继续添加更多AI功能）

### 毕业设计价值

1. **技术前沿**: AI + 向量数据库
2. **实用价值**: 真正解决就业匹配问题
3. **可演示性**: 效果直观可见
4. **论文素材**: 算法、架构、性能优化
5. **就业竞争力**: 掌握AI应用开发

---

**建议**: 先实现智能职位推荐，效果好再扩展其他功能！
