package com.gxcj.entity.query;

import lombok.Data;

@Data
public class CompanyJobQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String jobName;    // 职位名称（模糊查询）
    private Integer audit;     // 审核状态：0=待审核, 1=已通过, 2=已驳回
    private Integer status;    // 在招状态：0=已下架, 1=在招
}
