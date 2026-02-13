package com.gxcj.entity.query.school;

import lombok.Data;

@Data
public class SchoolGuidanceQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String teacherName;
    private String projectName;
    private String startTime;
    private String endTime;
    private String schoolId;
}
