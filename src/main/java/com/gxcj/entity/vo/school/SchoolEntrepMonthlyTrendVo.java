package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolEntrepMonthlyTrendVo {
    private String month;
    private Integer newCount;
    private Integer approvedCount;
}
