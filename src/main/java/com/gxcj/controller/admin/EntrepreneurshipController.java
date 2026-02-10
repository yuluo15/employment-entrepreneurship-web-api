package com.gxcj.controller.admin;

import com.gxcj.entity.vo.EntrepProjectVo;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.EntrepreneurshipService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 创业项目库管理接口
 */
@RestController
@RequestMapping("/api/entrep")
public class EntrepreneurshipController {

    @Autowired
    private EntrepreneurshipService entrepreneurshipService;

    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<PageResult<EntrepProjectVo>> getProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String schoolId,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String status) {
        
        PageResult<EntrepProjectVo> pageResult = entrepreneurshipService.getProjectList(
                pageNum, pageSize, projectName, schoolId, domain, status);
        return Result.success(pageResult);
    }

    /**
     * 审核项目
     */
    @PostMapping("/audit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<Void> auditProject(@RequestBody @Valid AuditRequest req) {
        entrepreneurshipService.auditProject(req.getProjectId(), req.getStatus(), req.getReason());
        return Result.success();
    }

    /**
     * 项目落地
     */
    @PostMapping("/complete/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<Void> completeProject(@PathVariable String projectId) {
        entrepreneurshipService.completeProject(projectId);
        return Result.success();
    }

    /**
     * 获取学校列表
     */
    @GetMapping("/schoolList")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<List<SchoolNameVo>> getSchoolList() {
        List<SchoolNameVo> schoolList = entrepreneurshipService.getSchoolList();
        return Result.success(schoolList);
    }

    /**
     * 获取项目领域字典
     */
    @GetMapping("/domains")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<Map<String, String>> getDomains() {
        Map<String, String> domains = entrepreneurshipService.getDomains();
        return Result.success(domains);
    }

    /**
     * 审核请求参数
     */
    @Data
    public static class AuditRequest {
        @NotBlank(message = "项目ID不能为空")
        private String projectId;
        
        @NotNull(message = "审核状态不能为空")
        private Integer status;  // 1=通过, 2=驳回
        
        private String reason;   // 驳回原因(status=2时必填)
    }
}
