package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxcj.constant.SysConstant;
import com.gxcj.context.UserContext;
import com.gxcj.controller.student.InteractionController;
import com.gxcj.entity.*;
import com.gxcj.mapper.*;
import com.gxcj.service.InteractionService;
import com.gxcj.stutas.SearchTypeEnum;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractionServiceImpl implements InteractionService {

    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SchoolMapper schoolMapper;

    @Override
    public Boolean toggleCollection(InteractionController.CollectionReq req) {
        CollectionEntity collectionEntity = collectionMapper.selectOne(new LambdaQueryWrapper<CollectionEntity>()
                .eq(CollectionEntity::getUserId, UserContext.getUserId())
                .eq(CollectionEntity::getTargetId, req.getTargetId())
                .eq(CollectionEntity::getType, req.getType()));
        if (collectionEntity == null) {
            collectionEntity = new CollectionEntity();
            collectionEntity.setId(EntityHelper.uuid());
            collectionEntity.setUserId(UserContext.getUserId());
            collectionEntity.setTargetId(req.getTargetId());
            collectionEntity.setType(req.getType());
            if (req.getType().equals(SearchTypeEnum.JOB.name())){
                JobEntity jobEntity = jobMapper.selectById(req.getTargetId());
                collectionEntity.setTitle(jobEntity.getJobName());
                CompanyEntity companyEntity = companyMapper.selectById(jobEntity.getCompanyId());
                collectionEntity.setSubTitle(companyEntity.getName());
                collectionEntity.setImage(companyEntity.getLogo());
            } else if (req.getType().equals(SearchTypeEnum.PROJECT.name())){
                ProjectEntity projectEntity = projectMapper.selectById(req.getTargetId());
                collectionEntity.setTitle(projectEntity.getProjectName());
                collectionEntity.setImage(projectEntity.getLogo());
                SchoolEntity schoolEntity = schoolMapper.selectById(projectEntity.getSchoolId());
                collectionEntity.setSubTitle(schoolEntity.getName());
            }
            collectionMapper.insert(collectionEntity);
            return true;
        }
        collectionMapper.deleteById(collectionEntity.getId());
        return false;
    }

    @Override
    public Boolean applyJob(InteractionController.ApplyJobReq req) {

        return null;
    }
}
