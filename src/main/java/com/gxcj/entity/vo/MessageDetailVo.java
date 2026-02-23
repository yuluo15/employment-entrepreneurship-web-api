package com.gxcj.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageDetailVo extends MessageVo {
    private String positionId;
    private String applicationId;
}
