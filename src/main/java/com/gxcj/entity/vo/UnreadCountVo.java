package com.gxcj.entity.vo;

import lombok.Data;

/**
 * 未读消息数量VO
 */
@Data
public class UnreadCountVo {
    private Integer total;
    private Integer systemCount;
    private Integer interviewCount;
    private Integer offerCount;
}
