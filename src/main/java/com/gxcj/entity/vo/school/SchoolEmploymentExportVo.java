package com.gxcj.entity.vo.school;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class SchoolEmploymentExportVo {
    @ExcelProperty(value = "学号", index = 0)
    @ColumnWidth(15)
    private String studentNo;
    
    @ExcelProperty(value = "姓名", index = 1)
    @ColumnWidth(15)
    private String studentName;
    
    @ExcelProperty(value = "学院", index = 2)
    @ColumnWidth(20)
    private String collegeName;
    
    @ExcelProperty(value = "专业", index = 3)
    @ColumnWidth(20)
    private String majorName;
    
    @ExcelProperty(value = "毕业年份", index = 4)
    @ColumnWidth(12)
    private Integer graduationYear;
    
    @ExcelProperty(value = "就业状态", index = 5)
    @ColumnWidth(12)
    private String employmentStatusText;
    
    @ExcelProperty(value = "就业单位", index = 6)
    @ColumnWidth(25)
    private String companyName;
    
    @ExcelProperty(value = "就业岗位", index = 7)
    @ColumnWidth(20)
    private String position;
    
    @ExcelProperty(value = "薪资范围", index = 8)
    @ColumnWidth(15)
    private String salary;
    
    @ExcelProperty(value = "工作地点", index = 9)
    @ColumnWidth(15)
    private String workLocation;
    
    @ExcelProperty(value = "就业日期", index = 10)
    @ColumnWidth(15)
    private String employmentDate;
    
    @ExcelProperty(value = "备注", index = 11)
    @ColumnWidth(30)
    private String remark;
}
