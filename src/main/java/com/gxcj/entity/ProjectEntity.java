package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_project")
public class ProjectEntity {
    @TableId(value = "project_id")
    private String projectId;
    private String userId;
    private String SchoolId;
    private String projectName;
    private String logo;
    private String domain;
    private String description;
    private Integer teamSize;
    private Integer jobsCreated;
    private String mentorId;
    private String mentorName;
    private String mentorComment;
    private String status;
    private String auditReason;
    private Timestamp auditTime;
    private Timestamp createTime;
    private Timestamp updateTime;
    //需求描述
    private String needs;
    //项目标语
    private String slogan;
    @TableField(exist = false)
    private String schoolName;
    @TableField(exist = false)
    private String leaderName;
}
