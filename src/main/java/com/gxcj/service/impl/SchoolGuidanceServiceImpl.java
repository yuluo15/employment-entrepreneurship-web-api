package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.ProjectCommentEntity;
import com.gxcj.entity.ProjectEntity;
import com.gxcj.entity.RoleEntity;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.query.school.SchoolGuidanceQuery;
import com.gxcj.entity.vo.school.SchoolGuidanceDetailVo;
import com.gxcj.entity.vo.school.SchoolGuidanceVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.ProjectCommentMapper;
import com.gxcj.mapper.ProjectMapper;
import com.gxcj.mapper.RoleMapper;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.mapper.TeacherMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolGuidanceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchoolGuidanceServiceImpl implements SchoolGuidanceService {

    @Autowired
    private ProjectCommentMapper commentMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public PageResult<SchoolGuidanceVo> getGuidanceList(SchoolGuidanceQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 查询本校所有教师ID
        List<TeacherEntity> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<TeacherEntity>()
                        .eq(TeacherEntity::getSchoolId, schoolId)
                        .select(TeacherEntity::getTeacherId));
        
        if (teachers.isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }
        
        List<String> teacherIds = teachers.stream()
                .map(TeacherEntity::getTeacherId)
                .collect(Collectors.toList());
        
        // 构建查询条件
        LambdaQueryWrapper<ProjectCommentEntity> wrapper = new LambdaQueryWrapper<ProjectCommentEntity>()
                .in(ProjectCommentEntity::getTeacherId, teacherIds);
        
        // 教师姓名筛选
        if (query.getTeacherName() != null && !query.getTeacherName().isEmpty()) {
            wrapper.like(ProjectCommentEntity::getTeacherName, query.getTeacherName());
        }
        
        // 项目名称筛选
        if (query.getProjectName() != null && !query.getProjectName().isEmpty()) {
            wrapper.like(ProjectCommentEntity::getProjectName, query.getProjectName());
        }
        
        // 时间范围筛选
        if (query.getStartTime() != null && !query.getStartTime().isEmpty()) {
            wrapper.apply("DATE(create_time) >= {0}", query.getStartTime());
        }
        if (query.getEndTime() != null && !query.getEndTime().isEmpty()) {
            wrapper.apply("DATE(create_time) <= {0}", query.getEndTime());
        }
        
        // 查询总数
        Long total = commentMapper.selectCount(wrapper);
        
        // 分页查询
        wrapper.orderByDesc(ProjectCommentEntity::getCreateTime);
        int offset = (query.getPageNum() - 1) * query.getPageSize();
        wrapper.last("LIMIT " + query.getPageSize() + " OFFSET " + offset);
        
        List<ProjectCommentEntity> comments = commentMapper.selectList(wrapper);
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SchoolGuidanceVo> voList = new ArrayList<>();
        if (total.intValue() == 0) {
            return new PageResult<>(total, voList);
        }
        // 批量查询教师信息
        Map<String, TeacherEntity> teacherMap = teacherMapper.selectList(
                new LambdaQueryWrapper<TeacherEntity>()
                        .in(TeacherEntity::getTeacherId, 
                            comments.stream().map(ProjectCommentEntity::getTeacherId).collect(Collectors.toList())))
                .stream()
                .collect(Collectors.toMap(TeacherEntity::getTeacherId, t -> t));
        
        // 批量查询项目信息
        Map<String, ProjectEntity> projectMap = projectMapper.selectList(
                new LambdaQueryWrapper<ProjectEntity>()
                        .in(ProjectEntity::getProjectId, 
                            comments.stream().map(ProjectCommentEntity::getProjectId).collect(Collectors.toList())))
                .stream()
                .collect(Collectors.toMap(ProjectEntity::getProjectId, p -> p));
        
        // 批量查询学生信息
        List<String> userIds = projectMap.values().stream()
                .map(ProjectEntity::getUserId)
                .collect(Collectors.toList());
        Map<String, StudentEntity> studentMap = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .in(StudentEntity::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(StudentEntity::getUserId, s -> s));
        
        for (ProjectCommentEntity comment : comments) {
            SchoolGuidanceVo vo = new SchoolGuidanceVo();
            vo.setId(comment.getId());
            vo.setTeacherId(comment.getTeacherId());
            vo.setTeacherName(comment.getTeacherName());
            vo.setProjectId(comment.getProjectId());
            vo.setProjectName(comment.getProjectName());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime() != null ? sdf.format(comment.getCreateTime()) : null);
            
            // 设置教师信息
            TeacherEntity teacher = teacherMap.get(comment.getTeacherId());
            if (teacher != null) {
                vo.setEmployeeNo(teacher.getEmployeeNo());
                vo.setCollegeName(teacher.getCollegeName());
                vo.setTitle(teacher.getTitle());
            }
            
            // 设置学生信息
            ProjectEntity project = projectMap.get(comment.getProjectId());
            if (project != null) {
                StudentEntity student = studentMap.get(project.getUserId());
                if (student != null) {
                    vo.setStudentName(student.getStudentName());
                }
            }
            
            voList.add(vo);
        }
        
        return new PageResult<>(total, voList);
    }

    @Override
    public SchoolGuidanceDetailVo getGuidanceDetail(String id, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 查询指导记录
        ProjectCommentEntity comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("指导记录不存在");
        }
        
        // 查询教师信息并验证权限
        TeacherEntity teacher = teacherMapper.selectById(comment.getTeacherId());
        if (teacher == null || !schoolId.equals(teacher.getSchoolId())) {
            throw new BusinessException("指导记录不存在或无权访问");
        }
        
        // 查询项目信息
        ProjectEntity project = projectMapper.selectById(comment.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 查询学生信息
        StudentEntity student = studentMapper.selectOne(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getUserId, project.getUserId()));
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SchoolGuidanceDetailVo vo = new SchoolGuidanceDetailVo();
        vo.setId(comment.getId());
        vo.setTeacherId(comment.getTeacherId());
        vo.setTeacherName(comment.getTeacherName());
        vo.setEmployeeNo(teacher.getEmployeeNo());
        vo.setCollegeName(teacher.getCollegeName());
        vo.setTitle(teacher.getTitle());
        vo.setProjectId(comment.getProjectId());
        vo.setProjectName(comment.getProjectName());
        vo.setDomain(project.getDomain());
        vo.setTeamSize(project.getTeamSize());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime() != null ? sdf.format(comment.getCreateTime()) : null);
        
        if (student != null) {
            vo.setStudentName(student.getStudentName());
            vo.setStudentNo(student.getStudentNo());
        }
        
        return vo;
    }

    // ==================== 私有方法 ====================

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
        
        throw new BusinessException("无权访问指导记录");
    }
}
