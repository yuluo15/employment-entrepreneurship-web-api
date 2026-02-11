package com.gxcj.controller.admin;

import com.gxcj.entity.query.JobAuditQuery;
import com.gxcj.entity.vo.JobAuditVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.JobAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位审核管理接口
 */
@RestController
@RequestMapping("/api/jobAudit")
@Validated
public class JobAuditController {

    @Autowired
    private JobAuditService jobAuditService;

    /**
     * 获取岗位列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<PageResult<JobAuditVo>> getJobAuditList(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer audit) {
        
        JobAuditQuery query = new JobAuditQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setJobName(jobName);
        query.setCompanyName(companyName);
        query.setAudit(audit);
        
        PageResult<JobAuditVo> result = jobAuditService.getJobAuditList(query);
        return Result.success(result);
    }

    /**
     * 审核岗位
     */
    @PostMapping("/audit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> auditJob(@RequestBody @Valid AuditReq req) {
        jobAuditService.auditJob(req.getJobId(), req.getAudit(), req.getReason());
        return Result.success();
    }

    /**
     * 下架岗位
     */
    @PostMapping("/offline/{jobId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> offlineJob(@PathVariable String jobId) {
        jobAuditService.offlineJob(jobId);
        return Result.success();
    }

    @Data
    public static class AuditReq {
        @NotBlank(message = "岗位ID不能为空")
        private String jobId;
        
        @NotNull(message = "审核状态不能为空")
        private Integer audit;
        
        private String reason;
    }
}
