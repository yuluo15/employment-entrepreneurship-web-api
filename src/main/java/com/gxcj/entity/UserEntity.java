package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gxcj.controller.AuthController;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@TableName("sys_user")
public class UserEntity {
    private String id;
    private String loginIdentity;
    private String email;
    private String phone;
    private String password;
    private String nickname;
    private String realName;
    private String avatar;
    private Integer gender;
    private String roleKey;
    private String ownerId;
    private Integer status;
    private Integer isDeleted;
    private String loginIp;
    private Timestamp loginDate;
    private String createBy;
    private Timestamp createTime;
    private Timestamp updateTime;

    @TableField(exist = false)
    private String roleName;



    public static UserEntity ofReq(AuthController.UserReq userReq) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(userReq,userEntity);
        return userEntity;
    }
}
