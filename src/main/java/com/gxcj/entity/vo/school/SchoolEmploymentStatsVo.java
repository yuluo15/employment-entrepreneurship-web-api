package com.gxcj.entity.vo.school;

import lombok.Data;

import java.util.List;

@Data
public class SchoolEmploymentStatsVo {
    private SchoolEmploymentStatsKpiVo kpi;
    private List<String> collegeList;
    private List<SchoolCollegeStatsVo> collegeStats;
    private List<SchoolMajorStatsVo> majorStats;
    private List<SchoolCompanyStatsVo> topCompanies;
    private List<SchoolSalaryDistributionVo> salaryDistribution;
    private List<SchoolIndustryDistributionVo> industryDistribution;
    private List<SchoolMonthlyTrendVo> monthlyTrend;
    private Integer totalCount;
    private Integer employedCount;
    private Double employmentRate;
}
