package com.gxcj.service;

import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.query.NoticeQuery;
import com.gxcj.result.PageResult;

public interface SchoolNoticeService {
    
    /**
     * 获取学校通知列表（分页）
     * 包含管理员发布的和本校发布的公告
     * 
     * @param query 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<NoticeEntity> list(NoticeQuery query, String userId);
    
    /**
     * 新增学校通知
     * 
     * @param notice 通知实体
     * @param userId 用户ID
     */
    void add(NoticeEntity notice, String userId);
    
    /**
     * 更新学校通知
     * 
     * @param notice 通知实体
     * @param userId 用户ID
     */
    void update(NoticeEntity notice, String userId);
    
    /**
     * 删除学校通知
     * 
     * @param noticeId 通知ID
     * @param userId 用户ID
     */
    void delete(String noticeId, String userId);
    
    /**
     * 发布学校通知
     * 
     * @param noticeId 通知ID
     * @param userId 用户ID
     */
    void publish(String noticeId, String userId);
    
    /**
     * 停用学校通知
     * 
     * @param noticeId 通知ID
     * @param userId 用户ID
     */
    void unpublish(String noticeId, String userId);
}
