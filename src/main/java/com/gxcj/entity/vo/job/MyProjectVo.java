package com.gxcj.entity.vo.job;

import lombok.Data;

@Data
public class MyProjectVo {
    private String id;
    private String name;
    private String logo;
    private String domain;      // 领域

    // --- 管理端特有字段 ---
    private Integer status;     // 0:审核中, 1:孵化中, 2:已驳回, 3:已完结
    private String statusText;  // "审核中"
    private String auditReason; // "项目计划书过于简单，请补充..."

    private String createTime;
    private Integer teamSize;   // 团队人数
}
