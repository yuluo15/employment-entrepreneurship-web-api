package com.gxcj.controller.teacher;

import com.gxcj.entity.vo.teacher.TeacherProfileVo;
import com.gxcj.result.Result;
import com.gxcj.service.TeacherProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
public class TeacherProfileController {

    @Autowired
    private TeacherProfileService teacherProfileService;

    @GetMapping("/info")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<TeacherProfileVo> getTeacherInfo() {
        TeacherProfileVo info = teacherProfileService.getTeacherInfo();
        return Result.success(info);
    }
}
