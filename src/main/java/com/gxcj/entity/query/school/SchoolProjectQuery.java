package com.gxcj.entity.query.school;

import lombok.Data;

@Data
public class SchoolProjectQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String projectName;
    private String studentName;
    private String domain;
    private String status;
    private String schoolId;
}
