package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class JobAuditVo {
    private String jobId;
    private String jobName;
    private String salaryRange;
    private String salaryRangeLabel;
    private String companyId;
    private String companyName;
    private String companyLogo;
    private String companyIndustry;
    private String city;
    private String location;
    private String education;
    private String educationLabel;
    private String experience;
    private String tags;
    private String description;
    private String requirement;
    private Integer audit;
    private Integer status;
    private Integer viewCount;
    private String hrId;
    private String hrName;
    private String contactPhone;
    private String reason;
    private String createTime;
}
