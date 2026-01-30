package com.gxcj.entity.query;

import lombok.Data;

@Data
public class StudentQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String schoolId;
    private String studentName;
    private String majorName;
    private Integer graduationYear;
    private String employmentStatus;
}
