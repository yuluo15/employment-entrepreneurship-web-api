package com.gxcj.entity.query;

import lombok.Data;

@Data
public class TeacherQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String schoolId;
    private String name;
    private String collegeName;
    private String title;
}
