package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolMajorStatsVo {
    private String majorName;
    private Integer totalCount;
    private Integer employedCount;
    private Double employmentRate;
}
