package com.gxcj.entity.vo.job;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class JobDetailVo {
    // --- 职位本职信息 ---
    private String id;
    private String title;          // jobName
    private String salaryRange;         // salaryRange
    private List<String> tags;     // 将 String "双休,五险" 转为 List
    private String city;
    private String experience;     // 比如 "1-3年" (建议数据库加这个字段或复用tags)
    private String education;
    private String description;
    private String address;        // 工作地点，通常取公司的 address 或单独字段
    private Timestamp createTime;     // 格式化后的时间

    // --- 关联公司信息 ---
    private String companyId;
    private String companyName;
    private String companyLogo;
    private String companyScale;
    private String companyIndustry;

    // --- 关联HR信息 ---
    private String hrId;
    private String hrName;
    private String hrTitle;        // position
    private String hrAvatar;

    // ---用户交互状态 ---
    private Boolean isCollected;   // 是否已收藏
    private Boolean isApplied;     // 是否已投递 (或者返回 String status 显示具体进度)
}
