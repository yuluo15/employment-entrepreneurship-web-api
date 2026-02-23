package com.gxcj.service;

import com.gxcj.entity.query.MessageQuery;
import com.gxcj.entity.vo.MessageDetailVo;
import com.gxcj.entity.vo.MessageVo;
import com.gxcj.entity.vo.UnreadCountVo;
import com.gxcj.result.PageResult;

/**
 * 消息通知服务接口
 */
public interface MessageService {
    
    /**
     * 获取消息列表（分页）
     */
    PageResult<MessageVo> getMessages(MessageQuery query);
    
    /**
     * 获取消息详情
     */
    MessageDetailVo getMessageDetail(String id, String studentId);
    
    /**
     * 获取未读消息数量
     */
    UnreadCountVo getUnreadCount(String studentId);
    
    /**
     * 标记消息为已读
     */
    int markAsRead(String id, String studentId);
    
    /**
     * 批量标记已读
     */
    int markAllAsRead(String studentId, Integer type);
    
    /**
     * 删除消息
     */
    int deleteMessage(String id, String studentId);
}
