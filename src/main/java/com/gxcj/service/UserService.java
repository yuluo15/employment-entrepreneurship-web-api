package com.gxcj.service;

import com.gxcj.controller.AuthController;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.vo.UserVo;
import org.apache.ibatis.annotations.Param;

public interface UserService {
    UserVo login(AuthController.UserReq userReq);

    void changePassword(AuthController.PasswordReq passwordReq);

    /**
     * 获取当前用户信息
     */
    UserEntity getUserInfo(String userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(UserEntity userEntity);
}
