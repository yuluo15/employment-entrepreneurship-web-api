package com.gxcj.entity.vo.school;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SchoolTeacherVo {
    private String teacherId;
    private String userId;
    private String schoolId;
    private String name;
    private String employeeNo;
    private String gender;
    private String collegeName;
    private String title;
    private String expertise;
    private String intro;
    private Integer guidanceCount;
    private BigDecimal ratingScore;
    private String phone;
    private String email;
    private String createTime;
    private String updateTime;
}
