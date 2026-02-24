package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.*;
import com.gxcj.entity.dto.TeacherGuidanceAddDto;
import com.gxcj.entity.query.TeacherGuidanceQuery;
import com.gxcj.entity.vo.teacher.TeacherGuidanceStatsVo;
import com.gxcj.entity.vo.teacher.TeacherGuidanceVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.TeacherGuidanceService;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherGuidanceServiceImpl implements TeacherGuidanceService {

    @Autowired
    private ProjectCommentMapper projectCommentMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TeacherGuidanceStatsVo getStats() {
        String userId = UserContext.getUserId();
        TeacherEntity teacherEntity = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>().eq(TeacherEntity::getUserId, userId));
        String teacherId = teacherEntity.getTeacherId();

        LambdaQueryWrapper<ProjectCommentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectCommentEntity::getTeacherId, teacherId);

        // 总指导次数
        Long totalCount = projectCommentMapper.selectCount(wrapper);

        // 指导项目数（去重）
        List<ProjectCommentEntity> allComments = projectCommentMapper.selectList(wrapper);
        long projectCount = allComments.stream()
                .map(ProjectCommentEntity::getProjectId)
                .distinct()
                .count();

        // 本月指导次数
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        Timestamp monthStart = Timestamp.valueOf(firstDayOfMonth.atStartOfDay());

        LambdaQueryWrapper<ProjectCommentEntity> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(ProjectCommentEntity::getTeacherId, teacherId)
                .ge(ProjectCommentEntity::getCreateTime, monthStart);
        Long monthCount = projectCommentMapper.selectCount(monthWrapper);

        TeacherGuidanceStatsVo vo = new TeacherGuidanceStatsVo();
        vo.setTotalCount(totalCount.intValue());
        vo.setProjectCount((int) projectCount);
        vo.setMonthCount(monthCount.intValue());

        return vo;
    }

    @Override
    public PageResult<TeacherGuidanceVo> getList(TeacherGuidanceQuery query) {
        String userId = UserContext.getUserId();
        TeacherEntity teacherEntity = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>().eq(TeacherEntity::getUserId, userId));
        String teacherId = teacherEntity.getTeacherId();
        UserEntity userEntity = userMapper.selectById(userId);

        LambdaQueryWrapper<ProjectCommentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectCommentEntity::getTeacherId, teacherId);

        // 关键词搜索（项目名称）
        if (StringUtils.hasText(query.getKeyword())) {
            // 先查询符合条件的项目ID
            LambdaQueryWrapper<ProjectEntity> projectWrapper = new LambdaQueryWrapper<>();
            projectWrapper.like(ProjectEntity::getProjectName, query.getKeyword());
            List<ProjectEntity> projects = projectMapper.selectList(projectWrapper);
            List<String> projectIds = projects.stream()
                    .map(ProjectEntity::getProjectId)
                    .collect(Collectors.toList());

            if (projectIds.isEmpty()) {
                return new PageResult<>(0L, new ArrayList<>());
            }

            wrapper.in(ProjectCommentEntity::getProjectId, projectIds);
        }

        wrapper.orderByDesc(ProjectCommentEntity::getCreateTime);

        Page<ProjectCommentEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ProjectCommentEntity> result = projectCommentMapper.selectPage(page, wrapper);

        List<TeacherGuidanceVo> voList = new ArrayList<>();
        if (!result.getRecords().isEmpty()) {
            // 批量查询项目信息
            List<String> projectIds = result.getRecords().stream()
                    .map(ProjectCommentEntity::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());
            List<ProjectEntity> projects = projectMapper.selectBatchIds(projectIds);
            Map<String, ProjectEntity> projectMap = projects.stream()
                    .collect(Collectors.toMap(ProjectEntity::getProjectId, p -> p));

            // 批量查询用户信息（通过userId获取学生姓名）
            List<String> userIds = projects.stream()
                    .map(ProjectEntity::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 通过userId查询学生信息
            LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.in(StudentEntity::getUserId, userIds);
            List<StudentEntity> students = studentMapper.selectList(studentWrapper);
            Map<String, StudentEntity> studentMap = students.stream()
                    .collect(Collectors.toMap(StudentEntity::getUserId, s -> s));

            // 查询教师信息
            TeacherEntity teacher = teacherMapper.selectById(teacherId);

            for (ProjectCommentEntity comment : result.getRecords()) {
                TeacherGuidanceVo vo = new TeacherGuidanceVo();
                vo.setId(comment.getId());
                vo.setProjectId(comment.getProjectId());
                vo.setContent(comment.getContent());
                vo.setTeacherId(teacherId);

                ProjectEntity project = projectMap.get(comment.getProjectId());
                if (project != null) {
                    vo.setProjectName(project.getProjectName());
                    vo.setProjectLogo(project.getLogo());
                    vo.setDomain(project.getDomain());

                    StudentEntity student = studentMap.get(project.getUserId());
                    if (student != null) {
                        vo.setStudentId(student.getStudentId());
                        vo.setStudentName(student.getStudentName());
                    }
                }

                if (teacher != null) {
                    vo.setTeacherName(teacher.getName());
                    vo.setTeacherAvatar(userEntity.getAvatar());
                }

                vo.setCreateTime(formatRelativeTime(comment.getCreateTime()));

                voList.add(vo);
            }
        }

        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    @Transactional
    public String addGuidance(TeacherGuidanceAddDto dto) {
        String userId = UserContext.getUserId();
        TeacherEntity teacherEntity = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>().eq(TeacherEntity::getUserId, userId));

        // 验证项目是否存在
        ProjectEntity project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 验证指导内容长度
        if (!StringUtils.hasText(dto.getContent()) || dto.getContent().length() < 10) {
            throw new BusinessException("指导内容不能少于10个字");
        }

        // 插入指导记录
        ProjectCommentEntity comment = new ProjectCommentEntity();
        comment.setId(EntityHelper.uuid());
        comment.setProjectId(dto.getProjectId());
        comment.setTeacherId(teacherEntity.getTeacherId());
        comment.setContent(dto.getContent());
        comment.setCreateTime(EntityHelper.now());
        comment.setProjectName(project.getProjectName());
        comment.setTeacherName(teacherEntity.getName());

        projectCommentMapper.insert(comment);

        teacherMapper.update(new LambdaUpdateWrapper<TeacherEntity>()
                .set(TeacherEntity::getGuidanceCount, teacherEntity.getGuidanceCount() + 1)
                .eq(TeacherEntity::getTeacherId, teacherEntity.getTeacherId()));

        return comment.getId();
    }

    private String formatRelativeTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime createTime = timestamp.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(createTime, now);
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        }

        long hours = ChronoUnit.HOURS.between(createTime, now);
        if (hours < 24) {
            return hours + "小时前";
        }

        long days = ChronoUnit.DAYS.between(createTime, now);
        return days + "天前";
    }
}
