package com.gxcj.controller;

import com.gxcj.context.UserContext;
import com.gxcj.entity.UserEntity;
import com.gxcj.exception.BusinessException;
import com.gxcj.result.Result;
import com.gxcj.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息管理接口
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 1. 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserEntity> getUserInfo() {
        String userId = UserContext.getUserId();
        UserEntity userEntity = userService.getUserInfo(userId);
        return Result.success(userEntity);
    }

    /**
     * 2. 更新用户信息
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody @Valid UserUpdateReq req) {
        String currentUserId = UserContext.getUserId();
        
        // 权限验证：用户只能修改自己的信息
        if (!currentUserId.equals(req.getId())) {
            throw new BusinessException("无权限修改其他用户信息");
        }
        
        // 构建更新实体
        UserEntity userEntity = new UserEntity();
        userEntity.setId(req.getId());
        userEntity.setEmail(req.getEmail());
        userEntity.setPhone(req.getPhone());
        userEntity.setNickname(req.getNickname());
        userEntity.setRealName(req.getRealName());
        userEntity.setAvatar(req.getAvatar());
        userEntity.setGender(req.getGender());
        
        userService.updateUserInfo(userEntity);
        return Result.success();
    }

    // ==================== 请求对象 ====================

    @Data
    public static class UserUpdateReq {
        @NotBlank(message = "用户ID不能为空")
        private String id;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Size(max = 50, message = "昵称最多50个字符")
        private String nickname;

        @Size(max = 50, message = "真实姓名最多50个字符")
        private String realName;

        private String avatar;

        private Integer gender;
    }
}
