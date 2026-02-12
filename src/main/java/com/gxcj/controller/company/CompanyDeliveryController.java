package com.gxcj.controller.company;

import com.gxcj.context.UserContext;
import com.gxcj.entity.JobEntity;
import com.gxcj.entity.StudentResumeEntity;
import com.gxcj.entity.query.DeliveryQuery;
import com.gxcj.entity.vo.DeliveryVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.DeliveryService;
import com.gxcj.service.JobService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业端简历管理接口
 */
@RestController
@RequestMapping("/api/company")
@Validated
public class CompanyDeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private JobService jobService;

    /**
     * 1. 获取投递简历列表（分页）
     */
    @GetMapping("/delivery/list")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<PageResult<DeliveryVo>> getDeliveryList(DeliveryQuery query) {
        PageResult<DeliveryVo> pageResult = deliveryService.getDeliveryList(query, UserContext.getUserId());
        return Result.success(pageResult);
    }

    /**
     * 2. 获取简历详情
     */
    @GetMapping("/resume/detail/{resumeId}")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<StudentResumeEntity> getResumeDetail(@PathVariable("resumeId") String resumeId) {
        StudentResumeEntity resume = deliveryService.getResumeDetail(resumeId, UserContext.getUserId());
        return Result.success(resume);
    }

    /**
     * 3. 安排面试
     */
    @PostMapping("/interview/arrange")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Map<String, String>> arrangeInterview(@RequestBody @Valid InterviewArrangeReq req) {
        String interviewId = deliveryService.arrangeInterview(req, UserContext.getUserId());
        return Result.success(Map.of("interviewId", interviewId));
    }

    /**
     * 4. 拒绝简历
     */
    @PostMapping("/delivery/reject")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> rejectDelivery(@RequestBody @Valid RejectReq req) {
        deliveryService.rejectDelivery(req.getDeliveryId(), req.getReason(), UserContext.getUserId());
        return Result.success();
    }

    /**
     * 5. 获取公司职位列表（用于筛选）
     */
    @GetMapping("/job/options")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<List<Map<String, String>>> getJobOptions() {
        // 使用已有的getCompanyJobList方法，但只返回id和jobName
        com.gxcj.entity.query.CompanyJobQuery query = new com.gxcj.entity.query.CompanyJobQuery();
        query.setPageNum(1);
        query.setPageSize(1000); // 获取所有职位
        query.setStatus(1); // 只显示在招的
        query.setAudit(1);  // 只显示已审核通过的

        PageResult<JobEntity> pageResult = jobService.getCompanyJobList(query, UserContext.getUserId());

        List<Map<String, String>> options = pageResult.getData().stream()
                .map(job -> Map.of("id", job.getId(), "jobName", job.getJobName()))
                .collect(Collectors.toList());

        return Result.success(options);
    }

    // ==================== 请求对象 ====================

    @Data
    public static class InterviewArrangeReq {
        @NotBlank(message = "投递记录ID不能为空")
        private String deliveryId;

        @NotBlank(message = "面试时间不能为空")
        private String interviewTime;

        @jakarta.validation.constraints.Min(value = 15, message = "面试时长最少15分钟")
        @jakarta.validation.constraints.Max(value = 240, message = "面试时长最多240分钟")
        private Integer duration;

        @NotBlank(message = "面试方式不能为空")
        private String type;  // ONSITE/VIDEO/PHONE

        @NotBlank(message = "面试地点不能为空")
        private String location;

        private String notes;
    }

    @Data
    public static class RejectReq {
        @NotBlank(message = "投递记录ID不能为空")
        private String deliveryId;

        private String reason;
    }


    /**
     * 6. 获取面试列表（分页）
     */
    @GetMapping("/interview/list")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<PageResult<com.gxcj.entity.vo.InterviewVo>> getInterviewList(com.gxcj.entity.query.InterviewQuery query) {
        PageResult<com.gxcj.entity.vo.InterviewVo> pageResult = deliveryService.getInterviewList(query, UserContext.getUserId());
        return Result.success(pageResult);
    }

    /**
     * 7. 完成面试
     */
    @PostMapping("/interview/complete")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> completeInterview(@RequestBody @Valid CompleteInterviewReq req) {
        deliveryService.completeInterview(req, UserContext.getUserId());
        return Result.success();
    }

    /**
     * 8. 取消面试
     */
    @PostMapping("/interview/cancel")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> cancelInterview(@RequestBody @Valid CancelInterviewReq req) {
        deliveryService.cancelInterview(req.getInterviewId(), req.getReason(), UserContext.getUserId());
        return Result.success();
    }

    /**
     * 9. 评价面试
     */
    @PostMapping("/interview/evaluate")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> evaluateInterview(@RequestBody @Valid EvaluateInterviewReq req) {
        deliveryService.evaluateInterview(req, UserContext.getUserId());
        return Result.success();
    }

    /**
     * 10. 获取人才库列表（分页）
     */
    @GetMapping("/talent/list")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<PageResult<DeliveryVo>> getTalentList(com.gxcj.entity.query.TalentQuery query) {
        PageResult<DeliveryVo> pageResult = deliveryService.getTalentList(query, UserContext.getUserId());
        return Result.success(pageResult);
    }

    /**
     * 11. 获取人才库统计
     */
    @GetMapping("/talent/statistics")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Map<String, Long>> getTalentStatistics() {
        Map<String, Long> statistics = deliveryService.getTalentStatistics(UserContext.getUserId());
        return Result.success(statistics);
    }

    /**
     * 12. 发放Offer
     */
    @PostMapping("/offer/send")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> sendOffer(@RequestBody @Valid SendOfferReq req) {
        deliveryService.sendOffer(req, UserContext.getUserId());
        return Result.success();
    }

    // ==================== 新增请求对象 ====================

    @Data
    public static class CompleteInterviewReq {
        @NotBlank(message = "面试ID不能为空")
        private String interviewId;

        @NotBlank(message = "面试结果不能为空")
        private String result;  // PASS/FAIL

        @jakarta.validation.constraints.Min(value = 1, message = "评分最小为1")
        @jakarta.validation.constraints.Max(value = 5, message = "评分最大为5")
        private Integer score;

        @NotBlank(message = "面试评价不能为空")
        private String comment;
    }

    @Data
    public static class CancelInterviewReq {
        @NotBlank(message = "面试ID不能为空")
        private String interviewId;

        @NotBlank(message = "取消原因不能为空")
        private String reason;
    }

    @Data
    public static class EvaluateInterviewReq {
        @NotBlank(message = "面试ID不能为空")
        private String interviewId;

        @jakarta.validation.constraints.Min(value = 1, message = "评分最小为1")
        @jakarta.validation.constraints.Max(value = 5, message = "评分最大为5")
        private Integer score;

        @NotBlank(message = "面试评价不能为空")
        private String comment;
    }

    @Data
    public static class SendOfferReq {
        @NotBlank(message = "投递记录ID不能为空")
        private String deliveryId;

        @NotBlank(message = "入职时间不能为空")
        private String entryDate;

        @NotBlank(message = "薪资待遇不能为空")
        private String salary;

        private String notes;
    }
}
