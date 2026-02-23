package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 系统消息通知表
 */
@Data
@TableName("sys_message")
public class MessageEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId
    private String id;

    /**
     * 接收人ID (学生ID 或 HR ID)
     */
    private String receiverId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 消息类型 (1:系统通知, 2:面试通知, 3:offer通知)
     */
    private Integer type;

    /**
     * 阅读状态 (0:未读, 1:已读)
     */
    private Integer isRead;

    /**
     * 关联业务ID (如投递ID、面试ID，用于点击跳转)
     */
    private String refId;

    /**
     * 创建时间
     */
    private Timestamp createTime;
}