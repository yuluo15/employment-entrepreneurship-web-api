package com.gxcj.controller.student;

import com.gxcj.entity.dto.ProjectApplyDto;
import com.gxcj.entity.dto.ProjectApplicationHandleDto;
import com.gxcj.entity.vo.ProjectApplicantVo;
import com.gxcj.entity.vo.ProjectApplicationVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.ProjectApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/project")
public class ProjectApplicationController {

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Map<String, String>> applyProject(@RequestBody ProjectApplyDto dto) {
        Map<String, String> result = projectApplicationService.applyProject(dto);
        return Result.success(result);
    }

    @PutMapping("/apply/{applicationId}/cancel")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> cancelApplication(@PathVariable String applicationId) {
        projectApplicationService.cancelApplication(applicationId);
        return Result.success();
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<ProjectApplicationVo>> getMyApplications(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String status) {
        PageResult<ProjectApplicationVo> result = projectApplicationService.getMyApplications(pageNum, pageSize, status);
        return Result.success(result);
    }

    @GetMapping("/{projectId}/applications")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<ProjectApplicantVo>> getProjectApplications(
            @PathVariable String projectId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String status) {
        PageResult<ProjectApplicantVo> result = projectApplicationService.getProjectApplications(projectId, pageNum, pageSize, status);
        return Result.success(result);
    }

    @PutMapping("/apply/{applicationId}/handle")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> handleApplication(
            @PathVariable String applicationId,
            @RequestBody ProjectApplicationHandleDto dto) {
        projectApplicationService.handleApplication(applicationId, dto);
        return Result.success();
    }
}
