package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.RoleEntity;
import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.query.NoticeQuery;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.NoticeMapper;
import com.gxcj.mapper.RoleMapper;
import com.gxcj.mapper.TeacherMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolNoticeService;
import com.gxcj.utils.EntityHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchoolNoticeServiceImpl implements SchoolNoticeService {

    @Autowired
    private NoticeMapper noticeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public PageResult<NoticeEntity> list(NoticeQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<NoticeEntity>()
                .and(w -> w
                        // 管理员发布的已发布公告
                        .or(w1 -> w1.eq(NoticeEntity::getPublisherType, "admin")
                                    .eq(NoticeEntity::getStatus, 1))
                        // 或本校发布的所有公告（包括草稿）
                        .or(w2 -> w2.eq(NoticeEntity::getPublisherType, "school")
                                    .eq(NoticeEntity::getPublisherId, schoolId))
                )
                // 标题筛选
                .like(StringUtils.isNotEmpty(query.getNoticeTitle()), 
                      NoticeEntity::getNoticeTitle, query.getNoticeTitle())
                // 类型筛选
                .eq(StringUtils.isNotEmpty(query.getNoticeType()), 
                    NoticeEntity::getNoticeType, query.getNoticeType());
        
        // 来源筛选
        if (StringUtils.isNotEmpty(query.getSource())) {
            if ("admin".equals(query.getSource())) {
                wrapper.eq(NoticeEntity::getPublisherType, "admin");
            } else if ("school".equals(query.getSource())) {
                wrapper.eq(NoticeEntity::getPublisherType, "school")
                       .eq(NoticeEntity::getPublisherId, schoolId);
            }
        }
        
        // 排序规则：置顶优先 > 发布时间 > 创建时间
        wrapper.orderByDesc(NoticeEntity::getIsTop)
               .orderByDesc(NoticeEntity::getPublishTime)
               .orderByDesc(NoticeEntity::getCreateTime);
        
        // 分页查询
        Page<NoticeEntity> page = noticeMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public void add(NoticeEntity notice, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 生成ID
        notice.setNoticeId(EntityHelper.uuid());
        
        // 设置发布者信息
        notice.setPublisherType("school");
        notice.setPublisherId(schoolId);
        notice.setCreateBy(userId);
        
        // 设置时间
        notice.setCreateTime(EntityHelper.now());
        notice.setUpdateTime(EntityHelper.now());
        
        // 初始化阅读量
        notice.setViewCount(0);
        
        // 学校发布的公告不支持置顶
        notice.setIsTop(0);
        
        // 学校发布的公告固定为 student（本校学生和教师都能看）
        notice.setTargetAudience("student");
        
        // 如果是直接发布，设置发布时间
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            notice.setPublishTime(EntityHelper.now());
        }
        
        // 设置默认值
        if (notice.getStatus() == null) {
            notice.setStatus(0); // 默认草稿
        }
        
        noticeMapper.insert(notice);
    }

    @Override
    public void update(NoticeEntity notice, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 验证公告是否存在且有权限
        NoticeEntity existNotice = noticeMapper.selectById(notice.getNoticeId());
        if (existNotice == null) {
            throw new BusinessException("公告不存在");
        }
        
        // 权限校验：只能更新本校发布的公告
        if (!"school".equals(existNotice.getPublisherType()) 
            || !schoolId.equals(existNotice.getPublisherId())) {
            throw new BusinessException("无权操作此公告");
        }
        
        // 更新时间
        notice.setUpdateTime(EntityHelper.now());
        
        // 学校发布的公告不支持置顶
        notice.setIsTop(0);
        
        // 学校发布的公告固定为 student
        notice.setTargetAudience("student");
        
        // 如果从草稿改为发布，且之前没有发布时间，则设置发布时间
        if (notice.getStatus() != null && notice.getStatus() == 1 
            && existNotice.getPublishTime() == null) {
            notice.setPublishTime(EntityHelper.now());
        }
        
        noticeMapper.updateById(notice);
    }

    @Override
    public void delete(String noticeId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 验证公告是否存在且有权限
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        // 权限校验：只能删除本校发布的公告
        if (!"school".equals(notice.getPublisherType()) 
            || !schoolId.equals(notice.getPublisherId())) {
            throw new BusinessException("无权操作此公告");
        }
        
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public void publish(String noticeId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 验证公告是否存在且有权限
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        // 权限校验：只能发布本校的公告
        if (!"school".equals(notice.getPublisherType()) 
            || !schoolId.equals(notice.getPublisherId())) {
            throw new BusinessException("无权操作此公告");
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
    public void unpublish(String noticeId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 验证公告是否存在且有权限
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        
        // 权限校验：只能停用本校的公告
        if (!"school".equals(notice.getPublisherType()) 
            || !schoolId.equals(notice.getPublisherId())) {
            throw new BusinessException("无权操作此公告");
        }
        
        if (notice.getStatus() == 0) {
            throw new BusinessException("公告已是草稿状态");
        }
        
        notice.setStatus(0);
        notice.setUpdateTime(EntityHelper.now());
        // 注意：停用时不清空 publishTime，保留首次发布时间
        
        noticeMapper.updateById(notice);
    }

    // ==================== 私有方法 ====================

    /**
     * 根据用户ID获取学校ID
     */
    private String getSchoolIdByUserId(String userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        RoleEntity roleEntity = roleMapper.selectById(user.getRoleKey());
        
        if ("ROLE_SCHOOL".equals(roleEntity.getRoleName()) || "ROLE_SCHOOL_ADMIN".equals(roleEntity.getRoleName())) {
            return user.getOwnerId();
        }
        
        if ("ROLE_TEACHER".equals(roleEntity.getRoleName())) {
            TeacherEntity teacher = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>()
                    .eq(TeacherEntity::getUserId, userId));
            if (teacher != null) {
                return teacher.getSchoolId();
            }
        }
        
        throw new BusinessException("无权访问通知公告");
    }
}
