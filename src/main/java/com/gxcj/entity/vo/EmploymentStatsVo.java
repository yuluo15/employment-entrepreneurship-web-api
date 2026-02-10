package com.gxcj.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 就业数据统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentStatsVo {
    
    // 1. KPI指标
    private KpiData kpi;
    
    // 2. 各校就业率排行 (TOP 10)
    private List<SchoolRankItem> schoolRank;
    
    // 3. 就业状态分布
    private List<StatusDistributionItem> employmentStatus;
    
    // 4. 薪资分布
    private List<SalaryDistributionItem> salaryDistribution;
    
    // 5. 热门行业 TOP5
    private List<IndustryItem> hotIndustries;
    
    // 6. 创业项目统计
    private EntrepreneurshipData entrepreneurship;
    
    /**
     * KPI指标数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiData {
        private Integer totalGraduates;           // 毕业生总数
        private Integer employedCount;            // 已就业人数
        private String employmentRate;            // 就业率 "88.5%"
        private Integer entrepreneurshipCount;    // 创业人数
        private String entrepreneurshipRate;      // 创业率 "3.2%"
        private String avgSalary;                 // 平均期望薪资 "6500"
    }
    
    /**
     * 学校排行项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchoolRankItem {
        private String schoolId;
        private String schoolName;
        private Integer totalStudents;
        private Integer employedStudents;
        private Double employmentRate;  // 数字类型，如 88.5
    }
    
    /**
     * 就业状态分布项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistributionItem {
        private String statusName;  // "待就业", "已签约", "升学", "灵活就业", "创业"
        private Integer count;
        private String percentage;  // "45.2%"
    }
    
    /**
     * 薪资分布项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryDistributionItem {
        private String range;  // "3k以下", "3k-5k", "5k-8k", "8k以上"
        private Integer count;
    }
    
    /**
     * 行业项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndustryItem {
        private String industry;
        private Integer count;
    }
    
    /**
     * 创业数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntrepreneurshipData {
        private Integer totalProjects;        // 创业项目总数
        private Integer approvedProjects;     // 已通过项目数
        private Integer pendingProjects;      // 待审核项目数
        private List<DomainDistributionItem> domainDistribution;  // 创业领域分布
    }
    
    /**
     * 领域分布项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainDistributionItem {
        private String domain;  // "互联网+", "文化创意", "现代农业"等
        private Integer count;
    }
}
