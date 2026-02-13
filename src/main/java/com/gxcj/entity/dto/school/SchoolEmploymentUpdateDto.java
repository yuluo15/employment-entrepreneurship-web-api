package com.gxcj.entity.dto.school;

import lombok.Data;

@Data
public class SchoolEmploymentUpdateDto {
    private String studentId;
    private String employmentStatus;
    
    // 已就业字段
    private String companyName;
    private String position;
    private String salary;
    private String employmentDate;
    private String workLocation;
    
    // 通用字段
    private String remark;
}
