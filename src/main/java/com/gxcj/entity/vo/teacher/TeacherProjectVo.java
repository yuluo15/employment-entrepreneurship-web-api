package com.gxcj.entity.vo.teacher;

import lombok.Data;

@Data
public class TeacherProjectVo {
    private String projectId;
    private String projectName;
    private String logo;
    private String studentId;
    private String studentName;
    private String schoolId;
    private String schoolName;
    private String collegeName;
    private String domain;
    private Integer teamSize;
    private String status;
    private Integer guidanceCount;
    private String createTime;
}
