package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolEmploymentStatsKpiVo {
    private Integer totalGraduates;
    private Integer employedCount;
    private String employmentRate;
    private Integer entrepreneurshipCount;
    private Integer incubatingCount;
    private String avgSalary;
}
