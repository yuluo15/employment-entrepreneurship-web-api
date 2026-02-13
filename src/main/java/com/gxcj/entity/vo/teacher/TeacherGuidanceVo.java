package com.gxcj.entity.vo.teacher;

import lombok.Data;

@Data
public class TeacherGuidanceVo {
    private String id;
    private String projectId;
    private String projectName;
    private String projectLogo;
    private String studentId;
    private String studentName;
    private String domain;
    private String teacherId;
    private String teacherName;
    private String teacherAvatar;
    private String content;
    private Double rating;
    private String createTime;
}
