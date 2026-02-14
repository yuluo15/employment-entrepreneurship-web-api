package com.gxcj.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobRecommendationVo {
    private String jobId;
    private String title;
    private String companyName;
    private String companyLogo;
    private String salaryRange;
    private String location;
    private String tags;
    private Integer matchScore;  // 匹配度（0-100）
    private String recommendReason;  // 推荐理由
}
