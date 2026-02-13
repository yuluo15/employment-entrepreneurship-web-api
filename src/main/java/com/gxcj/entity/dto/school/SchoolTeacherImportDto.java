package com.gxcj.entity.dto.school;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SchoolTeacherImportDto {
    @ExcelProperty("工号")
    private String employeeNo;
    
    @ExcelProperty("姓名")
    private String name;
    
    @ExcelProperty("性别")
    private String gender;
    
    @ExcelProperty("学院")
    private String collegeName;
    
    @ExcelProperty("职称")
    private String title;
    
    @ExcelProperty("专业领域")
    private String expertise;
    
    @ExcelProperty("联系电话")
    private String phone;
    
    @ExcelProperty("邮箱")
    private String email;
    
    @ExcelProperty("个人简介")
    private String intro;
}
