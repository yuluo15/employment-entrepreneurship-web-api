package com.gxcj.entity.query.school;

import lombok.Data;

/**
 * 学校端学生查询参数
 */
@Data
public class SchoolStudentQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String studentName;
    private String studentNo;
    private String collegeName;
    private String majorName;
    private Integer enrollmentYear;
    private String employmentStatus;
}
