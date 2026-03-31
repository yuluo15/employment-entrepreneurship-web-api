package com.gxcj.controller.teacher;

import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.query.TeacherProjectQuery;
import com.gxcj.entity.vo.ProjectStageVo;
import com.gxcj.entity.vo.StageGuidanceVo;
import com.gxcj.entity.vo.teacher.TeacherProjectDetailVo;
import com.gxcj.entity.vo.teacher.TeacherProjectGuidanceVo;
import com.gxcj.entity.vo.teacher.TeacherProjectVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.ProjectStageService;
import com.gxcj.service.TeacherProjectService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherProjectController {

    @Autowired
    private TeacherProjectService teacherProjectService;

    @Autowired
    private ProjectStageService projectStageService;

    @GetMapping("/projects/domain")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<List<DictDomainVo>> getProjectDomain() {
        List<DictDomainVo> dictDataEntity = teacherProjectService.getProjectDomain();
        return Result.success(dictDataEntity);
    }

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

    @GetMapping("/project/{projectId}/stages")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<List<ProjectStageVo>> getProjectStages(@PathVariable String projectId) {
        List<ProjectStageVo> stages = projectStageService.getProjectStages(projectId, true);
        return Result.success(stages);
    }

    @GetMapping("/project/stage/{stageId}/guidance")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<List<StageGuidanceVo>> getStageGuidance(@PathVariable String stageId) {
        List<StageGuidanceVo> guidanceList = projectStageService.getStageGuidance(stageId);
        return Result.success(guidanceList);
    }

    @Data
    public static class DictDomainVo{
        private String id;
        private String dictValue;
        private String dictLabel;
    }
}
