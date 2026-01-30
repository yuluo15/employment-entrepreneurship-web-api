package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.constant.SysConstant;
import com.gxcj.controller.admin.CompanyController;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.job.CompanyDetailVo;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.CompanyService;
import com.gxcj.stutas.DeleteStatusEnum;
import com.gxcj.stutas.DictTypeEnum;
import com.gxcj.stutas.StatusEnum;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DictDataMapper dictDataMapper;
    @Autowired
    private HrMapper hrMapper;
    @Autowired
    private JobMapper jobMapper;

    public PageResult<CompanyEntity> list(Integer pageNum, Integer pageSize, String name, String code, Integer status) {
//        PageHelper.startPage(pageNum, pageSize);
        Page<CompanyEntity> page = companyMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<CompanyEntity>()
                .eq(StringUtils.isNotEmpty(name), CompanyEntity::getName, name)
                .eq(status != null, CompanyEntity::getStatus, status)
                .eq(StringUtils.isNotEmpty(code), CompanyEntity::getCode, code)
                .eq(CompanyEntity::getIsDeleted, DeleteStatusEnum.NOT_DELETE.getCode()));
//        PageInfo<CompanyEntity> companyEntityPageInfo = new PageInfo<>(companyEntityList);
//        return new PageResult<>(companyEntityPageInfo.getTotal(), companyEntityPageInfo.getList());
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

//    @Transactional
//    public void create(CompanyController.CompanyCreateReq req) {
//        String companyId = EntityHelper.uuid();
//        UserEntity userEntity = new UserEntity();
//        userEntity.setId(EntityHelper.uuid());
//        userEntity.setLoginIdentity(req.getEmail());
//        userEntity.setPassword(EntityHelper.encodedPassword(SysConstant.DEFAULT_PASSWORD));
//        userEntity.setEmail(req.getEmail());
//        userEntity.setPhone(req.getContactPhone());
//        userEntity.setRoleKey(SysConstant.ROLE_COMPANY);
//        userEntity.setOwnerId(companyId);
//        userEntity.setCreateTime(EntityHelper.now());
//        userEntity.setUpdateTime(EntityHelper.now());
//        userMapper.insert(userEntity);
//
//        CompanyEntity companyEntity = CompanyEntity.ofReq(req);
//        companyEntity.setId(companyId);
//        companyEntity.setCreateTime(EntityHelper.now());
//        companyEntity.setUpdateTime(EntityHelper.now());
//        companyEntity.setDefaultAccountId(userEntity.getId());
//        companyMapper.insert(companyEntity);
//    }

    @Override
    public void auditCompany(CompanyController.AuditCompanyReq req) {
        companyMapper.update(new LambdaUpdateWrapper<CompanyEntity>()
                .set(CompanyEntity::getStatus, req.getStatus())
                .set(StringUtils.isNotEmpty(req.getReason()), CompanyEntity::getAuditReason, req.getReason())
                .eq(CompanyEntity::getId, req.getId()));
    }

    @Override
    public void update(CompanyController.CompanyCreateReq req) {
        CompanyEntity companyEntity = CompanyEntity.ofReq(req);
        companyMapper.updateById(companyEntity);
    }

    @Override
    public void delete(String id) {
        companyMapper.update(null, new LambdaUpdateWrapper<CompanyEntity>()
                .set(CompanyEntity::getIsDeleted, DeleteStatusEnum.DELETE.getCode())
                .eq(CompanyEntity::getId, id));
    }

    @Override
    public CompanyDetailVo getCompanyDetail(String companyId) {
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .in(DictDataEntity::getDictType, DictTypeEnum.sys_industry.name(), DictTypeEnum.sys_company_stage.name()));
        Map<String, String> map = list.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (e1, e2) -> e1));

        CompanyEntity companyEntity = companyMapper.selectById(companyId);
        CompanyDetailVo companyDetailVo = new CompanyDetailVo();
        BeanUtils.copyProperties(companyEntity, companyDetailVo);
        List<String> industry = Arrays.stream(companyEntity.getIndustry().split(","))
                .filter(map::containsKey)
                .map(map::get).toList();
//        companyDetailVo.setIndustry(String.join(",", industry));
        companyDetailVo.setIndustry(industry);
        companyDetailVo.setFundingStage(map.get(companyEntity.getFundingStage()));
        int jobCount = 0;

        List<HrEntity> hrEntityList = hrMapper.selectList(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getCompanyId, companyId)
                .eq(HrEntity::getStatus, StatusEnum.NORMAL.getCode()));
        List<String> hrIds = hrEntityList.stream().map(HrEntity::getHrId).toList();
        if (!hrIds.isEmpty()) {
            Long count = jobMapper.selectCount(new LambdaQueryWrapper<JobEntity>()
                    .in(JobEntity::getHrId, hrIds));
            jobCount = Integer.parseInt(count.toString());
        }
        if (companyEntity.getImages() != null && !companyEntity.getImages().isEmpty()) {
            companyDetailVo.setImages(Arrays.stream(companyEntity.getImages().split(",")).toList());
        }
        companyDetailVo.setJobCount(jobCount);

        return companyDetailVo;
    }


}
