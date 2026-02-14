package com.gxcj.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.JobEntity;
import com.gxcj.entity.StudentResumeEntity;
import com.gxcj.mapper.JobMapper;
import com.gxcj.mapper.StudentResumeMapper;
import com.gxcj.service.AIRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI向量生成定时任务
 * 用于批量生成职位和简历的向量数据
 */
@Component
@Slf4j
public class EmbeddingGenerationTask {

    @Autowired
    private AIRecommendationService recommendationService;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private StudentResumeMapper studentResumeMapper;

    /**
     * 批量生成职位向量
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateJobEmbeddings() {
        // 查找没有向量的职位
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getStatus, 1);
        wrapper.eq(JobEntity::getAudit, 1);
        wrapper.isNull(JobEntity::getEmbedding);
        wrapper.last("LIMIT 100"); // 每次最多处理100个

        List<JobEntity> jobs = jobMapper.selectList(wrapper);
        log.info("定时任务：开始生成职位向量，数量：{}", jobs.size());

        int successCount = 0;
        int failCount = 0;

        for (JobEntity job : jobs) {
            try {
                recommendationService.generateJobEmbedding(job.getId());
                successCount++;
            } catch (Exception e) {
                log.error("定时任务：生成职位向量失败: jobId={}", job.getId(), e);
                failCount++;
            }
        }

        log.info("定时任务：职位向量生成完成，成功：{}，失败：{}", successCount, failCount);
    }

    /**
     * 批量生成简历向量
     * 每天凌晨2:30执行
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void generateResumeEmbeddings() {
        // 查找没有向量的简历
        LambdaQueryWrapper<StudentResumeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentResumeEntity::getIsPublic, 1);
        wrapper.isNull(StudentResumeEntity::getEmbedding);
        wrapper.last("LIMIT 100"); // 每次最多处理100个

        List<StudentResumeEntity> resumes = studentResumeMapper.selectList(wrapper);
        log.info("定时任务：开始生成简历向量，数量：{}", resumes.size());

        int successCount = 0;
        int failCount = 0;

        for (StudentResumeEntity resume : resumes) {
            try {
                recommendationService.generateResumeEmbedding(resume.getStudentId());
                successCount++;
            } catch (Exception e) {
                log.error("定时任务：生成简历向量失败: studentId={}", resume.getStudentId(), e);
                failCount++;
            }
        }

        log.info("定时任务：简历向量生成完成，成功：{}，失败：{}", successCount, failCount);
    }
}
