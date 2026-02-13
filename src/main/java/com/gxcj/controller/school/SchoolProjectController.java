package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.dto.school.SchoolProjectAuditDto;
import com.gxcj.entity.query.school.SchoolProjectQuery;
import com.gxcj.entity.vo.school.SchoolProjectDetailVo;
import com.gxcj.entity.vo.school.SchoolProjectVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school/project")
@PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
public class SchoolProjectController {

    @Autowired
    private SchoolProjectService projectService;

    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    public Result<PageResult<SchoolProjectVo>> getProjectList(SchoolProjectQuery query) {
        String userId = UserContext.getUserId();
        PageResult<SchoolProjectVo> result = projectService.getProjectList(query, userId);
        return Result.success(result);
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/detail/{projectId}")
    public Result<SchoolProjectDetailVo> getProjectDetail(@PathVariable String projectId) {
        String userId = UserContext.getUserId();
        SchoolProjectDetailVo detail = projectService.getProjectDetail(projectId, userId);
        return Result.success(detail);
    }

    /**
     * 审核项目
     */
    @PutMapping("/audit")
    public Result<Void> auditProject(@RequestBody SchoolProjectAuditDto dto) {
        String userId = UserContext.getUserId();
        projectService.auditProject(dto, userId);
        return Result.success();
    }
}
