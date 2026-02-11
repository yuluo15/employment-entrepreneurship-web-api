package com.gxcj.service;

import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.query.NoticeQuery;
import com.gxcj.result.PageResult;

public interface NoticeService {
    
    /**
     * 分页查询公告列表
     */
    PageResult<NoticeEntity> list(NoticeQuery query);
    
    /**
     * 新增公告
     */
    void add(NoticeEntity notice);
    
    /**
     * 更新公告
     */
    void update(NoticeEntity notice);
    
    /**
     * 删除公告
     */
    void delete(String noticeId);
    
    /**
     * 发布公告
     */
    void publish(String noticeId);
    
    /**
     * 停用公告
     */
    void unpublish(String noticeId);
    
    /**
     * 设置置顶状态
     */
    void setTop(String noticeId, Integer isTop);
}
