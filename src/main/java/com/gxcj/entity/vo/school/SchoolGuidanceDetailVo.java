package com.gxcj.entity.vo.school;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolGuidanceDetailVo extends SchoolGuidanceVo {
    private String studentNo;
    private String domain;
    private Integer teamSize;
}
