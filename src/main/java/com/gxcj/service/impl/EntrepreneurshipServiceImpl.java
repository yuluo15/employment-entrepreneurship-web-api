package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.ProjectEntity;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.vo.EntrepProjectVo;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.DictDataMapper;
import com.gxcj.mapper.ProjectMapper;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.EntrepreneurshipService;
import com.gxcj.stutas.DictTypeEnum;
import com.gxcj.utils.EntityHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EntrepreneurshipServiceImpl implements EntrepreneurshipService {

    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DictDataMapper dictDataMapper;

    @Override
    public PageResult<EntrepProjectVo> getProjectList(Integer pageNum, Integer pageSize, 
                                                       String projectName, String schoolId, 
                                                       String domain, String status) {
        
        // 获取项目领域字典
        List<DictDataEntity> domainDict = dictDataMapper.selectList(
                new LambdaQueryWrapper<DictDataEntity>()
                        .in(DictDataEntity::getDictType, DictTypeEnum.sys_project_domain.name(), DictTypeEnum.sys_project_status.name()));
        Map<String, String> domainMap = domainDict.stream()
                .collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));
        
        // 构建查询条件
        LambdaQueryWrapper<ProjectEntity> queryWrapper = new LambdaQueryWrapper<ProjectEntity>()
                .like(StringUtils.isNotBlank(projectName), ProjectEntity::getProjectName, projectName)
                .eq(StringUtils.isNotBlank(schoolId), ProjectEntity::getSchoolId, schoolId)
                .eq(StringUtils.isNotBlank(domain), ProjectEntity::getDomain, domain)
                .eq(StringUtils.isNotBlank(status), ProjectEntity::getStatus, status)
                .orderByDesc(ProjectEntity::getCreateTime);
        
        // 分页查询
        Page<ProjectEntity> page = projectMapper.selectPage(
                new Page<>(pageNum, pageSize), queryWrapper);

        // 转换为VO
        List<EntrepProjectVo> voList = page.getRecords().stream().map(project -> {
            EntrepProjectVo vo = EntrepProjectVo.builder()
                    .projectId(project.getProjectId())
                    .projectName(project.getProjectName())
                    .logo(project.getLogo())
                    .slogan(project.getSlogan())
                    .schoolId(project.getSchoolId())
                    .mentorName(project.getMentorName())
                    .teamSize(project.getTeamSize())
                    .domain(project.getDomain())
//                    Arrays.stream(project.getDomain().split(","))
//                            .filter(domainMap::containsKey)
//                            .map(domainMap::get)
//                            .toList()
//                    .domainLabel(domainMap.getOrDefault(project.getDomain(), project.getDomain()))
                    .domainLabel(String.join(",", Arrays.stream(project.getDomain().split(","))
                            .filter(domainMap::containsKey)
                            .map(domainMap::get)
                            .toList()))
                    .status(project.getStatus())
//                    .status(domainMap.get(project.getStatus()))
                    .description(project.getDescription())
                    .needs(project.getNeeds())
                    .auditReason(project.getAuditReason())
                    .createTime(project.getCreateTime())
                    .build();
            
            // 查询学校名称
            if (StringUtils.isNotBlank(project.getSchoolId())) {
                SchoolEntity school = schoolMapper.selectById(project.getSchoolId());
                if (school != null) {
                    vo.setSchoolName(school.getName());
                }
            }
            
            // 查询负责人姓名
            if (StringUtils.isNotBlank(project.getUserId())) {
                UserEntity user = userMapper.selectById(project.getUserId());
                if (user != null) {
                    vo.setLeaderName(user.getNickname());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditProject(String projectId, Integer status, String reason) {
        // 查询项目
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 验证状态
        if (status == 2 && StringUtils.isBlank(reason)) {
            throw new BusinessException("驳回时必须填写驳回原因");
        }
        
        // 更新项目状态
        project.setStatus(status.toString());
        project.setAuditReason(reason);
        project.setAuditTime(EntityHelper.now());
        project.setUpdateTime(EntityHelper.now());
        
        int result = projectMapper.updateById(project);
        if (result <= 0) {
            throw new BusinessException("审核失败");
        }
        
        log.info("项目审核成功: projectId={}, status={}", projectId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeProject(String projectId) {
        // 查询项目
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 只有孵化中的项目才能落地
        if (!"1".equals(project.getStatus())) {
            throw new BusinessException("只有孵化中的项目才能标记为已落地");
        }
        
        // 更新为已落地状态
        project.setStatus("3");
        project.setUpdateTime(EntityHelper.now());
        
        int result = projectMapper.updateById(project);
        if (result <= 0) {
            throw new BusinessException("项目落地操作失败");
        }
        
        log.info("项目落地成功: projectId={}", projectId);
    }

    @Override
    public List<SchoolNameVo> getSchoolList() {
        List<SchoolEntity> schoolList = schoolMapper.selectList(
                new LambdaQueryWrapper<SchoolEntity>()
                        .eq(SchoolEntity::getStatus, 1)
                        .eq(SchoolEntity::getIsDeleted, 0)
                        .orderBy(true, true, SchoolEntity::getName));
        
        return schoolList.stream()
                .map(school -> SchoolNameVo.builder()
                        .id(school.getId())
                        .schoolName(school.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getDomains() {
        // 从字典表获取项目领域
        List<DictDataEntity> domainList = dictDataMapper.selectList(
                new LambdaQueryWrapper<DictDataEntity>()
                        .eq(DictDataEntity::getDictType, DictTypeEnum.sys_project_domain.name())
                        .eq(DictDataEntity::getStatus, 0)
                        .orderBy(true, true, DictDataEntity::getDictSort));
        
        // 转换为 Map<dictValue, dictLabel>
        return domainList.stream()
                .collect(Collectors.toMap(
                        DictDataEntity::getDictValue,
                        DictDataEntity::getDictLabel,
                        (existing, replacement) -> existing
                ));
    }
}
