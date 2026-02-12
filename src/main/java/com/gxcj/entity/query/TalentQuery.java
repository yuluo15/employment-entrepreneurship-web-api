package com.gxcj.entity.query;

import lombok.Data;

@Data
public class TalentQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String studentName;    // 学生姓名（模糊查询）
    private String status;         // 状态：OFFER=已录用/REJECTED=已拒绝
    private String jobId;          // 职位ID
}
