package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.query.NoticeQuery;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.NoticeMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.NoticeService;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public PageResult<NoticeEntity> list(NoticeQuery query) {
        Page<NoticeEntity> page = noticeMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<NoticeEntity>()
                        .like(StringUtils.isNotEmpty(query.getNoticeTitle()), 
                              NoticeEntity::getNoticeTitle, query.getNoticeTitle())
                        .eq(StringUtils.isNotEmpty(query.getNoticeType()), 
                            NoticeEntity::getNoticeType, query.getNoticeType())
                        .eq(query.getStatus() != null, 
                            NoticeEntity::getStatus, query.getStatus())
                        // 排序规则：置顶优先 > 发布时间 > 创建时间
                        .orderByDesc(NoticeEntity::getIsTop)
                        .orderByDesc(NoticeEntity::getPublishTime)
                        .orderByDesc(NoticeEntity::getCreateTime)
        );
        
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public void add(NoticeEntity notice) {
        // 生成ID
        notice.setNoticeId(EntityHelper.uuid());
        
        // 设置发布者信息（管理员发布）
        notice.setPublisherType("admin");
        notice.setPublisherId(null);
        
        // 设置创建信息
        notice.setCreateBy(UserContext.getUserId());
        notice.setCreateTime(EntityHelper.now());
        notice.setUpdateTime(EntityHelper.now());
        
        // 初始化阅读量
        notice.setViewCount(0);
        
        // 如果是直接发布，设置发布时间
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            notice.setPublishTime(EntityHelper.now());
        }
        
        // 设置默认值
        if (notice.getStatus() == null) {
            notice.setStatus(0); // 默认草稿
        }
        if (notice.getIsTop() == null) {
            notice.setIsTop(0); // 默认不置顶
        }
        
        noticeMapper.insert(notice);
    }

    @Override
    public void update(NoticeEntity notice) {
        // 验证公告是否存在
        NoticeEntity existNotice = noticeMapper.selectById(notice.getNoticeId());
        if (existNotice == null) {
            throw new BusinessException("公告不存在");
        }
        
        // 更新时间
        notice.setUpdateTime(EntityHelper.now());
        
        // 如果从草稿改为发布，且之前没有发布时间，则设置发布时间
        if (notice.getStatus() != null && notice.getStatus() == 1 
            && existNotice.getPublishTime() == null) {
            notice.setPublishTime(EntityHelper.now());
        }
        
        noticeMapper.updateById(notice);
    }

    @Override
    public void delete(String noticeId) {
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public void publish(String noticeId) {
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        if (notice.getStatus() == 1) {
            throw new BusinessException("公告已发布，无需重复操作");
        }
        
        notice.setStatus(1);
        notice.setUpdateTime(EntityHelper.now());
        
        // 首次发布时设置发布时间
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(EntityHelper.now());
        }
        
        noticeMapper.updateById(notice);
    }

    @Override
    public void unpublish(String noticeId) {
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        if (notice.getStatus() == 0) {
            throw new BusinessException("公告已是草稿状态");
        }
        
        notice.setStatus(0);
        notice.setUpdateTime(EntityHelper.now());
        // 注意：停用时不清空 publishTime，保留首次发布时间
        
        noticeMapper.updateById(notice);
    }

    @Override
    public void setTop(String noticeId, Integer isTop) {
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new BusinessException("置顶状态参数错误");
        }
        
        notice.setIsTop(isTop);
        notice.setUpdateTime(EntityHelper.now());
        
        noticeMapper.updateById(notice);
    }
}
