package com.gxcj.controller.student.profile;

import com.gxcj.entity.vo.job.StudentProfileVo;
import com.gxcj.result.Result;
import com.gxcj.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/student/profile")
public class StudentProfileController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<StudentProfileVo> summary(){
        StudentProfileVo studentProfileVo = studentService.getProfileSummary();
        return Result.success(studentProfileVo);
    }

}
