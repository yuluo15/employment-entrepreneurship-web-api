package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class ProjectApplicantVo {
    private String id;
    private String applicantId;
    private String applicantName;
    private String applicantAvatar;
    private String applicantSchool;
    private String applicantMajor;
    private String applicationReason;
    private String skills;
    private String status;
    private String createTime;
}
