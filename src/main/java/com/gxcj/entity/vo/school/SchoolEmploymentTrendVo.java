package com.gxcj.entity.vo.school;

import lombok.Data;

import java.util.List;

@Data
public class SchoolEmploymentTrendVo {
    private List<String> months;
    private List<Double> rates;
}
