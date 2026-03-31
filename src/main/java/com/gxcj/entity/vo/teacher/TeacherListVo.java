package com.gxcj.entity.vo.teacher;

import lombok.Data;

@Data
public class TeacherListVo {
    private String teacherId;
    private String teacherName;
    private String teacherNo;
    private String avatar;
    private String title;
    private String expertise;
    private String expertiseCode;
    private String schoolName;
    private String collegeName;
    private String intro;
    private Integer guidanceCount;
}
