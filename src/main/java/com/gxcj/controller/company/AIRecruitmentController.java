package com.gxcj.controller.company;

import com.gxcj.entity.vo.ResumeMatchVo;
import com.gxcj.result.Result;
import com.gxcj.service.AIRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI招聘辅助 - 企业端
 */
@RestController
@RequestMapping("/api/company/ai")
public class AIRecruitmentController {

    @Autowired
    private AIRecommendationService recommendationService;

    /**
     * AI筛选简历
     *
     * @param jobId 职位ID
     * @param limit 返回数量，默认20
     * @return 匹配的简历列表
     */
    @GetMapping("/screen/resumes")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<List<ResumeMatchVo>> screenResumes(
            @RequestParam String jobId,
            @RequestParam(defaultValue = "20") int limit) {
        List<ResumeMatchVo> matches = recommendationService.screenResumes(jobId, limit);
        return Result.success(matches);
    }
}
