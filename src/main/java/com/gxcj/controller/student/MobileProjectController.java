package com.gxcj.controller.student;

import com.gxcj.entity.dto.ProjectStageSaveDto;
import com.gxcj.entity.dto.StageStatusUpdateDto;
import com.gxcj.entity.vo.ProjectStageVo;
import com.gxcj.entity.vo.StageGuidanceVo;
import com.gxcj.entity.vo.job.MyJoinedProjectVo;
import com.gxcj.entity.vo.job.MyProjectVo;
import com.gxcj.entity.vo.job.ProjectDetailVo;
import com.gxcj.entity.vo.teacher.TeacherListVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.DictService;
import com.gxcj.service.ProjectApplicationService;
import com.gxcj.service.ProjectService;
import com.gxcj.service.ProjectStageService;
import com.gxcj.service.TeacherService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private DictService dictService;
    @Autowired
    private ProjectApplicationService projectApplicationService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private ProjectStageService projectStageService;

    @GetMapping("/project/detail/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<ProjectDetailVo> getProjectDetail(@PathVariable("id") String projectId){
        ProjectDetailVo projectDetailVo = projectService.getProjectDetail(projectId);
        return Result.success(projectDetailVo);
    }

    @GetMapping("/project/my/list")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<MyProjectVo>> getMyProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<MyProjectVo> pageResult = projectService.getMyProjectList(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/my/joined-projects")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<MyJoinedProjectVo>> getMyJoinedProjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {

        PageResult<MyJoinedProjectVo> pageResult = projectApplicationService.getMyJoinedProjects(pageNum, pageSize, status);
        return Result.success(pageResult);
    }

    @GetMapping("/project/getDomain")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Map<String, String>> getDomain(){
        Map<String, String> domainList = dictService.getDomain();
        return Result.success(domainList);
    }

    @PostMapping("/project/save")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Map<String, String>> save(@RequestBody ProjectForm projectForm){
        String projectId = projectService.save(projectForm);
        Map<String, String> result = new java.util.HashMap<>();
        result.put("projectId", projectId);
        return Result.success(result);
    }

    @PostMapping("/project/delete/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<String> delete(@PathVariable("id") String projectId){
        projectService.delete(projectId);
        return Result.success();
    }

    @GetMapping("/teacher/list")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<TeacherListVo>> getTeacherList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String expertise,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<TeacherListVo> result = teacherService.getTeacherList(keyword, expertise, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/project/stages/save")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> saveProjectStages(@RequestBody ProjectStageSaveDto dto) {
        projectStageService.saveStages(dto);
        return Result.success();
    }

    @GetMapping("/project/{projectId}/stages")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<List<ProjectStageVo>> getProjectStages(@PathVariable String projectId) {
        List<ProjectStageVo> stages = projectStageService.getProjectStages(projectId, false);
        return Result.success(stages);
    }

    @PutMapping("/project/stage/{stageId}/status")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> updateStageStatus(@PathVariable String stageId, @RequestBody StageStatusUpdateDto dto) {
        projectStageService.updateStageStatus(stageId, dto);
        return Result.success();
    }

    @GetMapping("/project/stage/{stageId}/guidance")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<List<StageGuidanceVo>> getStageGuidance(@PathVariable String stageId) {
        List<StageGuidanceVo> guidanceList = projectStageService.getStageGuidance(stageId);
        return Result.success(guidanceList);
    }

    @Data
    public static class ProjectForm{
        private String id;
        private String title;
        private String logo;
        private String domain;
        private String slogan;
        private String mentorName;
        private String mentorId;
        private String teamSize;
        private String description;
        private String needs;
    }

}
