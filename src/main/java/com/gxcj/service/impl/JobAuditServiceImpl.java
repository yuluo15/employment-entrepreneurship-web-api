package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.*;
import com.gxcj.entity.query.JobAuditQuery;
import com.gxcj.entity.vo.JobAuditVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.JobAuditService;
import com.gxcj.stutas.DictTypeEnum;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobAuditServiceImpl implements JobAuditService {

    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private HrMapper hrMapper;
    @Autowired
    private DictDataMapper dictDataMapper;

    @Override
    public PageResult<JobAuditVo> getJobAuditList(JobAuditQuery query) {
        // 获取字典数据
        List<DictDataEntity> dictList = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .in(DictDataEntity::getDictType, 
                    DictTypeEnum.sys_salary_range.name(), 
                    DictTypeEnum.sys_education.name(),
                    DictTypeEnum.sys_welfare));
        Map<String, String> dictMap = dictList.stream()
                .collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

        // 构建查询条件
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getJobName()), JobEntity::getJobName, query.getJobName())
               .eq(query.getAudit() != null, JobEntity::getAudit, query.getAudit())
               .orderByDesc(JobEntity::getCreateTime);

        // 如果有公司名称查询，需要先查询公司ID
        if (StringUtils.isNotBlank(query.getCompanyName())) {
            List<CompanyEntity> companies = companyMapper.selectList(new LambdaQueryWrapper<CompanyEntity>()
                    .like(CompanyEntity::getName, query.getCompanyName()));
            if (companies.isEmpty()) {
                return new PageResult<>(0L, List.of());
            }
            List<String> companyIds = companies.stream().map(CompanyEntity::getId).collect(Collectors.toList());
            wrapper.in(JobEntity::getCompanyId, companyIds);
        }

        // 分页查询
        Page<JobEntity> page = jobMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);

        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<JobAuditVo> voList = page.getRecords().stream().map(job -> {
            JobAuditVo vo = new JobAuditVo();
            vo.setJobId(job.getId());
            vo.setJobName(job.getJobName());
            vo.setSalaryRange(job.getSalaryRange());
            vo.setSalaryRangeLabel(dictMap.getOrDefault(job.getSalaryRange(), job.getSalaryRange()));
            vo.setCity(job.getCity());
            vo.setEducation(job.getEducation());
            vo.setEducationLabel(dictMap.getOrDefault(job.getEducation(), job.getEducation()));
            vo.setExperience(job.getExperience());
//            vo.setTags(job.getTags());
            vo.setTags(String.join(",", Arrays.stream(job.getTags().split(","))
                    .filter(dictMap::containsKey)
                    .map(dictMap::get)
                    .toList()));
            vo.setDescription(job.getDescription());
            vo.setRequirement(job.getRequirement());
            vo.setAudit(job.getAudit());
            vo.setViewCount(job.getViewCount());
            vo.setContactPhone(job.getContactPhone());
            vo.setReason(job.getReason());
            vo.setCreateTime(job.getCreateTime() != null ? sdf.format(job.getCreateTime()) : null);

            // 查询公司信息
            if (StringUtils.isNotBlank(job.getCompanyId())) {
                CompanyEntity company = companyMapper.selectById(job.getCompanyId());
                if (company != null) {
                    vo.setCompanyId(company.getId());
                    vo.setCompanyName(company.getName());
                    vo.setCompanyLogo(company.getLogo());
                    vo.setCompanyIndustry(company.getIndustry());
                    vo.setLocation(company.getAddress());
                }
            }

            // 查询HR信息
            if (StringUtils.isNotBlank(job.getHrId())) {
                HrEntity hr = hrMapper.selectById(job.getHrId());
                if (hr != null) {
                    vo.setHrId(hr.getHrId());
                    vo.setHrName(hr.getName());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public void auditJob(String jobId, Integer audit, String reason) {
        JobEntity job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        // 验证审核状态
        if (audit != 1 && audit != 2) {
            throw new BusinessException("审核状态不正确");
        }

        // 如果是驳回，必须填写原因
        if (audit == 2 && StringUtils.isBlank(reason)) {
            throw new BusinessException("驳回时必须填写原因");
        }

        job.setAudit(audit);
        job.setReason(reason);
        jobMapper.updateById(job);
    }

    @Override
    public void offlineJob(String jobId) {
        JobEntity job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        // 设置状态为下架（假设0表示下架）
        job.setStatus(0);
        jobMapper.updateById(job);
    }
}
