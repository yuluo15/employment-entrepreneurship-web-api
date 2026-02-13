package com.gxcj.entity.vo.school;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 学生导出VO
 */
@Data
public class SchoolStudentExportVo {
    @ExcelProperty("学号")
    private String studentNo;
    
    @ExcelProperty("姓名")
    private String studentName;
    
    @ExcelProperty("性别")
    private String genderText;
    
    @ExcelProperty("学院")
    private String collegeName;
    
    @ExcelProperty("专业")
    private String majorName;
    
    @ExcelProperty("班级")
    private String className;
    
    @ExcelProperty("学历")
    private String education;
    
    @ExcelProperty("入学年份")
    private Integer enrollmentYear;
    
    @ExcelProperty("毕业年份")
    private Integer graduationYear;
    
    @ExcelProperty("就业状态")
    private String employmentStatusText;
    
    @ExcelProperty("联系电话")
    private String phone;
    
    @ExcelProperty("邮箱")
    private String email;
}
