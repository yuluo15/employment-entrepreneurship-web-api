package com.gxcj.entity.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ExcelIgnoreUnannotated
public class TeacherExportVo {
    @ExcelProperty(value = "工号", index = 0)
    @ColumnWidth(15)
    private String employeeNo;
    @ExcelProperty(value = "姓名", index = 1)
    @ColumnWidth(15)
    private String name;
    @ExcelProperty(value = "所属学校", index = 2)
    @ColumnWidth(15)
    private String schoolName;
    @ExcelProperty(value = "所属院系", index = 3)
    @ColumnWidth(15)
    private String collegeName;
    @ExcelProperty(value = "职称", index = 4)
    @ColumnWidth(15)
    private String title;
    @ExcelProperty(value = "擅长领域", index = 5)
    @ColumnWidth(20)
    private String expertise;
    @ExcelProperty(value = "累计指导项目数", index = 6)
    @ColumnWidth(25)
    private Integer guidanceCount;
    @ExcelProperty(value = "手机", index = 7)
    @ColumnWidth(15)
    private String phone;
    @ExcelProperty(value = "邮箱", index = 8)
    @ColumnWidth(25)
    private String email;
}
