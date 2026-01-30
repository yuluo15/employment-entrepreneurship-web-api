package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
            return  new PageResult<>(projectEntityPage.getTotal(), list);
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
        if (StringUtils.isNotEmpty(jobEntity.getDescription())){
            stringBuilder.append("<b>【岗位职责】</b><br/>");
            stringBuilder.append(jobEntity.getDescription());
            stringBuilder.append("<br/><br/>");
        }
        if (StringUtils.isNotEmpty(jobEntity.getRequirement())){
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

        return jobDetailVo;
    }
}
