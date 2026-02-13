package com.gxcj.entity.query.school;

import lombok.Data;

@Data
public class SchoolTeacherQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String name;
    private String employeeNo;
    private String collegeName;
    private String title;
    private String schoolId;
}
