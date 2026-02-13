package com.gxcj.entity.vo.school;

import lombok.Data;

import java.util.List;

@Data
public class SchoolEntrepreneurshipStatsVo {
    private SchoolEntrepreneurshipKpiVo kpi;
    private List<String> collegeList;
    private List<SchoolEntrepCollegeStatsVo> collegeStats;
    private List<SchoolEntrepStatusDistributionVo> statusDistribution;
    private List<SchoolEntrepDomainDistributionVo> domainDistribution;
    private List<SchoolEntrepTeamSizeDistributionVo> teamSizeDistribution;
    private List<SchoolEntrepMonthlyTrendVo> monthlyTrend;
    private SchoolEntrepSuccessStatsVo successStats;
}
