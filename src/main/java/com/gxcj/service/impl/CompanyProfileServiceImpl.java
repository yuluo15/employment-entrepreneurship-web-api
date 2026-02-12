package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.controller.company.CompanyProfileController;
import com.gxcj.entity.CompanyEntity;
import com.gxcj.entity.HrEntity;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.CompanyMapper;
import com.gxcj.mapper.HrMapper;
import com.gxcj.service.CompanyProfileService;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {

    @Autowired
    private HrMapper hrMapper;
    
    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public CompanyEntity getCompanyInfo(String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId)
                .eq(HrEntity::getStatus, 1));
        
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR或账号已被禁用");
        }

        // 2. 查询企业信息
        CompanyEntity company = companyMapper.selectById(hrEntity.getCompanyId());
        if (company == null) {
            throw new BusinessException("企业信息不存在");
        }

        return company;
    }

    @Override
    public void updateCompanyInfo(CompanyProfileController.CompanyUpdateReq req, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId)
                .eq(HrEntity::getStatus, 1));
        
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR或账号已被禁用");
        }

        // 2. 验证企业ID是否匹配
        if (!req.getId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限修改其他企业的信息");
        }

        // 3. 查询原企业信息
        CompanyEntity existCompany = companyMapper.selectById(req.getId());
        if (existCompany == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 4. 检查统一社会信用代码是否重复（如果修改了）
        if (!req.getCode().equals(existCompany.getCode())) {
            Long count = companyMapper.selectCount(new LambdaQueryWrapper<CompanyEntity>()
                    .eq(CompanyEntity::getCode, req.getCode())
                    .ne(CompanyEntity::getId, req.getId()));
            
            if (count > 0) {
                throw new BusinessException("统一社会信用代码已被使用");
            }
        }

        // 5. 更新企业信息
        CompanyEntity updateEntity = new CompanyEntity();
        updateEntity.setId(req.getId());
        updateEntity.setName(req.getName());
        updateEntity.setCode(req.getCode());
        updateEntity.setIndustry(req.getIndustry());
        updateEntity.setScale(req.getScale());
        updateEntity.setFundingStage(req.getFundingStage());
        updateEntity.setWebsite(req.getWebsite());
        updateEntity.setLogo(req.getLogo());
        updateEntity.setAddress(req.getAddress());
        updateEntity.setContactPerson(req.getContactPerson());
        updateEntity.setContactPhone(req.getContactPhone());
        updateEntity.setEmail(req.getEmail());
        updateEntity.setDescription(req.getDescription());
        updateEntity.setTags(req.getTags());
        updateEntity.setImages(req.getImages());
        updateEntity.setUpdateTime(EntityHelper.now());

        // 6. 如果修改了核心信息（企业名称或统一社会信用代码），可能需要重新审核
        // 这里可以根据业务需求决定是否重置审核状态
        // if (!req.getName().equals(existCompany.getName()) || 
        //     !req.getCode().equals(existCompany.getCode())) {
        //     updateEntity.setStatus(0);  // 重置为待审核
        // }

        companyMapper.updateById(updateEntity);
    }
}
