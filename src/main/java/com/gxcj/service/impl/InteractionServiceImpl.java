package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxcj.constant.SysConstant;
import com.gxcj.context.UserContext;
import com.gxcj.controller.student.InteractionController;
import com.gxcj.entity.*;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.InteractionService;
import com.gxcj.stutas.JobDeliveryStatusEnum;
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
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private StudentResumeMapper studentResumeMapper;
    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;

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
        JobDeliveryEntity jobDeliveryEntity = new JobDeliveryEntity();
        jobDeliveryEntity.setId(EntityHelper.uuid());
        StudentEntity studentEntity = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, UserContext.getUserId()));
        if (studentEntity == null) {
            throw new BusinessException("您不是学生，无法投递简历");
        }
        jobDeliveryEntity.setStudentId(studentEntity.getStudentId());
        JobDeliveryEntity jobDelivery = jobDeliveryMapper.selectOne(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getStudentId, studentEntity.getStudentId())
                .eq(JobDeliveryEntity::getJobId, req.getJobId()));
        if (jobDelivery != null) {
            throw new BusinessException("您已经投递过简历，不能重复投递");
        }

        StudentResumeEntity studentResumeEntity = studentResumeMapper.selectOne(new LambdaQueryWrapper<StudentResumeEntity>()
                .eq(StudentResumeEntity::getStudentId, studentEntity.getStudentId()));
        if (studentResumeEntity == null) {
            throw new BusinessException("暂无简历，无法投递");
        }
        jobDeliveryEntity.setResumeId(studentResumeEntity.getResumeId());

        JobEntity jobEntity = jobMapper.selectById(req.getJobId());
        if (jobEntity == null) {
            throw new BusinessException("该职位已被下架");
        }
        jobDeliveryEntity.setJobId(jobEntity.getId());
        jobDeliveryEntity.setCompanyId(jobEntity.getCompanyId());

        jobDeliveryEntity.setStatus(JobDeliveryStatusEnum.DELIVERED.getValue());

        jobDeliveryEntity.setCreateTime(EntityHelper.now());
        jobDeliveryEntity.setUpdateTime(EntityHelper.now());

        jobDeliveryMapper.insert(jobDeliveryEntity);
        return true;
    }
}
