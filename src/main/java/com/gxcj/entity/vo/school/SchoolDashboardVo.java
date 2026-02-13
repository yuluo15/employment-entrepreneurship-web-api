package com.gxcj.entity.vo.school;

import lombok.Data;

import java.util.List;

@Data
public class SchoolDashboardVo {
    private String schoolName;
    private Integer studentCount;
    private Integer teacherCount;
    private String employmentRate;
    private Integer incubatingProjectCount;
    private List<SchoolPendingProjectVo> pendingProjects;
    private List<SchoolLatestNoticeVo> latestNotices;
    private SchoolEmploymentTrendVo employmentTrend;
    private List<SchoolProjectStatusVo> projectStatus;
    private List<SchoolCollegeEmploymentVo> collegeStats;
}
