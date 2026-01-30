package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_student_resume")
public class StudentResumeEntity {
    @TableId
    private String resumeId;
    private String studentId;
    private String expectedPosition;
    private String expectedSalary;
    private String targetCity;
    private Integer jobType;
    private String skills;
    private String personalSummary;
    private String projectExperience;
    private String internshipExperience;
    private Integer isPublic;
    private Integer resumeScore;
    private Timestamp updateTime;
}
