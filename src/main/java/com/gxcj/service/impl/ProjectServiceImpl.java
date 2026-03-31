package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.controller.student.MobileProjectController;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.job.MyProjectVo;
import com.gxcj.entity.vo.job.ProjectDetailVo;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.ProjectService;
import com.gxcj.stutas.DictTypeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private DictDataMapper dictDataMapper;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private ProjectApplicationMapper projectApplicationMapper;

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public ProjectDetailVo getProjectDetail(String projectId) {
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .in(DictDataEntity::getDictType, DictTypeEnum.sys_project_status.name(), DictTypeEnum.sys_project_domain.name()));
        Map<String, String> map = list.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

        ProjectEntity projectEntity = projectMapper.selectById(projectId);
        ProjectDetailVo projectDetailVo = new ProjectDetailVo();
        BeanUtils.copyProperties(projectEntity,projectDetailVo);

        projectDetailVo.setTitle(projectEntity.getProjectName());

        List<String> domainList = Arrays.stream(projectEntity.getDomain().split(","))
                .filter(map::containsKey)
                .map(map::get).toList();
        projectDetailVo.setTags(domainList);

        projectDetailVo.setStatus(map.get(projectEntity.getStatus()));
        SchoolEntity schoolEntity = schoolMapper.selectById(projectEntity.getSchoolId());
        projectDetailVo.setSchoolName(schoolEntity.getName());

        UserEntity userEntity = userMapper.selectById(projectEntity.getUserId());
        projectDetailVo.setLeaderName(userEntity.getNickname());
        projectDetailVo.setLeaderPhone(userEntity.getPhone());

        CollectionEntity collectionEntity = collectionMapper.selectOne(new LambdaQueryWrapper<CollectionEntity>()
                .eq(CollectionEntity::getUserId, UserContext.getUserId())
                .eq(CollectionEntity::getTargetId, projectId));
        if (collectionEntity != null) {
            projectDetailVo.setIsCollected(true);
        }

        // 判断是否是项目负责人
        projectDetailVo.setIsOwner(projectEntity.getUserId().equals(UserContext.getUserId()));

        // 查询申请状态（需要先获取学生ID）
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, UserContext.getUserId());
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        if (student != null) {
            ProjectApplicationEntity application = projectApplicationMapper.selectOne(
                    new LambdaQueryWrapper<ProjectApplicationEntity>()
                            .eq(ProjectApplicationEntity::getProjectId, projectId)
                            .eq(ProjectApplicationEntity::getApplicantId, student.getStudentId())
            );
            if (application != null) {
                projectDetailVo.setApplicationStatus(application.getStatus());
            }
        }

        return projectDetailVo;
    }

    @Override
    public PageResult<MyProjectVo> getMyProjectList(Integer pageNum, Integer pageSize) {

        List<DictDataEntity> dataEntityList = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .in(DictDataEntity::getDictType, DictTypeEnum.sys_project_status.name(), DictTypeEnum.sys_project_domain.name()));
        Map<String, String> map = dataEntityList.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

        Page<ProjectEntity> page = projectMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getUserId, UserContext.getUserId()) // 关键：只查我的
                        .orderByDesc(ProjectEntity::getCreateTime)
        );

        List<MyProjectVo> list = page.getRecords().stream().map(p -> {
            MyProjectVo vo = new MyProjectVo();
            BeanUtils.copyProperties(p, vo);
            vo.setId(p.getProjectId());
            vo.setName(p.getProjectName());
            vo.setDomain(String.join(",", Arrays.stream(p.getDomain().split(","))
                    .filter(map::containsKey)
                    .map(map::get).toList()));
            // 状态翻译
            vo.setStatusText(map.get(p.getStatus()));

            // 如果是被驳回状态，填充驳回原因
            if (p.getStatus().equals("2")) {
                vo.setAuditReason(p.getAuditReason());
            }
            
            // 查询待审核申请数量
            Long pendingCount = projectApplicationMapper.selectCount(
                    new LambdaQueryWrapper<ProjectApplicationEntity>()
                            .eq(ProjectApplicationEntity::getProjectId, p.getProjectId())
                            .eq(ProjectApplicationEntity::getStatus, "PENDING")
            );
            vo.setPendingApplicationCount(pendingCount.intValue());
            
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), list);
    }

    @Override
    public String save(MobileProjectController.ProjectForm projectForm) {
        if (projectForm.getId() == null || projectForm.getId().isEmpty()) {
            // 新增项目
            ProjectEntity projectEntity = new ProjectEntity();
            projectEntity.setProjectId(com.gxcj.utils.EntityHelper.uuid());
            projectEntity.setUserId(UserContext.getUserId());
            projectEntity.setProjectName(projectForm.getTitle());
            projectEntity.setLogo(projectForm.getLogo());
            projectEntity.setDomain(projectForm.getDomain());
            projectEntity.setSlogan(projectForm.getSlogan());
            
            // 处理指导教师（优先使用mentorId）
            if (projectForm.getMentorId() != null && !projectForm.getMentorId().isEmpty()) {
                projectEntity.setMentorId(projectForm.getMentorId());
                // 查询教师姓名作为冗余字段
                TeacherEntity teacher = teacherMapper.selectById(projectForm.getMentorId());
                if (teacher != null) {
                    projectEntity.setMentorName(teacher.getName());
                }
            } else if (projectForm.getMentorName() != null && !projectForm.getMentorName().isEmpty()) {
                // 兼容旧版本，如果只传了mentorName
                projectEntity.setMentorName(projectForm.getMentorName());
            }
            
            projectEntity.setTeamSize(projectForm.getTeamSize() != null ? Integer.parseInt(projectForm.getTeamSize()) : null);
            projectEntity.setDescription(projectForm.getDescription());
            projectEntity.setNeeds(projectForm.getNeeds());
            
            // 获取学生的学校ID
            UserEntity userEntity = userMapper.selectById(UserContext.getUserId());
            if (userEntity != null && userEntity.getOwnerId() != null) {
                projectEntity.setSchoolId(userEntity.getOwnerId());
            }
            
            // 设置默认状态为待审核
            projectEntity.setStatus("0");
            projectEntity.setCreateTime(com.gxcj.utils.EntityHelper.now());
            projectEntity.setUpdateTime(com.gxcj.utils.EntityHelper.now());
            
            projectMapper.insert(projectEntity);
            
            // 返回新创建的项目ID
            return projectEntity.getProjectId();
        } else {
            // 更新项目
            ProjectEntity projectEntity = projectMapper.selectById(projectForm.getId());
            if (projectEntity == null) {
                throw new com.gxcj.exception.BusinessException("项目不存在");
            }
            
            // 验证是否是项目创建者
            if (!projectEntity.getUserId().equals(UserContext.getUserId())) {
                throw new com.gxcj.exception.BusinessException("无权限修改此项目");
            }
            
            projectEntity.setProjectName(projectForm.getTitle());
            projectEntity.setLogo(projectForm.getLogo());
            projectEntity.setDomain(projectForm.getDomain());
            projectEntity.setSlogan(projectForm.getSlogan());
            
            // 处理指导教师（优先使用mentorId）
            if (projectForm.getMentorId() != null && !projectForm.getMentorId().isEmpty()) {
                projectEntity.setMentorId(projectForm.getMentorId());
                // 查询教师姓名作为冗余字段
                TeacherEntity teacher = teacherMapper.selectById(projectForm.getMentorId());
                if (teacher != null) {
                    projectEntity.setMentorName(teacher.getName());
                }
            } else if (projectForm.getMentorName() != null && !projectForm.getMentorName().isEmpty()) {
                // 兼容旧版本，如果只传了mentorName
                projectEntity.setMentorName(projectForm.getMentorName());
            }
            
            projectEntity.setTeamSize(projectForm.getTeamSize() != null ? Integer.parseInt(projectForm.getTeamSize()) : null);
            projectEntity.setDescription(projectForm.getDescription());
            projectEntity.setNeeds(projectForm.getNeeds());
            projectEntity.setUpdateTime(com.gxcj.utils.EntityHelper.now());
            
            projectMapper.updateById(projectEntity);
            
            // 返回更新的项目ID
            return projectEntity.getProjectId();
        }
    }

    @Override
    public void delete(String projectId) {
        ProjectEntity projectEntity = projectMapper.selectById(projectId);
        if (projectEntity == null) {
            throw new com.gxcj.exception.BusinessException("项目不存在");
        }
        
        // 验证是否是项目创建者
        if (!projectEntity.getUserId().equals(UserContext.getUserId())) {
            throw new com.gxcj.exception.BusinessException("无权限删除此项目");
        }
        
        projectMapper.deleteById(projectId);
    }
}
