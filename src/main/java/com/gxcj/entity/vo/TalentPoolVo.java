package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class TalentPoolVo {
    /**
     * 投递记录ID
     */
    private String id;
    
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 学生电话
     */
    private String studentPhone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 职位ID
     */
    private String jobId;
    
    /**
     * 职位名称
     */
    private String jobName;
    
    /**
     * 简历ID
     */
    private String resumeId;
    
    /**
     * 学校
     */
    private String school;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 学历
     */
    private String education;
    
    /**
     * 毕业年份
     */
    private Integer graduationYear;
    
    /**
     * 状态：OFFER=已录用/REJECTED=已拒绝
     */
    private String status;
    
    /**
     * 更新时间
     */
    private String updateTime;
    
    /**
     * 性别
     */
    private String gender;
}
