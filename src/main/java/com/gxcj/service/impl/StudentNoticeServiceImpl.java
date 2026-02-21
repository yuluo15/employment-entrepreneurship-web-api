package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.vo.StudentNoticeDetailVo;
import com.gxcj.entity.vo.StudentNoticeVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.NoticeMapper;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.StudentNoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentNoticeServiceImpl implements StudentNoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SchoolMapper schoolMapper;

    @Override
    public List<StudentNoticeVo> getHomeNotices() {
        String userId = UserContext.getUserId();

        // 查询学生信息获取学校ID
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        String studentSchoolId = null;
        if (student != null) {
            studentSchoolId = student.getSchoolId();
        }

        // 查询通知列表
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeEntity::getStatus, 1); // 已发布

        // 权限控制：学生可见的通知
        final String finalSchoolId = studentSchoolId;
        wrapper.and(w -> w
                // 管理员发布的面向学生的公告
                .or(w1 -> w1.eq(NoticeEntity::getPublisherType, "admin")
                           .in(NoticeEntity::getTargetAudience, "all", "student"))
                // 本校发布的公告
                .or(finalSchoolId != null, w2 -> w2.eq(NoticeEntity::getPublisherType, "school")
                                                    .eq(NoticeEntity::getPublisherId, finalSchoolId))
        );

        wrapper.orderByDesc(NoticeEntity::getIsTop);
        wrapper.orderByDesc(NoticeEntity::getPublishTime);
        wrapper.last("LIMIT 5");

        List<NoticeEntity> noticeList = noticeMapper.selectList(wrapper);

        // 批量查询学校信息
        List<String> schoolIds = noticeList.stream()
                .filter(n -> "school".equals(n.getPublisherType()) && StringUtils.hasText(n.getPublisherId()))
                .map(NoticeEntity::getPublisherId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> schoolNameMap = null;
        if (!schoolIds.isEmpty()) {
            List<SchoolEntity> schools = schoolMapper.selectBatchIds(schoolIds);
            schoolNameMap = schools.stream()
                    .collect(Collectors.toMap(SchoolEntity::getId, SchoolEntity::getName));
        }

        // 转换为VO
        List<StudentNoticeVo> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Map<String, String> finalSchoolNameMap = schoolNameMap;

        for (NoticeEntity notice : noticeList) {
            StudentNoticeVo vo = new StudentNoticeVo();
            vo.setNoticeId(notice.getNoticeId());
            vo.setNoticeTitle(notice.getNoticeTitle());
            vo.setNoticeType(notice.getNoticeType());
            vo.setNoticeTypeText(getNoticeTypeText(notice.getNoticeType()));
            vo.setPublishTime(notice.getPublishTime() != null ? sdf.format(notice.getPublishTime()) : null);
            vo.setPublisherType(notice.getPublisherType());
            vo.setPublisherName(getPublisherName(notice, finalSchoolNameMap));
            vo.setIsTop(notice.getIsTop());
            vo.setViewCount(notice.getViewCount());
            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional
    public StudentNoticeDetailVo getNoticeDetail(String noticeId) {
        String userId = UserContext.getUserId();
        
        // 查询学生信息获取学校ID
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        String studentSchoolId = null;
        if (student != null) {
            studentSchoolId = student.getSchoolId();
        }
        
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        if (notice.getStatus() != 1) {
            throw new BusinessException("通知未发布");
        }
        
        // 权限验证：学生只能查看管理员发布的面向学生的公告，或本校发布的公告
        boolean hasPermission = false;
        if ("admin".equals(notice.getPublisherType())) {
            // 管理员发布的公告，target_audience 为 'all' 或 'student'
            if ("all".equals(notice.getTargetAudience()) || "student".equals(notice.getTargetAudience())) {
                hasPermission = true;
            }
        } else if ("school".equals(notice.getPublisherType())) {
            // 学校发布的公告，必须是本校
            if (studentSchoolId != null && studentSchoolId.equals(notice.getPublisherId())) {
                hasPermission = true;
            }
        }
        
        if (!hasPermission) {
            throw new BusinessException("无权查看该通知");
        }

        // 浏览次数+1
        notice.setViewCount(notice.getViewCount() == null ? 1 : notice.getViewCount() + 1);
        noticeMapper.updateById(notice);

        // 查询学校名称
        String publisherName = "未知";
        if ("admin".equals(notice.getPublisherType())) {
            publisherName = "省教育厅";
        } else if ("school".equals(notice.getPublisherType()) && StringUtils.hasText(notice.getPublisherId())) {
            SchoolEntity school = schoolMapper.selectById(notice.getPublisherId());
            if (school != null) {
                publisherName = school.getName();
            }
        }

        // 转换为VO
        StudentNoticeDetailVo vo = new StudentNoticeDetailVo();
        BeanUtils.copyProperties(notice, vo);
        vo.setNoticeTypeText(getNoticeTypeText(notice.getNoticeType()));
        vo.setPublisherName(publisherName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        vo.setPublishTime(notice.getPublishTime() != null ? sdf.format(notice.getPublishTime()) : null);

        return vo;
    }

    @Override
    public PageResult<StudentNoticeVo> getNoticeList(Integer pageNum, Integer pageSize, String noticeType, String publisherType) {
        String userId = UserContext.getUserId();

        // 查询学生信息获取学校ID
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        String studentSchoolId = null;
        if (student != null) {
            studentSchoolId = student.getSchoolId();
        }

        // 查询通知列表
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeEntity::getStatus, 1); // 已发布

        // 权限控制：学生可见的通知
        final String finalSchoolId = studentSchoolId;
        wrapper.and(w -> w
                // 管理员发布的面向学生的公告
                .or(w1 -> w1.eq(NoticeEntity::getPublisherType, "admin")
                           .in(NoticeEntity::getTargetAudience, "all", "student"))
                // 本校发布的公告
                .or(finalSchoolId != null, w2 -> w2.eq(NoticeEntity::getPublisherType, "school")
                                                    .eq(NoticeEntity::getPublisherId, finalSchoolId))
        );

        // 类型筛选
        if (StringUtils.hasText(noticeType)) {
            wrapper.eq(NoticeEntity::getNoticeType, noticeType);
        }

        // 发布者类型筛选
        if (StringUtils.hasText(publisherType)) {
            wrapper.eq(NoticeEntity::getPublisherType, publisherType);
        }

        wrapper.orderByDesc(NoticeEntity::getIsTop);
        wrapper.orderByDesc(NoticeEntity::getPublishTime);

        Page<NoticeEntity> page = new Page<>(pageNum, pageSize);
        Page<NoticeEntity> result = noticeMapper.selectPage(page, wrapper);

        // 批量查询学校信息
        List<String> schoolIds = result.getRecords().stream()
                .filter(n -> "school".equals(n.getPublisherType()) && StringUtils.hasText(n.getPublisherId()))
                .map(NoticeEntity::getPublisherId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> schoolNameMap = null;
        if (!schoolIds.isEmpty()) {
            List<SchoolEntity> schools = schoolMapper.selectBatchIds(schoolIds);
            schoolNameMap = schools.stream()
                    .collect(Collectors.toMap(SchoolEntity::getId, SchoolEntity::getName));
        }

        // 转换为VO
        List<StudentNoticeVo> voList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Map<String, String> finalSchoolNameMap = schoolNameMap;

        for (NoticeEntity notice : result.getRecords()) {
            StudentNoticeVo vo = new StudentNoticeVo();
            vo.setNoticeId(notice.getNoticeId());
            vo.setNoticeTitle(notice.getNoticeTitle());
            vo.setNoticeType(notice.getNoticeType());
            vo.setNoticeTypeText(getNoticeTypeText(notice.getNoticeType()));
            vo.setPublishTime(notice.getPublishTime() != null ? sdf.format(notice.getPublishTime()) : null);
            vo.setPublisherType(notice.getPublisherType());
            vo.setPublisherName(getPublisherName(notice, finalSchoolNameMap));
            vo.setIsTop(notice.getIsTop());
            vo.setViewCount(notice.getViewCount());
            voList.add(vo);
        }

        return new PageResult<>(result.getTotal(), voList);
    }

    private String getNoticeTypeText(String noticeType) {
        if (noticeType == null) {
            return "一般通知";
        }
        switch (noticeType) {
            case "ACTIVITY":
                return "活动通知";
            case "COMPETITION":
                return "赛事通知";
            case "LECTURE":
                return "讲座通知";
            case "POLICY":
                return "政策通知";
            default:
                return "一般通知";
        }
    }

    private String getPublisherName(NoticeEntity notice, Map<String, String> schoolNameMap) {
        if ("admin".equals(notice.getPublisherType())) {
            return "省教育厅";
        } else if ("school".equals(notice.getPublisherType()) && StringUtils.hasText(notice.getPublisherId())) {
            if (schoolNameMap != null && schoolNameMap.containsKey(notice.getPublisherId())) {
                return schoolNameMap.get(notice.getPublisherId());
            }
        }
        return "未知";
    }
}
