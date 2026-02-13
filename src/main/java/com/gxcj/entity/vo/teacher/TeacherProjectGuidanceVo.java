package com.gxcj.entity.vo.teacher;

import lombok.Data;

@Data
public class TeacherProjectGuidanceVo {
    private String id;
    private String projectId;
    private String teacherId;
    private String teacherName;
    private String teacherAvatar;
    private String content;
    private String createTime;
}
