package com.gxcj.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 企业工作台数据VO
 */
@Data
public class CompanyDashboardVo {
    
    /**
     * 企业名称
     */
    private String companyName;
    
    /**
     * 企业LOGO
     */
    private String companyLogo;
    
    /**
     * HR姓名
     */
    private String hrName;
    
    /**
     * 在招职位数
     */
    private Integer activeJobCount;
    
    /**
     * 待处理简历数
     */
    private Integer pendingResumeCount;
    
    /**
     * 今日面试数
     */
    private Integer todayInterviewCount;
    
    /**
     * 本月简历数
     */
    private Integer monthResumeCount;
    
    /**
     * 待处理简历列表（最新5条）
     */
    private List<PendingResumeItem> pendingResumes;
    
    /**
     * 今日面试列表
     */
    private List<TodayInterviewItem> todayInterviews;
    
    /**
     * 简历投递趋势（近7天）
     */
    private ResumeTrend resumeTrend;
    
    /**
     * 职位投递排行TOP5
     */
    private List<JobRankItem> jobRank;
    
    /**
     * 待处理简历项
     */
    @Data
    public static class PendingResumeItem {
        private String id;
        private String studentName;
        private String studentAvatar;
        private String jobName;
        private String createTime;
    }
    
    /**
     * 今日面试项
     */
    @Data
    public static class TodayInterviewItem {
        private String id;
        private String studentName;
        private String jobName;
        private String interviewTime;
        private Integer type;  // 1=线下, 2=视频, 3=电话
    }
    
    /**
     * 简历趋势
     */
    @Data
    public static class ResumeTrend {
        private List<String> dates;   // 日期数组 MM-DD
        private List<Integer> counts; // 对应日期的简历数量
    }
    
    /**
     * 职位排行项
     */
    @Data
    public static class JobRankItem {
        private String jobName;
        private Integer count;
    }
}
