package com.gxcj.entity.vo.teacher;

import lombok.Data;

@Data
public class TeacherProfileVo {
    private String teacherId;
    private String userId;
    private String name;
    private String avatar;
    private String employeeNo;
    private String schoolId;
    private String schoolName;
    private String collegeName;
    private String title;
    private String expertise;
    private String phone;
    private String email;
    private Integer guidanceCount;
    private Integer projectCount;
    private Double ratingScore;
    private Integer status;
    private String createTime;
}
