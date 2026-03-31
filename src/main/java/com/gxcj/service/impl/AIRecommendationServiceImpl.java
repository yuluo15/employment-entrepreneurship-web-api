package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.JobRecommendationVo;
import com.gxcj.entity.vo.ResumeMatchVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.AIRecommendationService;
import com.gxcj.stutas.DictTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIRecommendationServiceImpl implements AIRecommendationService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private StudentResumeMapper studentResumeMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DictDataMapper dictDataMapper;

    @Override
    public List<JobRecommendationVo> recommendJobs(String studentId, int limit) {
        // 1. 获取学生简历
        LambdaQueryWrapper<StudentResumeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentResumeEntity::getStudentId, studentId);
        StudentResumeEntity resume = studentResumeMapper.selectOne(wrapper);

        if (resume == null) {
            throw new BusinessException("简历不存在，请先完善简历");
        }

        // 2. 如果没有向量，先生成
        if (resume.getEmbedding() == null) {
            generateResumeEmbedding(studentId);
            resume = studentResumeMapper.selectOne(wrapper);
        }

        // 3. 使用原生SQL进行向量相似度搜索
        String sql = """
            SELECT 
                j.job_id,
                j.job_name,
                j.company_id,
                j.salary_range,
                j.city,
                j.tags,
                1 - (j.embedding <=> CAST(? AS vector)) as similarity
            FROM biz_job j
            WHERE j.status = 1 
              AND j.audit = 1
              AND j.embedding IS NOT NULL
            ORDER BY j.embedding <=> CAST(? AS vector)
            LIMIT ?
            """;

        String vectorString = arrayToVectorString(resume.getEmbedding());
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, vectorString, vectorString, limit);

        // 4. 构建推荐结果
        StudentResumeEntity finalResume = resume;
        return results.stream()
                .map(row -> buildRecommendation(row, finalResume))
                .collect(Collectors.toList());
    }

    @Override
    public void generateResumeEmbedding(String studentId) {
        LambdaQueryWrapper<StudentResumeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentResumeEntity::getStudentId, studentId);
        StudentResumeEntity resume = studentResumeMapper.selectOne(wrapper);

        if (resume == null) {
            throw new BusinessException("简历不存在");
        }

        // 构建简历文本
        String resumeText = buildResumeText(resume, studentId);

        // 生成向量
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(resumeText));
            float[] embedding = response.getResult().getOutput();

            // 保存向量（使用原生SQL）
            String sql = "UPDATE biz_student_resume SET embedding = CAST(? AS vector) WHERE resume_id = ?";
            String vectorString = arrayToVectorString(embedding);
            jdbcTemplate.update(sql, vectorString, resume.getResumeId());

            log.info("生成简历向量成功: studentId={}", studentId);
        } catch (Exception e) {
            log.error("生成简历向量失败: studentId={}", studentId, e);
            throw new BusinessException("生成简历向量失败: " + e.getMessage());
        }
    }

    @Override
    public void generateJobEmbedding(String jobId) {
        JobEntity job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }

        // 构建职位文本
        String jobText = buildJobText(job);

        // 生成向量
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(jobText));
            float[] embedding = response.getResult().getOutput();

            // 保存向量（使用原生SQL）
            String sql = "UPDATE biz_job SET embedding = CAST(? AS vector) WHERE job_id = ?";
            String vectorString = arrayToVectorString(embedding);
            jdbcTemplate.update(sql, vectorString, jobId);

            log.info("生成职位向量成功: jobId={}", jobId);
        } catch (Exception e) {
            log.error("生成职位向量失败: jobId={}", jobId, e);
            throw new BusinessException("生成职位向量失败: " + e.getMessage());
        }
    }

    @Override
    public void batchGenerateJobEmbeddings() {
        // 查找没有向量的职位
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(JobEntity::getStatus, 1);
//        wrapper.eq(JobEntity::getAudit, 1);
        wrapper.isNull(JobEntity::getEmbedding);
        wrapper.last("LIMIT 100"); // 每次最多处理100个

        List<JobEntity> jobs = jobMapper.selectList(wrapper);
        log.info("开始批量生成职位向量，数量：{}", jobs.size());

        int successCount = 0;
        int failCount = 0;

        for (JobEntity job : jobs) {
            try {
                generateJobEmbedding(job.getId());
                successCount++;
            } catch (Exception e) {
                log.error("生成职位向量失败: jobId={}", job.getId(), e);
                failCount++;
            }
        }

        log.info("批量生成职位向量完成，成功：{}，失败：{}", successCount, failCount);
    }

    @Override
    public void batchGenerateResumeEmbeddings() {
        // 查找没有向量的简历
        LambdaQueryWrapper<StudentResumeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentResumeEntity::getIsPublic, 1);
        wrapper.isNull(StudentResumeEntity::getEmbedding);
        wrapper.last("LIMIT 100"); // 每次最多处理100个

        List<StudentResumeEntity> resumes = studentResumeMapper.selectList(wrapper);
        log.info("开始批量生成简历向量，数量：{}", resumes.size());

        int successCount = 0;
        int failCount = 0;

        for (StudentResumeEntity resume : resumes) {
            try {
                generateResumeEmbedding(resume.getStudentId());
                successCount++;
            } catch (Exception e) {
                log.error("生成简历向量失败: studentId={}", resume.getStudentId(), e);
                failCount++;
            }
        }

        log.info("批量生成简历向量完成，成功：{}，失败：{}", successCount, failCount);
    }

    /**
     * 构建简历文本
     */
    private String buildResumeText(StudentResumeEntity resume, String studentId) {
        StringBuilder sb = new StringBuilder();

        // 获取学生基本信息
        StudentEntity student = studentMapper.selectById(studentId);
        if (student != null) {
            if (StringUtils.hasText(student.getMajorName())) {
                sb.append("专业：").append(student.getMajorName()).append("\n");
            }
            if (StringUtils.hasText(student.getEducation())) {
                sb.append("学历：").append(student.getEducation()).append("\n");
            }
        }

        // 求职意向
        if (StringUtils.hasText(resume.getExpectedPosition())) {
            sb.append("求职意向：").append(resume.getExpectedPosition()).append("\n");
        }
        if (StringUtils.hasText(resume.getExpectedSalary())) {
            sb.append("期望薪资：").append(resume.getExpectedSalary()).append("\n");
        }
        if (StringUtils.hasText(resume.getTargetCity())) {
            sb.append("期望城市：").append(resume.getTargetCity()).append("\n");
        }

        // 个人总结
        if (StringUtils.hasText(resume.getPersonalSummary())) {
            sb.append("个人简介：").append(resume.getPersonalSummary()).append("\n");
        }

        // 技能
        if (StringUtils.hasText(resume.getSkills())) {
            sb.append("技能：").append(resume.getSkills()).append("\n");
        }

        // 教育经历
        if (resume.getEducationHistory() != null && !resume.getEducationHistory().isEmpty()) {
            sb.append("教育经历：");
            for (StudentResumeEntity.EducationItem item : resume.getEducationHistory()) {
                sb.append(item.getSchool()).append(" ").append(item.getMajor()).append(" ").append(item.getDegree()).append("；");
            }
            sb.append("\n");
        }

        // 实习经历
        if (resume.getInternshipExp() != null && !resume.getInternshipExp().isEmpty()) {
            sb.append("实习经历：");
            for (StudentResumeEntity.InternshipItem item : resume.getInternshipExp()) {
                sb.append(item.getCompany()).append(" ").append(item.getRole()).append("；");
            }
            sb.append("\n");
        }

        // 项目经验
        if (resume.getProjectExp() != null && !resume.getProjectExp().isEmpty()) {
            sb.append("项目经验：");
            for (StudentResumeEntity.ProjectItem item : resume.getProjectExp()) {
                sb.append(item.getProjectName()).append(" ").append(item.getRole()).append("；");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 构建职位文本
     */
    private String buildJobText(JobEntity job) {
        StringBuilder sb = new StringBuilder();

        sb.append("职位名称：").append(job.getJobName()).append("\n");
        sb.append("薪资范围：").append(job.getSalaryRange()).append("\n");
        sb.append("工作城市：").append(job.getCity()).append("\n");

        if (StringUtils.hasText(job.getEducation())) {
            sb.append("学历要求：").append(job.getEducation()).append("\n");
        }
        if (StringUtils.hasText(job.getExperience())) {
            sb.append("经验要求：").append(job.getExperience()).append("\n");
        }
        if (StringUtils.hasText(job.getTags())) {
            sb.append("技能标签：").append(job.getTags()).append("\n");
        }
        if (StringUtils.hasText(job.getDescription())) {
            sb.append("职位描述：").append(job.getDescription()).append("\n");
        }
        if (StringUtils.hasText(job.getRequirement())) {
            sb.append("任职要求：").append(job.getRequirement()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 构建推荐结果
     */
    private JobRecommendationVo buildRecommendation(Map<String, Object> row, StudentResumeEntity resume) {
        String jobId = (String) row.get("job_id");
        String jobName = (String) row.get("job_name");
        String companyId = (String) row.get("company_id");
        String salaryRange = (String) row.get("salary_range");
        String city = (String) row.get("city");
        String tags = (String) row.get("tags");
        Double similarity = (Double) row.get("similarity");

        // 计算匹配度
        int matchScore = (int) (similarity * 100);

        // 获取公司信息
        CompanyEntity company = companyMapper.selectById(companyId);
        String companyName = company != null ? company.getName() : "未知公司";
        String companyLogo = company != null ? company.getLogo() : null;

        // 生成推荐理由
        String reason = generateRecommendationReason(matchScore);

        return JobRecommendationVo.builder()
                .jobId(jobId)
                .title(jobName)
                .companyName(companyName)
                .companyLogo(companyLogo)
                .salaryRange(salaryRange)
                .location(city)
                .tags(tags)
                .matchScore(matchScore)
                .recommendReason(reason)
                .build();
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

    /**
     * 将float数组转换为PostgreSQL vector字符串格式
     */
    private String arrayToVectorString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<ResumeMatchVo> screenResumes(String jobId, int limit) {
        // 1. 获取职位信息
        JobEntity job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("职位不存在");
        }

        // 2. 如果职位没有向量，先生成
        if (job.getEmbedding() == null) {
            generateJobEmbedding(jobId);
            job = jobMapper.selectById(jobId);
        }

        // 3. 使用原生SQL进行向量相似度搜索
        String sql = """
            SELECT 
                r.resume_id,
                r.student_id,
                r.expected_position,
                r.expected_salary,
                r.target_city,
                r.skills,
                1 - (r.embedding <=> CAST(? AS vector)) as similarity
            FROM biz_student_resume r
            WHERE r.is_public = 1
              AND r.embedding IS NOT NULL
            ORDER BY r.embedding <=> CAST(? AS vector)
            LIMIT ?
            """;

        String vectorString = arrayToVectorString(job.getEmbedding());
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, vectorString, vectorString, limit);

        // 4. 构建匹配结果
        JobEntity finalJob = job;
        return results.stream()
                .map(row -> buildResumeMatch(row, finalJob))
                .collect(Collectors.toList());
    }

    /**
     * 构建简历匹配结果
     */
    private ResumeMatchVo buildResumeMatch(Map<String, Object> row, JobEntity job) {
        String resumeId = (String) row.get("resume_id");
        String studentId = (String) row.get("student_id");
        String expectedPosition = (String) row.get("expected_position");
        String expectedSalary = (String) row.get("expected_salary");
        String targetCity = (String) row.get("target_city");
        String skills = (String) row.get("skills");
        Double similarity = (Double) row.get("similarity");

        // 计算匹配度
        int matchScore = (int) (similarity * 100);

        Map<String, String> map = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(DictDataEntity::getDictType, DictTypeEnum.sys_education.name())).stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

        // 获取学生基本信息
        StudentEntity student = studentMapper.selectById(studentId);

        // 生成匹配理由
        String matchReason = generateMatchReason(matchScore);

        return ResumeMatchVo.builder()
                .resumeId(resumeId)
                .studentId(studentId)
                .studentName(student != null ? student.getStudentName() : "")
                .school(student != null ? student.getCollegeName() : "")
                .major(student != null ? student.getMajorName() : "")
                .education(student != null ? map.get(student.getEducation()) : "")
                .graduationYear(student != null ? student.getGraduationYear() : null)
                .expectedPosition(expectedPosition)
                .expectedSalary(expectedSalary)
                .targetCity(targetCity)
                .skills(skills)
                .matchScore(matchScore)
                .matchReason(matchReason)
                .phone(student != null ? maskPhone(student.getPhone()) : "")
                .email(student != null ? maskEmail(student.getEmail()) : "")
                .build();
    }

    /**
     * 生成匹配理由
     */
    private String generateMatchReason(int matchScore) {
        if (matchScore >= 90) {
            return "候选人的技能和经验与职位要求高度匹配";
        } else if (matchScore >= 80) {
            return "候选人的专业背景与职位要求相符";
        } else if (matchScore >= 70) {
            return "候选人具备职位所需的基本能力";
        } else {
            return "候选人可能适合该职位";
        }
    }

    /**
     * 手机号脱敏: 138****1234
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏: zha***@example.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 3) {
            return email;
        }
        return name.substring(0, 3) + "***@" + parts[1];
    }
}
