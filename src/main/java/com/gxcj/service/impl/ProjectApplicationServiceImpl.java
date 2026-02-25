package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.MessageEntity;
import com.gxcj.entity.ProjectApplicationEntity;
import com.gxcj.entity.ProjectEntity;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.dto.ProjectApplyDto;
import com.gxcj.entity.dto.ProjectApplicationHandleDto;
import com.gxcj.entity.vo.ProjectApplicantVo;
import com.gxcj.entity.vo.ProjectApplicationVo;
import com.gxcj.entity.vo.job.MyJoinedProjectVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.MessageMapper;
import com.gxcj.mapper.ProjectApplicationMapper;
import com.gxcj.mapper.ProjectMapper;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.ProjectApplicationService;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectApplicationServiceImpl implements ProjectApplicationService {

    @Autowired
    private ProjectApplicationMapper projectApplicationMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SchoolMapper schoolMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public Map<String, String> applyProject(ProjectApplyDto dto) {
        String userId = UserContext.getUserId();

        // 查询项目
        ProjectEntity project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 检查是否是项目负责人
        if (project.getUserId().equals(userId)) {
            throw new BusinessException("不能申请自己的项目");
        }

        // 验证申请理由长度
        if (!StringUtils.hasText(dto.getApplicationReason()) || dto.getApplicationReason().length() < 20) {
            throw new BusinessException("申请理由至少20个字");
        }

        // 查询学生信息
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 查询用户信息（获取头像）
        UserEntity user = userMapper.selectById(userId);

        // 查询学校信息
        SchoolEntity school = null;
        if (student.getSchoolId() != null) {
            school = schoolMapper.selectById(student.getSchoolId());
        }

        // 创建申请记录
        ProjectApplicationEntity application = new ProjectApplicationEntity();
        application.setId(EntityHelper.uuid());
        application.setProjectId(dto.getProjectId());
        application.setApplicantId(student.getStudentId());
        application.setApplicantName(student.getStudentName());
        application.setApplicantAvatar(user != null ? user.getAvatar() : null);
        application.setApplicantSchool(school != null ? school.getName() : null);
        application.setApplicantMajor(student.getMajorName());
        application.setApplicationReason(dto.getApplicationReason());
        application.setSkills(dto.getSkills());
        application.setStatus("PENDING");
        application.setCreateTime(EntityHelper.now());
        application.setUpdateTime(EntityHelper.now());

        try {
            projectApplicationMapper.insert(application);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("您已经申请过该项目");
        }

        // 发送系统消息通知项目负责人
        MessageEntity message = new MessageEntity();
        message.setId(EntityHelper.uuid());
        message.setReceiverId(project.getUserId());
        message.setTitle("新的项目申请");
        message.setContent("学生 " + student.getStudentName() + " 申请加入您的项目《" + project.getProjectName() + "》");
        message.setType(1); // 系统通知
        message.setIsRead(0); // 未读
        message.setRefId(application.getId());
        message.setCreateTime(EntityHelper.now());
        messageMapper.insert(message);

        Map<String, String> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", application.getStatus());
        return result;
    }

    @Override
    @Transactional
    public void cancelApplication(String applicationId) {
        String userId = UserContext.getUserId();

        // 查询学生信息
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 查询申请记录
        ProjectApplicationEntity application = projectApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }

        // 验证是否是申请人
        if (!application.getApplicantId().equals(student.getStudentId())) {
            throw new BusinessException("无权取消该申请");
        }

        // 只能取消待审核状态的申请
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("只能取消待审核状态的申请");
        }

        // 更新状态为已取消
        application.setStatus("CANCELLED");
        application.setUpdateTime(EntityHelper.now());
        projectApplicationMapper.updateById(application);
    }

    @Override
    public PageResult<ProjectApplicationVo> getMyApplications(Integer pageNum, Integer pageSize, String status) {
        String userId = UserContext.getUserId();

        // 查询学生信息
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 查询申请记录
        LambdaQueryWrapper<ProjectApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplicationEntity::getApplicantId, student.getStudentId());
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProjectApplicationEntity::getStatus, status);
        }
        wrapper.orderByDesc(ProjectApplicationEntity::getCreateTime);

        Page<ProjectApplicationEntity> page = new Page<>(pageNum, pageSize);
        Page<ProjectApplicationEntity> result = projectApplicationMapper.selectPage(page, wrapper);

        List<ProjectApplicationVo> voList = new ArrayList<>();
        if (!result.getRecords().isEmpty()) {
            // 批量查询项目信息
            List<String> projectIds = result.getRecords().stream()
                    .map(ProjectApplicationEntity::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());
            List<ProjectEntity> projects = projectMapper.selectBatchIds(projectIds);
            Map<String, ProjectEntity> projectMap = projects.stream()
                    .collect(Collectors.toMap(ProjectEntity::getProjectId, p -> p));

            // 批量查询项目负责人信息
            List<String> userIds = projects.stream()
                    .map(ProjectEntity::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<UserEntity> users = userMapper.selectBatchIds(userIds);
            Map<String, UserEntity> userMap = users.stream()
                    .collect(Collectors.toMap(UserEntity::getId, u -> u));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (ProjectApplicationEntity application : result.getRecords()) {
                ProjectApplicationVo vo = new ProjectApplicationVo();
                vo.setId(application.getId());
                vo.setProjectId(application.getProjectId());
                vo.setApplicationReason(application.getApplicationReason());
                vo.setSkills(application.getSkills());
                vo.setStatus(application.getStatus());
                vo.setReplyMessage(application.getReplyMessage());
                vo.setCreateTime(application.getCreateTime() != null ? sdf.format(application.getCreateTime()) : null);
                vo.setReplyTime(application.getReplyTime() != null ? sdf.format(application.getReplyTime()) : null);

                ProjectEntity project = projectMap.get(application.getProjectId());
                if (project != null) {
                    vo.setProjectName(project.getProjectName());
                    vo.setProjectLogo(project.getLogo());

                    UserEntity user = userMap.get(project.getUserId());
                    if (user != null) {
                        vo.setLeaderName(user.getNickname());
                    }
                }

                voList.add(vo);
            }
        }

        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    public PageResult<ProjectApplicantVo> getProjectApplications(String projectId, Integer pageNum, Integer pageSize, String status) {
        String userId = UserContext.getUserId();

        // 查询项目
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 验证是否是项目负责人
        if (!project.getUserId().equals(userId)) {
            throw new BusinessException("只有项目负责人可以查看申请列表");
        }

        // 查询申请记录
        LambdaQueryWrapper<ProjectApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplicationEntity::getProjectId, projectId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProjectApplicationEntity::getStatus, status);
        }
        wrapper.orderByDesc(ProjectApplicationEntity::getCreateTime);

        Page<ProjectApplicationEntity> page = new Page<>(pageNum, pageSize);
        Page<ProjectApplicationEntity> result = projectApplicationMapper.selectPage(page, wrapper);

        List<ProjectApplicantVo> voList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (ProjectApplicationEntity application : result.getRecords()) {
            ProjectApplicantVo vo = new ProjectApplicantVo();
            vo.setId(application.getId());
            vo.setApplicantId(application.getApplicantId());
            vo.setApplicantName(application.getApplicantName());
            vo.setApplicantAvatar(application.getApplicantAvatar());
            vo.setApplicantSchool(application.getApplicantSchool());
            vo.setApplicantMajor(application.getApplicantMajor());
            vo.setApplicationReason(application.getApplicationReason());
            vo.setSkills(application.getSkills());
            vo.setStatus(application.getStatus());
            vo.setCreateTime(application.getCreateTime() != null ? sdf.format(application.getCreateTime()) : null);

            voList.add(vo);
        }

        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    @Transactional
    public void handleApplication(String applicationId, ProjectApplicationHandleDto dto) {
        String userId = UserContext.getUserId();

        // 查询申请记录
        ProjectApplicationEntity application = projectApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }

        // 查询项目
        ProjectEntity project = projectMapper.selectById(application.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 验证是否是项目负责人
        if (!project.getUserId().equals(userId)) {
            throw new BusinessException("只有项目负责人可以处理申请");
        }

        // 只能处理待审核状态的申请
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("只能处理待审核状态的申请");
        }

        // 更新申请状态
        if ("APPROVE".equals(dto.getAction())) {
            application.setStatus("APPROVED");
        } else if ("REJECT".equals(dto.getAction())) {
            application.setStatus("REJECTED");
        } else {
            throw new BusinessException("无效的操作");
        }

        application.setReplyMessage(dto.getReplyMessage());
        application.setReplyTime(EntityHelper.now());
        application.setUpdateTime(EntityHelper.now());
        projectApplicationMapper.updateById(application);

        // 发送系统消息通知申请人
        // 查询申请人的userId
        StudentEntity applicant = studentMapper.selectById(application.getApplicantId());
        if (applicant != null && applicant.getUserId() != null) {
            MessageEntity message = new MessageEntity();
            message.setId(EntityHelper.uuid());
            message.setReceiverId(applicant.getUserId());
            
            if ("APPROVE".equals(dto.getAction())) {
                message.setTitle("项目申请已通过");
                message.setContent("您申请加入的项目《" + project.getProjectName() + "》已通过审核");
            } else {
                message.setTitle("项目申请未通过");
                message.setContent("您申请加入的项目《" + project.getProjectName() + "》未通过审核");
            }
            
            message.setType(1); // 系统通知
            message.setIsRead(0); // 未读
            message.setRefId(applicationId);
            message.setCreateTime(EntityHelper.now());
            messageMapper.insert(message);
        }
    }

    @Override
    public PageResult<MyJoinedProjectVo> getMyJoinedProjects(Integer pageNum, Integer pageSize, String status) {
        String userId = UserContext.getUserId();

        // 查询学生信息
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 查询申请记录
        LambdaQueryWrapper<ProjectApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplicationEntity::getApplicantId, student.getStudentId());
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProjectApplicationEntity::getStatus, status);
        }
        wrapper.orderByDesc(ProjectApplicationEntity::getCreateTime);

        Page<ProjectApplicationEntity> page = new Page<>(pageNum, pageSize);
        Page<ProjectApplicationEntity> result = projectApplicationMapper.selectPage(page, wrapper);

        List<MyJoinedProjectVo> voList = new ArrayList<>();
        if (!result.getRecords().isEmpty()) {
            // 批量查询项目信息
            List<String> projectIds = result.getRecords().stream()
                    .map(ProjectApplicationEntity::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());
            List<ProjectEntity> projects = projectMapper.selectBatchIds(projectIds);
            Map<String, ProjectEntity> projectMap = projects.stream()
                    .collect(Collectors.toMap(ProjectEntity::getProjectId, p -> p));

            // 批量查询项目负责人信息
            List<String> userIds = projects.stream()
                    .map(ProjectEntity::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<UserEntity> users = userMapper.selectBatchIds(userIds);
            Map<String, UserEntity> userMap = users.stream()
                    .collect(Collectors.toMap(UserEntity::getId, u -> u));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (ProjectApplicationEntity application : result.getRecords()) {
                MyJoinedProjectVo vo = new MyJoinedProjectVo();
                vo.setApplicationId(application.getId());
                vo.setProjectId(application.getProjectId());
                vo.setStatus(application.getStatus());
                vo.setApplicationReason(application.getApplicationReason());
                vo.setReplyMessage(application.getReplyMessage());
                vo.setApplyTime(application.getCreateTime() != null ? sdf.format(application.getCreateTime()) : null);
                vo.setReplyTime(application.getReplyTime() != null ? sdf.format(application.getReplyTime()) : null);

                ProjectEntity project = projectMap.get(application.getProjectId());
                if (project != null) {
                    vo.setProjectName(project.getProjectName());
                    vo.setLogo(project.getLogo());
                    vo.setDomain(project.getDomain());
                    vo.setTeamSize(project.getTeamSize());

                    UserEntity user = userMap.get(project.getUserId());
                    if (user != null) {
                        vo.setLeaderName(user.getNickname());
                    }
                }

                voList.add(vo);
            }
        }

        return new PageResult<>(result.getTotal(), voList);
    }
}
