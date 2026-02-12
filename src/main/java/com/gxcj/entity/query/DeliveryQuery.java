package com.gxcj.entity.query;

import lombok.Data;

@Data
public class DeliveryQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String jobId;          // 职位ID
    private String studentName;    // 学生姓名（模糊查询）
    private String startDate;      // 投递开始日期 YYYY-MM-DD
    private String endDate;        // 投递结束日期 YYYY-MM-DD
    private String status;         // 投递状态
}
