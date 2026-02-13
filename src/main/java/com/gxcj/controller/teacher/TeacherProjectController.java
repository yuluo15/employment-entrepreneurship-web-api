package com.gxcj.controller.teacher;

import com.gxcj.entity.query.TeacherProjectQuery;
import com.gxcj.entity.vo.teacher.TeacherProjectDetailVo;
import com.gxcj.entity.vo.teacher.TeacherProjectGuidanceVo;
import com.gxcj.entity.vo.teacher.TeacherProjectVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.TeacherProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherProjectController {

    @Autowired
    private TeacherProjectService teacherProjectService;

    @GetMapping("/projects")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<PageResult<TeacherProjectVo>> getProjects(TeacherProjectQuery query) {
        PageResult<TeacherProjectVo> result = teacherProjectService.getProjects(query);
        return Result.success(result);
    }

    @GetMapping("/project/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<TeacherProjectDetailVo> getProjectDetail(@PathVariable String id) {
        TeacherProjectDetailVo detail = teacherProjectService.getProjectDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/project/{id}/guidance")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<List<TeacherProjectGuidanceVo>> getProjectGuidanceList(@PathVariable String id) {
        List<TeacherProjectGuidanceVo> list = teacherProjectService.getProjectGuidanceList(id);
        return Result.success(list);
    }
}
