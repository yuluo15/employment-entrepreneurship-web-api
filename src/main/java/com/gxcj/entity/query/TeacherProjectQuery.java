package com.gxcj.entity.query;

import lombok.Data;

@Data
public class TeacherProjectQuery {
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
    private String scope;
    private String status;
    private String domain;
}
