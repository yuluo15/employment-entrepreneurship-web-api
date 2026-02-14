package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class TalentStatisticsVo {
    /**
     * 已录用数量
     */
    private Integer offerCount;
    
    /**
     * 已拒绝数量
     */
    private Integer rejectedCount;
    
    /**
     * 总计
     */
    private Integer totalCount;
}
