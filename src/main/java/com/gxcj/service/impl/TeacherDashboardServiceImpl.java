package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.teacher.*;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.TeacherDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectCommentMapper projectCommentMapper;
    
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public TeacherDashboardVo getDashboardData(String userId) {
        log.info("获取教师工作台数据: userId={}", userId);
        
        // 获取教师信息
        TeacherEntity teacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<TeacherEntity>()
                        .eq(TeacherEntity::getUserId, userId)
        );
        
        if (teacher == null) {
            throw new BusinessException("教师信息不存在");
        }
        
        TeacherDashboardVo vo = new TeacherDashboardVo();
        
        // 1. 教师基本信息
        vo.setTeacherInfo(getTeacherInfo(teacher));
        
        // 2. 统计数据
        vo.setStats(getStats(teacher));
        
        // 3. 待指导项目（最新5个）
        vo.setPendingProjects(getPendingProjects(teacher));
        
        // 4. 最近指导记录（最新5条）
        vo.setRecentGuidance(getRecentGuidance(teacher));
        
        return vo;
    }

    // ==================== 私有方法 ====================

    /**
     * 获取教师基本信息
     */
    private TeacherInfoVo getTeacherInfo(TeacherEntity teacher) {
        TeacherInfoVo vo = new TeacherInfoVo();
        vo.setTeacherId(teacher.getTeacherId());
        vo.setName(teacher.getName());
        vo.setTitle(teacher.getTitle());
        vo.setCollegeName(teacher.getCollegeName());
        vo.setSchoolId(teacher.getSchoolId());
        vo.setExpertise(teacher.getExpertise());
        
        // 获取学校名称
        SchoolEntity school = schoolMapper.selectById(teacher.getSchoolId());
        if (school != null) {
            vo.setSchoolName(school.getName());
        }
        
        return vo;
    }

    /**
     * 获取统计数据
     */
    private TeacherStatsVo getStats(TeacherEntity teacher) {
        TeacherStatsVo vo = new TeacherStatsVo();
        
        // 总指导次数
        int guidanceCount = projectCommentMapper.selectCount(
                new LambdaQueryWrapper<ProjectCommentEntity>()
                        .eq(ProjectCommentEntity::getTeacherId, teacher.getTeacherId())
        ).intValue();
        vo.setGuidanceCount(guidanceCount);
        
        // 指导项目数（去重）
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(
                new LambdaQueryWrapper<ProjectCommentEntity>()
                        .eq(ProjectCommentEntity::getTeacherId, teacher.getTeacherId())
        );
        long projectCount = comments.stream()
                .map(ProjectCommentEntity::getProjectId)
                .distinct()
                .count();
        vo.setProjectCount((int) projectCount);
        
        // 评分（使用教师表中的rating_score）
        vo.setRatingScore(teacher.getRatingScore() != null ? 
                teacher.getRatingScore().doubleValue() : 5.0);
        
        // 本周新增项目数（本校）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date weekAgo = cal.getTime();
        
        int weekNewCount = projectMapper.selectCount(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getSchoolId, teacher.getSchoolId())
                        .ge(ProjectEntity::getCreateTime, weekAgo)
        ).intValue();
        vo.setWeekNewCount(weekNewCount);
        
        return vo;
    }

    /**
     * 获取待指导项目（指导次数为0的项目，优先本校，最新5个）
     */
    private List<TeacherPendingProjectVo> getPendingProjects(TeacherEntity teacher) {
        // 获取该教师已指导过的项目ID列表
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(
                new LambdaQueryWrapper<ProjectCommentEntity>()
                        .eq(ProjectCommentEntity::getTeacherId, teacher.getTeacherId())
        );
        
        Set<String> guidedProjectIds = comments.stream()
                .map(ProjectCommentEntity::getProjectId)
                .collect(Collectors.toSet());
        
        // 查询所有项目
        List<ProjectEntity> allProjects = projectMapper.selectList(
                new LambdaQueryWrapper<ProjectEntity>()
                        .orderByDesc(ProjectEntity::getCreateTime)
        );
        
        // 过滤出未指导的项目，优先本校
        List<ProjectEntity> pendingProjects = allProjects.stream()
                .filter(p -> !guidedProjectIds.contains(p.getProjectId()))
                .sorted((p1, p2) -> {
                    // 本校项目优先
                    boolean isSchool1 = teacher.getSchoolId().equals(p1.getSchoolId());
                    boolean isSchool2 = teacher.getSchoolId().equals(p2.getSchoolId());
                    if (isSchool1 && !isSchool2) return -1;
                    if (!isSchool1 && isSchool2) return 1;
                    // 时间倒序
                    return p2.getCreateTime().compareTo(p1.getCreateTime());
                })
                .limit(5)
                .collect(Collectors.toList());
        
        return pendingProjects.stream().map(project -> {
            TeacherPendingProjectVo vo = new TeacherPendingProjectVo();
            vo.setProjectId(project.getProjectId());
            vo.setProjectName(project.getProjectName());
            vo.setLogo(project.getLogo());
            vo.setDomain(project.getDomain());
            vo.setCreateTime(formatRelativeTime(project.getCreateTime()));
            
            // 获取学生信息
            StudentEntity student = studentMapper.selectOne(
                    new LambdaQueryWrapper<StudentEntity>()
                            .eq(StudentEntity::getUserId, project.getUserId())
            );
            if (student != null) {
                vo.setStudentId(student.getStudentId());
                vo.setStudentName(student.getStudentName());
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取最近指导记录（最新5条）
     */
    private List<TeacherRecentGuidanceVo> getRecentGuidance(TeacherEntity teacher) {
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(
                new LambdaQueryWrapper<ProjectCommentEntity>()
                        .eq(ProjectCommentEntity::getTeacherId, teacher.getTeacherId())
                        .orderByDesc(ProjectCommentEntity::getCreateTime)
                        .last("LIMIT 5")
        );
        
        return comments.stream().map(comment -> {
            TeacherRecentGuidanceVo vo = new TeacherRecentGuidanceVo();
            vo.setId(comment.getId());
            vo.setProjectId(comment.getProjectId());
            vo.setProjectName(comment.getProjectName());
            vo.setContent(comment.getContent());
            vo.setCreateTime(formatRelativeTime(comment.getCreateTime()));
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 格式化相对时间
     */
    private String formatRelativeTime(Date date) {
        if (date == null) {
            return "";
        }
        
        long diff = System.currentTimeMillis() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (seconds < 60) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 30) {
            return days + "天前";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
    }
}
