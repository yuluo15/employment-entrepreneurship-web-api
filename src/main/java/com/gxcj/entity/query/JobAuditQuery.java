package com.gxcj.entity.query;

import lombok.Data;

@Data
public class JobAuditQuery {
    private Integer pageNum;
    private Integer pageSize;
    private String jobName;
    private String companyName;
    private Integer audit;
}
