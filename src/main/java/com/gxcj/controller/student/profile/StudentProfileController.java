package com.gxcj.controller.student.profile;

import com.gxcj.context.UserContext;
import com.gxcj.entity.vo.job.MyProfileVo;
import com.gxcj.entity.vo.job.StudentProfileVo;
import com.gxcj.result.Result;
import com.gxcj.service.StudentService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    // 1. 获取个人详细信息 (回显)
    @GetMapping("/detail")
    public Result<MyProfileVo> getProfileDetail() {
        // 这里的 service 方法内部需要关联查询 biz_student 和 sys_school
        MyProfileVo vo = studentService.getStudentProfile(UserContext.getUserId());
        return Result.success(vo);
    }

    // 2. 更新个人信息
    @PostMapping("/update")
    public Result<Void> updateProfile(@RequestBody @Validated ProfileUpdateReq req) {

        studentService.updateStudentProfile(UserContext.getUserId(), req);
        return Result.success();
    }


    @Data
    public static class ProfileUpdateReq {
        private String avatar;       // 头像
        private Integer gender;      // 性别

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Email(message = "邮箱格式不正确")
        private String email;

        private String graduationYear;
    }

}
