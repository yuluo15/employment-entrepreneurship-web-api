package com.gxcj.entity.vo;

import com.gxcj.entity.StudentResumeEntity;
import lombok.Data;

@Data
public class StudentResumeVo extends StudentResumeEntity {
    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 性别 (1:男, 2:女)
     */
    private Integer gender;

    /**
     * 联系电话
     */
    private String studentPhone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 最高学历所在学校 (如果在教育经历中已有，通常也会在顶层冗余一个用于快速展示)
     */
    private String school;

    /**
     * 所学专业
     */
    private String major;

    /**
     * 学历标识 (例如: "college", "bachelor", "master", "doctor"，前端用来匹配字典)
     */
    private String education;

    /**
     * 毕业年份 (例如: "2026")
     */
    private String graduationYear;
}
