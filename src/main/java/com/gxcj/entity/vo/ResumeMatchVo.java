package com.gxcj.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeMatchVo {
    /**
     * 简历ID
     */
    private String resumeId;
    
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
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
     * 期望职位
     */
    private String expectedPosition;
    
    /**
     * 期望薪资
     */
    private String expectedSalary;
    
    /**
     * 期望城市
     */
    private String targetCity;
    
    /**
     * 技能
     */
    private String skills;
    
    /**
     * 匹配度分数 (0-100)
     */
    private Integer matchScore;
    
    /**
     * 匹配理由
     */
    private String matchReason;
    
    /**
     * 联系电话（脱敏）
     */
    private String phone;
    
    /**
     * 邮箱（脱敏）
     */
    private String email;
}
