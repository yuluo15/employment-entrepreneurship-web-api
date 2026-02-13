package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.*;
import com.gxcj.entity.dto.school.SchoolProjectAuditDto;
import com.gxcj.entity.query.school.SchoolProjectQuery;
import com.gxcj.entity.vo.school.SchoolProjectDetailVo;
import com.gxcj.entity.vo.school.SchoolProjectVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolProjectService;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchoolProjectServiceImpl implements SchoolProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public PageResult<SchoolProjectVo> getProjectList(SchoolProjectQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<ProjectEntity> wrapper = new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getProjectName()), 
                      ProjectEntity::getProjectName, query.getProjectName())
                .like(StringUtils.isNotEmpty(query.getDomain()), 
                      ProjectEntity::getDomain, query.getDomain())
                .eq(StringUtils.isNotEmpty(query.getStatus()), 
                    ProjectEntity::getStatus, query.getStatus())
                .orderByDesc(ProjectEntity::getCreateTime);
        
        // 分页查询
        Page<ProjectEntity> page = projectMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 批量查询学生信息
        List<String> userIds = page.getRecords().stream()
                .map(ProjectEntity::getUserId)
                .collect(Collectors.toList());
        
        Map<String, StudentEntity> studentMap = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .in(StudentEntity::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(StudentEntity::getUserId, s -> s));
        
        List<SchoolProjectVo> voList = page.getRecords().stream()
                .filter(project -> {
                    // 如果有学生姓名筛选，需要过滤
                    if (StringUtils.isNotEmpty(query.getStudentName())) {
                        StudentEntity student = studentMap.get(project.getUserId());
                        return student != null && student.getStudentName().contains(query.getStudentName());
                    }
                    return true;
                })
                .map(project -> {
                    SchoolProjectVo vo = new SchoolProjectVo();
                    BeanUtils.copyProperties(project, vo);
                    vo.setCreateTime(project.getCreateTime() != null ? sdf.format(project.getCreateTime()) : null);
                    
                    // 设置学生信息
                    StudentEntity student = studentMap.get(project.getUserId());
                    if (student != null) {
                        vo.setStudentName(student.getStudentName());
                        vo.setStudentNo(student.getStudentNo());
                    }
                    
                    return vo;
                }).collect(Collectors.toList());
        
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public SchoolProjectDetailVo getProjectDetail(String projectId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 查询项目（带权限校验）
        ProjectEntity project = projectMapper.selectOne(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getProjectId, projectId)
                .eq(ProjectEntity::getSchoolId, schoolId));
        
        if (project == null) {
            throw new BusinessException("项目不存在或无权访问");
        }
        
        // 查询学生信息
        StudentEntity student = studentMapper.selectOne(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getUserId, project.getUserId()));
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SchoolProjectDetailVo vo = new SchoolProjectDetailVo();
        BeanUtils.copyProperties(project, vo);
        vo.setCreateTime(project.getCreateTime() != null ? sdf.format(project.getCreateTime()) : null);
        vo.setUpdateTime(project.getUpdateTime() != null ? sdf.format(project.getUpdateTime()) : null);
        vo.setAuditTime(project.getAuditTime() != null ? sdf.format(project.getAuditTime()) : null);
        
        if (student != null) {
            vo.setStudentName(student.getStudentName());
            vo.setStudentNo(student.getStudentNo());
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditProject(SchoolProjectAuditDto dto, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 参数校验
        if (!"1".equals(dto.getStatus()) && !"2".equals(dto.getStatus())) {
            throw new BusinessException("审核状态参数错误");
        }
        
        // 驳回时必须填写原因
        if ("2".equals(dto.getStatus()) && StringUtils.isBlank(dto.getAuditReason())) {
            throw new BusinessException("驳回时必须填写驳回原因");
        }
        
        // 查询项目
        ProjectEntity project = projectMapper.selectOne(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getProjectId, dto.getProjectId())
                .eq(ProjectEntity::getSchoolId, schoolId));
        
        if (project == null) {
            throw new BusinessException("项目不存在或无权访问");
        }
        
        // 只能审核待审核状态的项目
        if (!"0".equals(project.getStatus())) {
            throw new BusinessException("只能审核待审核状态的项目");
        }
        
        // 更新项目状态
        project.setStatus(dto.getStatus());
        project.setAuditReason(dto.getAuditReason());
        project.setAuditTime(EntityHelper.now());
        project.setUpdateTime(EntityHelper.now());
        projectMapper.updateById(project);
        
        // 发送系统消息通知学生
        MessageEntity message = new MessageEntity();
        message.setId(EntityHelper.uuid());
        message.setReceiverId(project.getUserId());
        message.setType(1); // 系统消息
        message.setIsRead(0);
        message.setRefId(dto.getProjectId());
        message.setCreateTime(EntityHelper.now());
        
        if ("1".equals(dto.getStatus())) {
            message.setTitle("项目审核通过");
            message.setContent("您的创业项目《" + project.getProjectName() + "》已通过审核，现在可以开始孵化了！");
        } else {
            message.setTitle("项目审核未通过");
            message.setContent("您的创业项目《" + project.getProjectName() + "》审核未通过。驳回原因：" + dto.getAuditReason());
        }
        
        messageMapper.insert(message);
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
        
        throw new BusinessException("无权访问项目信息");
    }
}
