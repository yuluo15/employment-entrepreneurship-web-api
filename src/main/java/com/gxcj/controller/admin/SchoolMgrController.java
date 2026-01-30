package com.gxcj.controller.admin;

import com.gxcj.entity.SchoolEntity;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schoolMgr")
public class SchoolMgrController {

    @Autowired
    private SchoolService schoolService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<SchoolEntity>> list(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                         @RequestParam(required = false) String name, @RequestParam(required = false) Integer status) {
        PageResult<SchoolEntity> list = schoolService.list(pageNum, pageSize, name, status);
        return Result.success(list);
    }

    @PostMapping("/createSchool")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> createSchool(@RequestBody @Valid SchoolCreateReq rep){
        schoolService.createSchool(rep);
        return Result.success();
    }

    @PostMapping("/updateSchool")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> updateSchool(@RequestBody @Valid SchoolUpdateReq req){
        schoolService.updateSchool(req);
        return Result.success();
    }

    @PostMapping("/resetPassword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> resetPassword(@RequestBody @Valid IdReq req){
        schoolService.resetPassword(req);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> deleteSchool(@PathVariable String id){
        schoolService.deleteSchool(id);
        return Result.success();
    }

    @PostMapping("/updateStatus")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> updateStatus(@RequestBody @Valid StatusUpdateReq req){
        schoolService.updateStatus(req);
        return Result.success();
    }

    @GetMapping("/deletedList")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<SchoolEntity>> deletedList(){
        PageResult<SchoolEntity> list = schoolService.deleteList();
        return Result.success(list);
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> restoreSchool(@PathVariable String id){
        schoolService.restoreSchool(id);
        return Result.success();
    }

    @Data
    public static class SchoolCreateReq {
        @NotBlank(message = "学校名称不能为空")
        private String name;
        @NotBlank(message = "院校代码不能为空")
        private String code;
        @NotBlank(message = "邮箱不能为空")
        private String email;
        @NotBlank(message = "手机不能为空")
        private String contactPhone;
        private String logo;
        private String address;
    }

    @Data
    public static class SchoolUpdateReq {
        @NotNull(message = "ID不能为空")
        private String id;

        private String name;
        private String logo;
        private String contactPhone;
        private String address;
    }

    @Data
    public static class IdReq {
        @NotNull(message = "ID不能为空")
        private String id;
        @NotBlank(message = "密码不能为空")
        private String newPassword;
    }

    @Data
    public static class StatusUpdateReq {
        @NotNull
        private String id;
        @NotNull
        private Integer status;
    }
}
