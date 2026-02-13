package com.gxcj.entity.vo.teacher;

import lombok.Data;

@Data
public class TeacherPendingProjectVo {
    private String projectId;
    private String projectName;
    private String logo;
    private String studentId;
    private String studentName;
    private String domain;
    private String createTime;
}
