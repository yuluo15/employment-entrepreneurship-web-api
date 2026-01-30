package com.gxcj.entity.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ExcelIgnoreUnannotated
public class StudentExportVo {
    @ExcelProperty(value = "学号", index = 0)
    @ColumnWidth(15)
    private String studentNo;
    @ExcelProperty(value = "姓名", index = 1)
    @ColumnWidth(15)
    private String studentName;
    @ExcelProperty(value = "学校", index = 2)
    @ColumnWidth(15)
    private String schoolName;
    @ExcelProperty(value = "院系", index = 3)
    @ColumnWidth(20)
    private String collegeName;
    @ExcelProperty(value = "专业", index = 4)
    @ColumnWidth(15)
    private String majorName;
    @ExcelProperty(value = "班级", index = 5)
    @ColumnWidth(15)
    private String className;
    @ExcelProperty(value = "学历", index = 6)
    @ColumnWidth(15)
    private String education;
    @ExcelProperty(value = "届别", index = 7)
    @ColumnWidth(15)
    private Integer graduationYear;
    @ExcelProperty(value = "就业状态", index = 8)
    @ColumnWidth(15)
    private String employmentStatus;
    @ExcelProperty(value = "电话", index = 9)
    @ColumnWidth(15)
    private String phone;

}
