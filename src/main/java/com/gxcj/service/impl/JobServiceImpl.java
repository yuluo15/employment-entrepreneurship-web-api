package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.controller.student.JobController;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.job.JobDetailVo;
import com.gxcj.entity.vo.job.SearchResultVo;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.JobService;
import com.gxcj.stutas.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private HrMapper hrMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DictDataMapper dictDataMapper;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public PageResult<SearchResultVo> search(JobController.SearchReq req) {
        String type = req.getType();
        if (type.equals(SearchTypeEnum.JOB.name())) {
            //整体列表
            Page<JobEntity> page = jobMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()), new LambdaQueryWrapper<JobEntity>()
                    .like(StringUtils.isNotBlank(req.getKeyword()), JobEntity::getJobName, req.getKeyword())
                    .eq(JobEntity::getStatus, StatusEnum.NORMAL.getCode()));
            if (page.getRecords() == null || page.getRecords().size() == 0) {
                return new PageResult<>();
            }
            //公司信息
            List<String> companyIds = page.getRecords().stream().map(JobEntity::getCompanyId).toList();
            List<CompanyEntity> companyEntityList = companyMapper.selectByIds(companyIds);
            Map<String, String> companyMap = companyEntityList.stream().collect(Collectors.toMap(CompanyEntity::getId,
                    companyEntity -> companyEntity.getName() + "|" + companyEntity.getScale(), (x, y) -> x));
            List<String> hrIds = page.getRecords().stream().map(JobEntity::getHrId).toList();
            List<HrEntity> hrEntityList = hrMapper.selectByIds(hrIds);
            Map<String, String> hrAvatarMap = hrEntityList.stream().collect(Collectors.toMap(HrEntity::getHrId, HrEntity::getAvatar, (x, y) -> x));
            Map<String, String> hrMap = hrEntityList.stream().collect(Collectors.toMap(HrEntity::getHrId,
                    hrEntity -> hrEntity.getName() + "·" + hrEntity.getPosition(), (x, y) -> x));
            List<DictDataEntity> sysWelfare = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                    .eq(DictDataEntity::getDictType, DictTypeEnum.sys_welfare.name()));
            Map<String, String> dictDataMap = sysWelfare.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));
            List<JobEntity> records = page.getRecords();
            List<SearchResultVo> list = records.stream().map(jobEntity -> {
                SearchResultVo searchResultVo = new SearchResultVo();
                searchResultVo.setId(jobEntity.getId());
                searchResultVo.setType(type);
                searchResultVo.setTitle(jobEntity.getJobName());
                searchResultVo.setSubTitle(companyMap.get(jobEntity.getCompanyId()));
                searchResultVo.setAvatar(hrAvatarMap.get(jobEntity.getHrId()));
                searchResultVo.setNickName(hrMap.get(jobEntity.getHrId()));
                searchResultVo.setLocation(jobEntity.getCity());
                searchResultVo.setHighlight(jobEntity.getSalaryRange());
                searchResultVo.setTags(Arrays.stream(jobEntity.getTags().split(",")).toList()
                        .stream()
                        .filter(dictDataMap::containsKey)
                        .map(dictDataMap::get).collect(Collectors.toList()));
                searchResultVo.setTargetId(jobEntity.getHrId());
                searchResultVo.setOwnerId(jobEntity.getCompanyId());
                return searchResultVo;
            }).toList();
            return new PageResult<>(page.getTotal(), list);
        } else if (type.equals(SearchTypeEnum.COMPANY.name())) {
            Page<CompanyEntity> page = companyMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()), new LambdaQueryWrapper<CompanyEntity>()
                    .like(StringUtils.isNotBlank(req.getKeyword()), CompanyEntity::getName, req.getKeyword())
                    .eq(CompanyEntity::getStatus, CompanyStatusEnum.APPROVED.getCode())
                    .eq(CompanyEntity::getIsDeleted, DeleteStatusEnum.NOT_DELETE.getCode()));
            List<DictDataEntity> dictDataList = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                    .in(DictDataEntity::getDictType,
                            DictTypeEnum.sys_industry.name(),
                            DictTypeEnum.sys_company_stage.name(),
                            DictTypeEnum.sys_company_tag.name()));
            Map<String, String> map = dictDataList.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

            List<SearchResultVo> list = page.getRecords().stream().map(companyEntity -> {
                SearchResultVo searchResultVo = new SearchResultVo();
                searchResultVo.setId(companyEntity.getId());
                searchResultVo.setType(type);
                searchResultVo.setTitle(companyEntity.getName());
                String industry = String.join(",", Arrays.stream(companyEntity.getIndustry().split(",")).toList()
                        .stream()
                        .filter(map::containsKey)
                        .map(map::get).toList());
                searchResultVo.setSubTitle(industry + "|" + companyEntity.getScale());
                searchResultVo.setAvatar(companyEntity.getLogo());
                searchResultVo.setNickName(companyEntity.getName());
                searchResultVo.setLocation(companyEntity.getAddress());

                searchResultVo.setHighlight(map.get(companyEntity.getFundingStage()));
                searchResultVo.setTags(Arrays.stream(companyEntity.getTags().split(",")).toList()
                        .stream()
                        .filter(map::containsKey)
                        .map(map::get).toList());
                searchResultVo.setTargetId(companyEntity.getId());
                searchResultVo.setOwnerId(companyEntity.getId());
                return searchResultVo;
            }).toList();
            return new PageResult<>(page.getTotal(), list);
        } else if (type.equals(SearchTypeEnum.PROJECT.name())) {
            Page<ProjectEntity> projectEntityPage = projectMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()), new LambdaQueryWrapper<ProjectEntity>()
                    .like(StringUtils.isNotBlank(req.getKeyword()), ProjectEntity::getProjectName, req.getKeyword()));
            List<String> schoolIds = projectEntityPage.getRecords().stream().map(ProjectEntity::getSchoolId).toList();
            List<SchoolEntity> schoolEntities = schoolMapper.selectByIds(schoolIds);
            Map<String, String> schoolMap = schoolEntities.stream().collect(Collectors.toMap(SchoolEntity::getId, SchoolEntity::getName, (x, y) -> x));

            List<String> userIds = projectEntityPage.getRecords().stream().map(ProjectEntity::getUserId).toList();
            List<UserEntity> userEntities = userMapper.selectByIds(userIds);
            Map<String, String> userMap = userEntities.stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getNickname, (x, y) -> x));

            List<DictDataEntity> dictDataList = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                    .in(DictDataEntity::getDictType, DictTypeEnum.sys_project_status.name(), DictTypeEnum.sys_project_domain.name()));
            Map<String, String> map = dictDataList.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

            List<SearchResultVo> list = projectEntityPage.getRecords().stream().map(projectEntity -> {
                SearchResultVo searchResultVo = new SearchResultVo();
                searchResultVo.setId(projectEntity.getProjectId());
                searchResultVo.setType(type);
                searchResultVo.setTitle(projectEntity.getProjectName());
                searchResultVo.setSubTitle(projectEntity.getMentorName() + "|" + projectEntity.getTeamSize());
                searchResultVo.setAvatar(projectEntity.getLogo());
                searchResultVo.setNickName(userMap.get(projectEntity.getUserId()));
                searchResultVo.setLocation(schoolMap.get(projectEntity.getSchoolId()));
                searchResultVo.setHighlight(map.get(projectEntity.getStatus()));
                List<String> domainList = Arrays.stream(projectEntity.getDomain().split(","))
                        .filter(map::containsKey)
                        .map(map::get).toList();
                searchResultVo.setTags(domainList);
                searchResultVo.setTargetId(projectEntity.getProjectId());
                searchResultVo.setOwnerId(projectEntity.getSchoolId());
                return searchResultVo;
            }).toList();
            return new PageResult<>(projectEntityPage.getTotal(), list);
        }
        return null;
    }

    @Override
    public JobDetailVo getJobDetail(String jobId) {
        //职位信息
        JobEntity jobEntity = jobMapper.selectById(jobId);
        JobDetailVo jobDetailVo = new JobDetailVo();
        BeanUtils.copyProperties(jobEntity, jobDetailVo);
        jobDetailVo.setTitle(jobEntity.getJobName());

        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .in(DictDataEntity::getDictType, DictTypeEnum.sys_welfare.name(), DictTypeEnum.sys_industry.name()));
        Map<String, String> map = list.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));
        jobDetailVo.setTags(Arrays.stream(jobEntity.getTags().split(","))
                .filter(map::containsKey)
                .map(map::get).toList());
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(jobEntity.getDescription())) {
            stringBuilder.append("<b>【岗位职责】</b><br/>");
            stringBuilder.append(jobEntity.getDescription());
            stringBuilder.append("<br/><br/>");
        }
        if (StringUtils.isNotEmpty(jobEntity.getRequirement())) {
            stringBuilder.append("<b>【任职要求】</b><br/>");
            stringBuilder.append(jobEntity.getRequirement());
        }
        jobDetailVo.setDescription(stringBuilder.toString());

        //关联公司信息
        CompanyEntity companyEntity = companyMapper.selectById(jobEntity.getCompanyId());
        jobDetailVo.setCompanyId(companyEntity.getId());
        jobDetailVo.setCompanyName(companyEntity.getName());
        jobDetailVo.setCompanyLogo(companyEntity.getLogo());
        jobDetailVo.setCompanyIndustry(String.join(",", Arrays.stream(companyEntity.getIndustry().split(","))
                .filter(map::containsKey)
                .map(map::get).toList()));
        jobDetailVo.setCompanyScale(companyEntity.getScale());
        jobDetailVo.setAddress(companyEntity.getAddress());
        //关联hr信息
        HrEntity hrEntity = hrMapper.selectById(jobEntity.getHrId());
        jobDetailVo.setHrId(hrEntity.getHrId());
        jobDetailVo.setHrName(hrEntity.getName());
        jobDetailVo.setHrAvatar(hrEntity.getAvatar());
        jobDetailVo.setHrTitle(hrEntity.getPosition());

        //是否收藏
        CollectionEntity collectionEntity = collectionMapper.selectOne(new LambdaQueryWrapper<CollectionEntity>()
                .eq(CollectionEntity::getUserId, UserContext.getUserId())
                .eq(CollectionEntity::getTargetId, jobEntity.getId()));
        if (collectionEntity != null) {
            jobDetailVo.setIsCollected(true);
        }
        //是否投递
        StudentEntity studentEntity = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, UserContext.getUserId()));
        JobDeliveryEntity jobDeliveryEntity = jobDeliveryMapper.selectOne(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getStudentId, studentEntity.getStudentId())
                .eq(JobDeliveryEntity::getJobId, jobEntity.getId()));
        if (jobDeliveryEntity != null) {
            jobDetailVo.setIsApplied(true);
        }
        return jobDetailVo;
    }

    @Override
    public String addJob(JobEntity jobEntity, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR，无法发布职位");
        }

        // 2. 设置职位信息
        jobEntity.setId(com.gxcj.utils.EntityHelper.uuid());
        jobEntity.setCompanyId(hrEntity.getCompanyId());
        jobEntity.setHrId(hrEntity.getHrId());

        // 3. 设置初始状态
        jobEntity.setStatus(1);        // 在招
        jobEntity.setAudit(0);         // 待审核
        jobEntity.setViewCount(0);     // 阅读量初始化为0
        jobEntity.setCreateTime(com.gxcj.utils.EntityHelper.now());

        // 4. 插入数据库
        jobMapper.insert(jobEntity);

        return jobEntity.getId();
    }

    @Override
    public void updateJob(JobEntity jobEntity, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR，无法修改职位");
        }

        // 2. 验证职位是否存在
        JobEntity existJob = jobMapper.selectById(jobEntity.getId());
        if (existJob == null) {
            throw new com.gxcj.exception.BusinessException("职位不存在");
        }

        // 3. 权限验证：只能修改自己公司的职位
        if (!existJob.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new com.gxcj.exception.BusinessException("无权限修改其他公司的职位");
        }

        // 4. 权限验证：只能修改自己发布的职位
        if (!existJob.getHrId().equals(hrEntity.getHrId())) {
            throw new com.gxcj.exception.BusinessException("只能修改自己发布的职位");
        }

        // 5. 状态限制：已下架的职位不允许修改
        if (existJob.getStatus() == 0) {
            throw new com.gxcj.exception.BusinessException("已下架的职位不允许修改");
        }

        // 6. 如果职位已被驳回，修改后需要重新审核
        if (existJob.getAudit() == 2) {
            jobEntity.setAudit(0);  // 重新审核
            jobEntity.setReason(null);  // 清空驳回原因
        }

        // 7. 更新职位信息（只更新允许修改的字段）
        JobEntity updateEntity = new JobEntity();
        updateEntity.setId(jobEntity.getId());
        updateEntity.setJobName(jobEntity.getJobName());
        updateEntity.setSalaryRange(jobEntity.getSalaryRange());
        updateEntity.setCity(jobEntity.getCity());
        updateEntity.setEducation(jobEntity.getEducation());
        updateEntity.setExperience(jobEntity.getExperience());
        updateEntity.setTags(jobEntity.getTags());
        updateEntity.setDescription(jobEntity.getDescription());
        updateEntity.setRequirement(jobEntity.getRequirement());
        updateEntity.setContactPhone(jobEntity.getContactPhone());

        // 如果是被驳回状态，设置为待审核
        if (existJob.getAudit() == 2) {
            updateEntity.setAudit(0);
            updateEntity.setReason(null);
        }

        jobMapper.updateById(updateEntity);
    }

    @Override
    public JobEntity getJobForEdit(String jobId, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR");
        }

        // 2. 查询职位信息
        JobEntity jobEntity = jobMapper.selectById(jobId);
        if (jobEntity == null) {
            throw new com.gxcj.exception.BusinessException("职位不存在");
        }

        // 3. 权限验证：只能查看自己公司的职位
        if (!jobEntity.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new com.gxcj.exception.BusinessException("无权限查看其他公司的职位");
        }

        // 4. 关联查询公司和HR信息
        CompanyEntity companyEntity = companyMapper.selectById(jobEntity.getCompanyId());
        HrEntity jobHr = hrMapper.selectById(jobEntity.getHrId());

        // 5. 设置额外信息（用于前端展示）
        // 注意：这里可以根据需要添加更多字段

        return jobEntity;
    }

    @Override
    public PageResult<JobEntity> getCompanyJobList(com.gxcj.entity.query.CompanyJobQuery query, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR");
        }

        // 2. 分页查询职位列表（只查询自己公司的职位）
        Page<JobEntity> page = jobMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<JobEntity>()
                        .eq(JobEntity::getCompanyId, hrEntity.getCompanyId())
                        .like(StringUtils.isNotEmpty(query.getJobName()),
                                JobEntity::getJobName, query.getJobName())
                        .eq(query.getAudit() != null,
                                JobEntity::getAudit, query.getAudit())
                        .eq(query.getStatus() != null,
                                JobEntity::getStatus, query.getStatus())
                        .orderByDesc(JobEntity::getCreateTime)
        );

        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public void deleteJob(String jobId, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR");
        }

        // 2. 验证职位是否存在
        JobEntity jobEntity = jobMapper.selectById(jobId);
        if (jobEntity == null) {
            throw new com.gxcj.exception.BusinessException("职位不存在");
        }

        // 3. 权限验证：只能删除自己公司的职位
        if (!jobEntity.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new com.gxcj.exception.BusinessException("无权限删除其他公司的职位");
        }

        // 4. 权限验证：只能删除自己发布的职位
        if (!jobEntity.getHrId().equals(hrEntity.getHrId())) {
            throw new com.gxcj.exception.BusinessException("只能删除自己发布的职位");
        }

        // 5. 检查是否有投递记录
        Long deliveryCount = jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getJobId, jobId));
        if (deliveryCount > 0) {
            throw new com.gxcj.exception.BusinessException("该职位已有" + deliveryCount + "条投递记录，无法删除");
        }

        // 6. 物理删除（如果需要软删除，可以改为更新 is_deleted 字段）
        jobMapper.deleteById(jobId);
    }

    @Override
    public void offlineJob(String jobId, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR");
        }

        // 2. 验证职位是否存在
        JobEntity jobEntity = jobMapper.selectById(jobId);
        if (jobEntity == null) {
            throw new com.gxcj.exception.BusinessException("职位不存在");
        }

        // 3. 权限验证：只能下架自己公司的职位
        if (!jobEntity.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new com.gxcj.exception.BusinessException("无权限下架其他公司的职位");
        }

        // 4. 状态验证：只能下架在招的职位
        if (jobEntity.getStatus() == 0) {
            throw new com.gxcj.exception.BusinessException("职位已下架，无需重复操作");
        }

        // 5. 更新状态为下架
        JobEntity updateEntity = new JobEntity();
        updateEntity.setId(jobId);
        updateEntity.setStatus(0);

        jobMapper.updateById(updateEntity);
    }

    @Override
    public void onlineJob(String jobId, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new com.gxcj.exception.BusinessException("您不是企业HR");
        }

        // 2. 验证职位是否存在
        JobEntity jobEntity = jobMapper.selectById(jobId);
        if (jobEntity == null) {
            throw new com.gxcj.exception.BusinessException("职位不存在");
        }

        // 3. 权限验证：只能上架自己公司的职位
        if (!jobEntity.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new com.gxcj.exception.BusinessException("无权限上架其他公司的职位");
        }

        // 4. 状态验证：只能上架已下架的职位
        if (jobEntity.getStatus() == 1) {
            throw new com.gxcj.exception.BusinessException("职位已上架，无需重复操作");
        }

        // 5. 审核状态验证：只能上架已通过审核的职位
        if (jobEntity.getAudit() != 1) {
            String auditMsg = jobEntity.getAudit() == 0 ? "待审核" : "已驳回";
            throw new com.gxcj.exception.BusinessException("只能上架已通过审核的职位，当前状态：" + auditMsg);
        }

        // 6. 更新状态为上架
        JobEntity updateEntity = new JobEntity();
        updateEntity.setId(jobId);
        updateEntity.setStatus(1);

        jobMapper.updateById(updateEntity);
    }
}
