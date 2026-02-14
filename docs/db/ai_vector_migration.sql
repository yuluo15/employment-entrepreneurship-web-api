-- ============================================
-- AI向量功能数据库迁移脚本
-- ============================================
-- 功能：为智能职位推荐添加向量字段
-- 使用：PostgreSQL + pgvector扩展
-- ============================================

-- 1. 启用pgvector扩展（如果还没有启用）
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================
-- 核心表：添加向量字段
-- ============================================

-- 2. 职位表（biz_job）添加向量字段
-- 用途：存储职位描述的语义向量，用于职位推荐
ALTER TABLE biz_job 
ADD COLUMN IF NOT EXISTS embedding vector(1536);

COMMENT ON COLUMN biz_job.embedding IS 'AI生成的职位描述向量（1536维），用于语义相似度搜索';

-- 3. 学生简历表（biz_student_resume）添加向量字段
-- 用途：存储简历的语义向量，用于职位匹配
ALTER TABLE biz_student_resume 
ADD COLUMN IF NOT EXISTS embedding vector(1536);

COMMENT ON COLUMN biz_student_resume.embedding IS 'AI生成的简历向量（1536维），用于职位匹配';

-- ============================================
-- 创建向量索引（加速相似度搜索）
-- ============================================

-- 4. 为职位表创建向量索引
-- 使用ivfflat索引算法，适合大规模向量检索
-- lists参数：建议设置为 行数/1000，这里假设有10000个职位
CREATE INDEX IF NOT EXISTS idx_biz_job_embedding 
ON biz_job 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

COMMENT ON INDEX idx_biz_job_embedding IS '职位向量余弦相似度索引，加速AI推荐查询';

-- 5. 为简历表创建向量索引
-- lists参数：假设有50000个学生
CREATE INDEX IF NOT EXISTS idx_biz_student_resume_embedding 
ON biz_student_resume 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

COMMENT ON INDEX idx_biz_student_resume_embedding IS '简历向量余弦相似度索引，加速简历筛选';

-- ============================================
-- 可选：创业项目表添加向量字段
-- ============================================

-- 6. 创业项目表（biz_project）添加向量字段（可选）
-- 用途：用于项目推荐、项目匹配
ALTER TABLE biz_project 
ADD COLUMN IF NOT EXISTS embedding vector(1536);

COMMENT ON COLUMN biz_project.embedding IS 'AI生成的项目描述向量（1536维），用于项目推荐';

-- 7. 为项目表创建向量索引
CREATE INDEX IF NOT EXISTS idx_biz_project_embedding 
ON biz_project 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 50);

COMMENT ON INDEX idx_biz_project_embedding IS '项目向量余弦相似度索引，用于项目推荐';

-- ============================================
-- 辅助表：AI推荐记录表（可选）
-- ============================================

-- 8. 创建AI推荐记录表（用于记录推荐历史和效果分析）
CREATE TABLE IF NOT EXISTS biz_ai_recommendation (
    id varchar(255) PRIMARY KEY,
    student_id varchar(255) NOT NULL,
    job_id varchar(255) NOT NULL,
    match_score int NOT NULL,  -- 匹配度分数（0-100）
    recommend_reason varchar(500),  -- 推荐理由
    is_clicked int DEFAULT 0,  -- 是否点击查看
    is_delivered int DEFAULT 0,  -- 是否投递
    create_time timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_student FOREIGN KEY (student_id) REFERENCES biz_student(student_id),
    CONSTRAINT fk_recommendation_job FOREIGN KEY (job_id) REFERENCES biz_job(job_id)
);

CREATE INDEX idx_ai_recommendation_student ON biz_ai_recommendation(student_id);
CREATE INDEX idx_ai_recommendation_job ON biz_ai_recommendation(job_id);
CREATE INDEX idx_ai_recommendation_score ON biz_ai_recommendation(match_score DESC);

COMMENT ON TABLE biz_ai_recommendation IS 'AI推荐记录表，用于分析推荐效果';
COMMENT ON COLUMN biz_ai_recommendation.match_score IS '匹配度分数（0-100），基于向量相似度计算';
COMMENT ON COLUMN biz_ai_recommendation.is_clicked IS '用户是否点击查看该推荐（0=否，1=是）';
COMMENT ON COLUMN biz_ai_recommendation.is_delivered IS '用户是否投递该推荐职位（0=否，1=是）';

-- ============================================
-- 辅助表：向量生成任务队列（可选）
-- ============================================

-- 9. 创建向量生成任务表（用于异步生成向量）
CREATE TABLE IF NOT EXISTS biz_embedding_task (
    id varchar(255) PRIMARY KEY,
    target_type varchar(50) NOT NULL,  -- JOB, RESUME, PROJECT
    target_id varchar(255) NOT NULL,
    status varchar(20) DEFAULT 'PENDING',  -- PENDING, PROCESSING, COMPLETED, FAILED
    retry_count int DEFAULT 0,
    error_message text,
    create_time timestamp DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_embedding_task UNIQUE (target_type, target_id)
);

CREATE INDEX idx_embedding_task_status ON biz_embedding_task(status);
CREATE INDEX idx_embedding_task_type ON biz_embedding_task(target_type);

COMMENT ON TABLE biz_embedding_task IS '向量生成任务队列，用于异步处理向量生成';
COMMENT ON COLUMN biz_embedding_task.target_type IS '目标类型：JOB=职位，RESUME=简历，PROJECT=项目';
COMMENT ON COLUMN biz_embedding_task.status IS '任务状态：PENDING=待处理，PROCESSING=处理中，COMPLETED=已完成，FAILED=失败';

-- ============================================
-- 测试查询示例
-- ============================================

-- 10. 测试向量相似度查询（职位推荐）
-- 说明：这个查询会找到与给定简历向量最相似的10个职位
-- 使用方法：将 :resume_vector 替换为实际的简历向量
/*
SELECT 
    j.job_id,
    j.job_name,
    j.company_id,
    j.salary_range,
    j.city,
    1 - (j.embedding <=> :resume_vector::vector) as similarity,
    ROUND((1 - (j.embedding <=> :resume_vector::vector)) * 100) as match_score
FROM biz_job j
WHERE j.status = 1 
  AND j.audit = 1
  AND j.embedding IS NOT NULL
ORDER BY j.embedding <=> :resume_vector::vector
LIMIT 10;
*/

-- 11. 测试向量相似度查询（简历筛选）
-- 说明：这个查询会找到与给定职位向量最相似的10份简历
-- 使用方法：将 :job_vector 替换为实际的职位向量
/*
SELECT 
    r.resume_id,
    r.student_id,
    s.student_name,
    s.major_name,
    r.expected_position,
    1 - (r.embedding <=> :job_vector::vector) as similarity,
    ROUND((1 - (r.embedding <=> :job_vector::vector)) * 100) as match_score
FROM biz_student_resume r
JOIN biz_student s ON r.student_id = s.student_id
WHERE r.is_public = 1
  AND r.embedding IS NOT NULL
ORDER BY r.embedding <=> :job_vector::vector
LIMIT 10;
*/

-- ============================================
-- 数据统计查询
-- ============================================

-- 12. 查看已生成向量的数据统计
SELECT 
    '职位' as 类型,
    COUNT(*) as 总数,
    COUNT(embedding) as 已生成向量数,
    ROUND(COUNT(embedding)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as 完成率
FROM biz_job
WHERE status = 1 AND audit = 1

UNION ALL

SELECT 
    '简历' as 类型,
    COUNT(*) as 总数,
    COUNT(embedding) as 已生成向量数,
    ROUND(COUNT(embedding)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as 完成率
FROM biz_student_resume
WHERE is_public = 1

UNION ALL

SELECT 
    '项目' as 类型,
    COUNT(*) as 总数,
    COUNT(embedding) as 已生成向量数,
    ROUND(COUNT(embedding)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as 完成率
FROM biz_project
WHERE status = '1';

-- ============================================
-- 清理脚本（谨慎使用！）
-- ============================================

-- 如果需要重新生成所有向量，可以使用以下命令清空向量字段
-- 警告：这会删除所有已生成的向量数据！
/*
UPDATE biz_job SET embedding = NULL;
UPDATE biz_student_resume SET embedding = NULL;
UPDATE biz_project SET embedding = NULL;
*/

-- 如果需要完全移除向量功能，可以使用以下命令
-- 警告：这会删除所有向量相关的表和字段！
/*
DROP TABLE IF EXISTS biz_ai_recommendation CASCADE;
DROP TABLE IF EXISTS biz_embedding_task CASCADE;
DROP INDEX IF EXISTS idx_biz_job_embedding;
DROP INDEX IF EXISTS idx_biz_student_resume_embedding;
DROP INDEX IF EXISTS idx_biz_project_embedding;
ALTER TABLE biz_job DROP COLUMN IF EXISTS embedding;
ALTER TABLE biz_student_resume DROP COLUMN IF EXISTS embedding;
ALTER TABLE biz_project DROP COLUMN IF EXISTS embedding;
*/

-- ============================================
-- 执行完成提示
-- ============================================

-- 执行此脚本后，请按以下步骤操作：
-- 1. 确认pgvector扩展已启用：SELECT * FROM pg_extension WHERE extname = 'vector';
-- 2. 确认向量字段已添加：\d biz_job, \d biz_student_resume
-- 3. 确认索引已创建：\di idx_biz_job_embedding
-- 4. 开始实现Spring AI服务，生成向量数据
-- 5. 测试向量相似度查询

SELECT '✅ AI向量功能数据库迁移完成！' as 状态;
SELECT '📝 下一步：实现Spring AI服务，开始生成向量数据' as 提示;
