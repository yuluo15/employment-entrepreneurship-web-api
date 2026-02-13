package com.gxcj.entity.vo.school;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学校端学生简历VO
 */
@Data
public class SchoolStudentResumeVo {
    private String resumeId;
    private String studentId;
    private String expectedPosition;
    private String expectedSalary;
    private String targetCity;
    private Integer jobType;
    private String arrivalTime;
    private String personalSummary;
    private String skills;
    private List<EducationItem> educationHistory;
    private List<InternshipItem> internshipExp;
    private List<ProjectItem> projectExp;
    private List<CertificateItem> certificates;
    private Integer isPublic;
    private Integer resumeScore;
    private Integer viewCount;
    private String createTime;
    private String updateTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationItem {
        private String school;
        private String major;
        private String degree;
        private String startDate;
        private String endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipItem {
        private String company;
        private String position;
        private String startDate;
        private String endDate;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectItem {
        private String name;
        private String role;
        private String startDate;
        private String endDate;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateItem {
        private String name;
        private String issuer;
        private String issueDate;
    }
}
