package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolEmploymentVo {
    private String studentId;
    private String studentName;
    private String studentNo;
    private String collegeName;
    private String majorName;
    private Integer graduationYear;
    private String employmentStatus;
    
    // 已就业字段（从关联表获取）
    private String companyName;
    private String position;
    private String salary;
    private String employmentDate;
    private String workLocation;
    
    // 通用字段
    private String remark;
    private String updateTime;
}
