package com.gxcj.controller.admin;

import com.gxcj.result.Result;
import com.gxcj.service.AIRecommendationService;
import com.gxcj.task.EmbeddingGenerationTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI管理接口
 * 用于管理员手动触发向量生成任务
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AIAdminController {

    @Autowired
    private AIRecommendationService recommendationService;

    @Autowired
    private EmbeddingGenerationTask embeddingTask;

    /**
     * 批量生成职位向量（通过Service）
     */
    @PostMapping("/generate/jobs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> generateJobEmbeddings() {
        recommendationService.batchGenerateJobEmbeddings();
        return Result.success();
    }

    /**
     * 批量生成简历向量（通过Service）
     */
    @PostMapping("/generate/resumes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> generateResumeEmbeddings() {
        recommendationService.batchGenerateResumeEmbeddings();
        return Result.success();
    }

    /**
     * 手动触发职位向量生成定时任务
     */
    @PostMapping("/task/generate/jobs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> triggerJobEmbeddingTask() {
        embeddingTask.generateJobEmbeddings();
        return Result.success();
    }

    /**
     * 手动触发简历向量生成定时任务
     */
    @PostMapping("/task/generate/resumes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> triggerResumeEmbeddingTask() {
        embeddingTask.generateResumeEmbeddings();
        return Result.success();
    }
}
