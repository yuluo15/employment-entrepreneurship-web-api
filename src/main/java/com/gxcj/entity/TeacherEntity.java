package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_teacher")
public class TeacherEntity {
    @TableId
    private String teacherId;
    private String userId;
    private String schoolId;
    private String name;
    private String employeeNo;
    private String gender;
    private String collegeName;
    private String title;
    private String expertise;
    private String intro;
    private Integer guidanceCount;
    private Integer ratingScore;
    private String phone;
    private String email;
    private Timestamp createTime;
    private Timestamp updateTime;
}
