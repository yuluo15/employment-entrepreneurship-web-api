-- ============================================
-- AI简历筛选功能 - 测试SQL
-- ============================================

-- 1. 检查职位是否有向量
SELECT 
    job_id, 
    job_name, 
    company_id,
    embedding IS NOT NULL as has_embedding
FROM biz_job
WHERE status = 1 AND audit = 1
LIMIT 10;

-- 2. 检查简历是否有向量
SELECT 
    resume_id, 
    student_id, 
    expected_position,
    embedding IS NOT NULL as has_embedding
FROM biz_student_resume
WHERE is_public = 1
LIMIT 10;

-- 3. 测试向量相似度查询（为指定职位查找匹配的简历）
-- 替换 'YOUR_JOB_ID' 为实际的职位ID
SELECT 
    r.resume_id,
    r.student_id,
    s.student_name,
    s.college_name,
    s.major_name,
    r.expected_position,
    r.skills,
    1 - (r.embedding <=> (SELECT embedding FROM biz_job WHERE job_id = 'YOUR_JOB_ID')) as similarity,
    ROUND((1 - (r.embedding <=> (SELECT embedding FROM biz_job WHERE job_id = 'YOUR_JOB_ID'))) * 100) as match_score
FROM biz_student_resume r
JOIN biz_student s ON r.student_id = s.student_id
WHERE r.is_public = 1
  AND r.embedding IS NOT NULL
ORDER BY r.embedding <=> (SELECT embedding FROM biz_job WHERE job_id = 'YOUR_JOB_ID')
LIMIT 20;

-- 4. 查看向量生成统计
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
WHERE is_public = 1;

-- 5. 查看某个职位的详细信息
SELECT 
    job_id,
    job_name,
    salary_range,
    city,
    education,
    tags,
    description,
    requirement,
    embedding IS NOT NULL as has_embedding
FROM biz_job
WHERE job_id = 'YOUR_JOB_ID';
