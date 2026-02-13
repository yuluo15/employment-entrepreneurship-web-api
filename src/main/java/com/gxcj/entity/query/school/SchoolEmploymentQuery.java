package com.gxcj.entity.query.school;

import lombok.Data;

@Data
public class SchoolEmploymentQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String studentName;
    private String studentNo;
    private String employmentStatus;
    private Integer graduationYear;
    private String schoolId;
}
