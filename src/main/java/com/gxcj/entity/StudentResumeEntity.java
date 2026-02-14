package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.gxcj.handle.ListToVectorTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@TableName(value = "biz_student_resume", autoResultMap = true)
public class StudentResumeEntity implements Serializable {

    @TableId
    private String resumeId;
    private String studentId;
    // ... 基础字段 ...
    private String expectedPosition;
    private String expectedSalary;
    private String targetCity;
    private Integer jobType;
    private String arrivalTime;
    private String skills;
    private String personalSummary;

    /**
     * 教育经历
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<EducationItem> educationHistory;

    /**
     * 实习经历
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<InternshipItem> internshipExp;

    /**
     * 项目经历
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ProjectItem> projectExp;

    /**
     * 证书/奖项
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CertificateItem> certificates;

    // ... 其他字段 ...
    private Integer isPublic;
    private Integer resumeScore;
    private Integer viewCount;
    private Timestamp createTime;
    private Timestamp updateTime;
    
    // AI向量字段
    @TableField(typeHandler = ListToVectorTypeHandler.class) // 指定处理器
    private float[] embedding;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationItem implements Serializable {
        private String school;       // 学校
        private String major;        // 专业
        private String degree;       // 学历 (本科/硕士)
        private String startDate;    // 开始时间 (2020-09)
        private String endDate;      // 结束时间 (2024-06)
        private String description;  // 主修课程/描述 (可选)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipItem implements Serializable {
        private String company;      // 公司名
        private String role;         // 职位 (Java开发)
        private String startDate;
        private String endDate;
        private String description;  // 工作内容
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectItem implements Serializable {
        private String projectName;  // 项目名
        private String role;         // 担任角色 (队长/核心开发)
        private String startDate;
        private String endDate;
        private String description;  // 项目描述
        private String projectLink;  // 项目链接 (可选)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateItem implements Serializable {
        private String name;         // 证书名 (CET-6)
        private String date;         // 获得时间
        private String issuer;       // 颁发机构 (可选)
    }
}