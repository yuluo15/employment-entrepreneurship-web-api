package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.ProjectEntity;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.vo.job.ProjectDetailVo;
import com.gxcj.mapper.DictDataMapper;
import com.gxcj.mapper.ProjectMapper;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.UserMapper;
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

//        long startTime = System.nanoTime();
//        List<ProjectEntity> projectEntities = projectMapper.selectProjectDetail(projectId);
//        long endTime = System.nanoTime();
//        System.out.println("end-time = " + (endTime - startTime)/1000000);
        return projectDetailVo;
    }
}
