package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_project_application")
public class ProjectApplicationEntity {
    @TableId
    private String id;
    private String projectId;
    private String applicantId;
    private String applicantName;
    private String applicantAvatar;
    private String applicantSchool;
    private String applicantMajor;
    private String applicationReason;
    private String skills;
    private String status;
    private String replyMessage;
    private Timestamp replyTime;
    private Timestamp createTime;
    private Timestamp updateTime;
}
