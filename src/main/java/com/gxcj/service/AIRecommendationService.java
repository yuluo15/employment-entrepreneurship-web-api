package com.gxcj.service;

import com.gxcj.entity.vo.JobRecommendationVo;
import com.gxcj.entity.vo.ResumeMatchVo;

import java.util.List;

public interface AIRecommendationService {
    /**
     * 为学生推荐职位
     */
    List<JobRecommendationVo> recommendJobs(String studentId, int limit);
    
    /**
     * 为职位筛选匹配的简历（企业端）
     */
    List<ResumeMatchVo> screenResumes(String jobId, int limit);
    
    /**
     * 生成简历向量
     */
    void generateResumeEmbedding(String studentId);
    
    /**
     * 生成职位向量
     */
    void generateJobEmbedding(String jobId);
    
    /**
     * 批量生成职位向量
     */
    void batchGenerateJobEmbeddings();
    
    /**
     * 批量生成简历向量
     */
    void batchGenerateResumeEmbeddings();
}
