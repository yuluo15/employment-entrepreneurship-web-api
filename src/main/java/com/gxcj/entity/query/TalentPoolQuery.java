package com.gxcj.entity.query;

import lombok.Data;

@Data
public class TalentPoolQuery {
    /**
     * 页码
     */
    private Integer pageNum;
    
    /**
     * 每页条数
     */
    private Integer pageSize;
    
    /**
     * 学生姓名（模糊查询）
     */
    private String studentName;
    
    /**
     * 状态：OFFER=已录用/REJECTED=已拒绝
     */
    private String status;
    
    /**
     * 职位ID
     */
    private String jobId;
}
