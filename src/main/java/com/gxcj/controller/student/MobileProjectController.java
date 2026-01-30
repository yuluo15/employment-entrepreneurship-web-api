package com.gxcj.controller.student;

import com.gxcj.entity.vo.job.ProjectDetailVo;
import com.gxcj.result.Result;
import com.gxcj.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/project")
public class MobileProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<ProjectDetailVo> getProjectDetail(@PathVariable("id") String projectId){
        ProjectDetailVo projectDetailVo = projectService.getProjectDetail(projectId);
        return Result.success(projectDetailVo);
    }

}
