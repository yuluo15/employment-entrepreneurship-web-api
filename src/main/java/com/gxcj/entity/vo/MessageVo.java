package com.gxcj.entity.vo;

import lombok.Data;

/**
 * 消息列表VO
 */
@Data
public class MessageVo {
    private String id;
    private String title;
    private String content;
    private Integer type;
    private Integer isRead;
    private String refId;
    private String createTime;
    
    // 扩展字段
    private String companyName;
    private String positionName;
}
