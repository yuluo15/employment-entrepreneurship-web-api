package com.gxcj.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页概览数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeOverviewVo {
    
    // 核心指标
    private Integer schoolCount;        // 入驻学校总数
    private Integer companyCount;       // 入驻企业总数
    private Integer studentCount;       // 学生总数
    private Integer teacherCount;       // 教师总数
    private Integer pendingJobs;        // 待审核岗位数量
    private Integer pendingProjects;    // 待审核创业项目数量
    
    // 学校排行
    private List<SchoolRankItem> schoolRank;
    
    // 创业领域分布
    private List<DomainDistributionItem> domainDistribution;
    
    // 最新入驻企业
    private List<LatestCompanyItem> latestCompanies;
    
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
        private Integer studentCount;
    }
    
    /**
     * 创业领域分布项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainDistributionItem {
        private String domain;
        private String domainLabel;
        private Integer count;
    }
    
    /**
     * 最新入驻企业项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestCompanyItem {
        private String companyId;
        private String companyName;
        private String industry;
        private Integer jobCount;
        private String createTime;
    }
}
