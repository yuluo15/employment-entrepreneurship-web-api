package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gxcj.controller.admin.SchoolMgrController;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@TableName("sys_school")
public class SchoolEntity {
    @TableId
    private String id;
    private String name;
    private String code;
    private String contactPhone;
    private String address;
    private Integer status;
    private String logo;
    private String createBy;
    private Timestamp updateTime;
    private Timestamp createTime;
    private Integer isDeleted;
    private String defaultAccountId;
    @TableField(exist = false)
    private String email;

    public static <T> SchoolEntity ofRep(T rep) {
        if (rep == null) {
            return null;
        }
        SchoolEntity schoolEntity = new SchoolEntity();
        BeanUtils.copyProperties(rep, schoolEntity);
        return schoolEntity;
    }
}
