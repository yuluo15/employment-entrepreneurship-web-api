package com.gxcj.entity.vo.teacher;

import lombok.Data;

import java.util.List;

@Data
public class TeacherProjectDetailVo {
    private String projectId;
    private String projectName;
    private String logo;
    private String description;
    private String studentId;
    private String studentName;
    private String studentAvatar;
    private String studentPhone;
    private String schoolId;
    private String schoolName;
    private String collegeName;
    private String domain;
    private Integer teamSize;
    private String status;
    private List<String> tags;
    private String createTime;
    private Boolean isMentor; // 当前登录教师是否是该项目的指导教师
}
