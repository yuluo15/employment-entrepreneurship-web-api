package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.context.UserContext;
import com.gxcj.entity.*;
import com.gxcj.entity.dto.ProjectStageSaveDto;
import com.gxcj.entity.dto.StageStatusUpdateDto;
import com.gxcj.entity.vo.ProjectStageVo;
import com.gxcj.entity.vo.StageGuidanceVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.ProjectStageService;
import com.gxcj.utils.EntityHelper;
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
public class ProjectStageServiceImpl implements ProjectStageService {

    @Autowired
    private ProjectStageMapper projectStageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectCommentMapper projectCommentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void saveStages(ProjectStageSaveDto dto) {
        String userId = UserContext.getUserId();

        // 验证项目是否存在
        ProjectEntity project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 验证是否是项目负责人
        if (!project.getUserId().equals(userId)) {
            throw new BusinessException("只有项目负责人可以设置项目阶段");
        }

        // 删除该项目原有的所有阶段
        LambdaQueryWrapper<ProjectStageEntity> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProjectStageEntity::getProjectId, dto.getProjectId());
        projectStageMapper.delete(deleteWrapper);

        // 批量插入新的阶段数据
        if (dto.getStages() != null && !dto.getStages().isEmpty()) {
            for (ProjectStageSaveDto.StageItem item : dto.getStages()) {
                ProjectStageEntity stage = new ProjectStageEntity();
                stage.setStageId(EntityHelper.uuid());
                stage.setProjectId(dto.getProjectId());
                stage.setStageName(item.getStageName());
                stage.setStageOrder(item.getStageOrder());
                stage.setDescription(item.getDescription());
                stage.setStatus(StringUtils.hasText(item.getStatus()) ? item.getStatus() : "NOT_STARTED");
                stage.setCreateTime(EntityHelper.now());
                stage.setUpdateTime(EntityHelper.now());

                projectStageMapper.insert(stage);
            }
        }
    }

    @Override
    public List<ProjectStageVo> getProjectStages(String projectId, boolean isTeacher) {
        // 查询项目的所有阶段
        LambdaQueryWrapper<ProjectStageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectStageEntity::getProjectId, projectId)
                .orderByAsc(ProjectStageEntity::getStageOrder);
        List<ProjectStageEntity> stages = projectStageMapper.selectList(wrapper);

        if (stages.isEmpty()) {
            return new ArrayList<>();
        }

        // 统计每个阶段的指导评论数量
        List<String> stageIds = stages.stream()
                .map(ProjectStageEntity::getStageId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProjectCommentEntity> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(ProjectCommentEntity::getStageId, stageIds);
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(commentWrapper);

        Map<String, Long> guidanceCountMap = comments.stream()
                .collect(Collectors.groupingBy(ProjectCommentEntity::getStageId, Collectors.counting()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<ProjectStageVo> voList = new ArrayList<>();
        for (ProjectStageEntity stage : stages) {
            ProjectStageVo vo = new ProjectStageVo();
            vo.setStageId(stage.getStageId());
            vo.setStageName(stage.getStageName());
            vo.setStageOrder(stage.getStageOrder());
            vo.setDescription(stage.getDescription());
            vo.setStatus(stage.getStatus());
            vo.setStartTime(stage.getStartTime() != null ? sdf.format(stage.getStartTime()) : null);
            vo.setCompleteTime(stage.getCompleteTime() != null ? sdf.format(stage.getCompleteTime()) : null);
            vo.setGuidanceCount(guidanceCountMap.getOrDefault(stage.getStageId(), 0L).intValue());

            // 教师端需要判断是否可以指导
            // 关键修改：只有进行中（IN_PROGRESS）的阶段才能指导
            if (isTeacher) {
                vo.setCanGuidance("IN_PROGRESS".equals(stage.getStatus()));
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    @Transactional
    public void updateStageStatus(String stageId, StageStatusUpdateDto dto) {
        String userId = UserContext.getUserId();

        // 验证阶段是否存在
        ProjectStageEntity stage = projectStageMapper.selectById(stageId);
        if (stage == null) {
            throw new BusinessException("阶段不存在");
        }

        // 验证项目是否存在
        ProjectEntity project = projectMapper.selectById(stage.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 验证是否是项目负责人
        if (!project.getUserId().equals(userId)) {
            throw new BusinessException("只有项目负责人可以更新阶段状态");
        }

        // 更新阶段状态
        stage.setStatus(dto.getStatus());
        stage.setUpdateTime(EntityHelper.now());

        // 如果状态改为 IN_PROGRESS，记录 start_time
        if ("IN_PROGRESS".equals(dto.getStatus()) && stage.getStartTime() == null) {
            stage.setStartTime(EntityHelper.now());
        }

        // 如果状态改为 COMPLETED，记录 complete_time
        if ("COMPLETED".equals(dto.getStatus())) {
            stage.setCompleteTime(EntityHelper.now());
        }

        // 如果从 COMPLETED 回退，清空 complete_time
        if (!"COMPLETED".equals(dto.getStatus())) {
            stage.setCompleteTime(null);
        }

        projectStageMapper.updateById(stage);
    }

    @Override
    public List<StageGuidanceVo> getStageGuidance(String stageId) {
        // 验证阶段是否存在
        ProjectStageEntity stage = projectStageMapper.selectById(stageId);
        if (stage == null) {
            throw new BusinessException("阶段不存在");
        }

        // 查询该阶段的所有指导记录
        LambdaQueryWrapper<ProjectCommentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectCommentEntity::getStageId, stageId)
                .orderByDesc(ProjectCommentEntity::getCreateTime);
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(wrapper);

        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询教师信息
        List<String> teacherIds = comments.stream()
                .map(ProjectCommentEntity::getTeacherId)
                .distinct()
                .collect(Collectors.toList());
        List<TeacherEntity> teachers = teacherMapper.selectBatchIds(teacherIds);
        Map<String, TeacherEntity> teacherMap = teachers.stream()
                .collect(Collectors.toMap(TeacherEntity::getTeacherId, t -> t));

        // 批量查询用户信息（获取头像）
        List<String> userIds = teachers.stream()
                .map(TeacherEntity::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<UserEntity> users = userMapper.selectBatchIds(userIds);
        Map<String, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<StageGuidanceVo> voList = new ArrayList<>();
        for (ProjectCommentEntity comment : comments) {
            StageGuidanceVo vo = new StageGuidanceVo();
            vo.setId(comment.getId());
            vo.setTeacherId(comment.getTeacherId());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime() != null ? sdf.format(comment.getCreateTime()) : null);

            TeacherEntity teacher = teacherMap.get(comment.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getName());
                UserEntity user = userMap.get(teacher.getUserId());
                if (user != null) {
                    vo.setTeacherAvatar(user.getAvatar());
                }
            }

            voList.add(vo);
        }

        return voList;
    }
}
