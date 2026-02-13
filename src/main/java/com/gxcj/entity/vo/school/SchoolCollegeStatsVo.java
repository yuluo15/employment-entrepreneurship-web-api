package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolCollegeStatsVo {
    private String collegeName;
    private Integer totalCount;
    private Integer employedCount;
    private Double employmentRate;
}
