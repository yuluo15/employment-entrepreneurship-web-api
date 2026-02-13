package com.gxcj.entity.query;

import lombok.Data;

@Data
public class TeacherGuidanceQuery {
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
}
