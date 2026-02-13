package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolEntrepSuccessStatsVo {
    private String approvalRate;
    private Integer approvedCount;
    private Integer submittedCount;
    private String landingRate;
    private Integer landedCount;
    private Double avgTeamSize;
}
