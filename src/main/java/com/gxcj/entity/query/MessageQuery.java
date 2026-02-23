package com.gxcj.entity.query;

import lombok.Data;

/**
 * 消息查询参数
 */
@Data
public class MessageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Integer type;      // 消息类型：1=系统通知, 2=面试通知, 3=Offer通知
    private Integer isRead;    // 阅读状态：0=未读, 1=已读
    private String receiverId; // 接收人ID
}
