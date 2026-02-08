package com.gxcj.entity.vo.job;

import lombok.Data;

@Data
public class StudentDeliveryVo {
    private String id;           // 投递记录ID
    private String jobId;        // 职位ID (点击跳详情)
    private String jobName;
    private String salary;       // 薪资范围

    private String companyId;
    private String companyName;
    private String companyLogo;

    private String status;       // 状态 (DELIVERED, INTERVIEW...)
    private String statusText;   // 状态中文 (前端也可以转，后端转更方便)
    private String createTime;   // 投递时间
}
