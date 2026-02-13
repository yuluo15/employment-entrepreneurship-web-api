package com.gxcj.entity.vo.school;

import lombok.Data;

/**
 * 学校端学生信息VO
 */
@Data
public class SchoolStudentVo {
    private String studentId;
    private String userId;
    private String schoolId;
    private String studentName;
    private String studentNo;
    private String gender;
    private String collegeName;
    private String majorName;
    private String className;
    private String education;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String phone;
    private String email;
    private String employmentStatus;
    private String createTime;
    private String updateTime;
}
