package com.gxcj.controller.student;

import com.gxcj.entity.vo.job.MyProjectVo;
import com.gxcj.entity.vo.job.ProjectDetailVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.DictService;
import com.gxcj.service.ProjectService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/project")
public class MobileProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private DictService dictService;

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<ProjectDetailVo> getProjectDetail(@PathVariable("id") String projectId){
        ProjectDetailVo projectDetailVo = projectService.getProjectDetail(projectId);
        return Result.success(projectDetailVo);
    }

    @GetMapping("/my/list")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<MyProjectVo>> getMyProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResult<MyProjectVo> pageResult = projectService.getMyProjectList(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/getDomain")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Map<String, String>> getDomain(){
        Map<String, String> domainList = dictService.getDomain();
        return Result.success(domainList);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<String> save(@RequestBody ProjectForm projectForm){
        projectService.save(projectForm);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<String> delete(@PathVariable("id") String projectId){
        projectService.delete(projectId);
        return Result.success();
    }


    @Data
    public static class ProjectForm{
        private String id;
        private String title;
        private String logo;
        private String domain;
        private String slogan;
        private String mentorName;
        private String teamSize;
        private String description;
        private String needs;
    }

}
