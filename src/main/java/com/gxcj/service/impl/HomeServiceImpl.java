package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.HomeOverviewVo;
import com.gxcj.mapper.*;
import com.gxcj.service.HomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private JobMapper jobMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private HomeMapper homeMapper;

    @Override
    public HomeOverviewVo getOverview() {
        log.info("开始获取首页概览数据");
        
        // 1. 核心指标统计
        Integer schoolCount = getSchoolCount();
        Integer companyCount = getCompanyCount();
        Integer studentCount = getStudentCount();
        Integer teacherCount = getTeacherCount();
        Integer pendingJobs = getPendingJobsCount();
        Integer pendingProjects = getPendingProjectsCount();
        
        // 2. 学校排行
        List<HomeOverviewVo.SchoolRankItem> schoolRank = homeMapper.getSchoolRank();
        
        // 3. 创业领域分布
        List<HomeOverviewVo.DomainDistributionItem> domainDistribution = homeMapper.getDomainDistribution();
        
        // 4. 最新入驻企业
        List<HomeOverviewVo.LatestCompanyItem> latestCompanies = homeMapper.getLatestCompanies();
        
        // 组装返回数据
        return HomeOverviewVo.builder()
                .schoolCount(schoolCount)
                .companyCount(companyCount)
                .studentCount(studentCount)
                .teacherCount(teacherCount)
                .pendingJobs(pendingJobs)
                .pendingProjects(pendingProjects)
                .schoolRank(schoolRank != null ? schoolRank : new ArrayList<>())
                .domainDistribution(domainDistribution != null ? domainDistribution : new ArrayList<>())
                .latestCompanies(latestCompanies != null ? latestCompanies : new ArrayList<>())
                .build();
    }

    /**
     * 获取入驻学校总数
     */
    private Integer getSchoolCount() {
        Long count = schoolMapper.selectCount(
                new LambdaQueryWrapper<SchoolEntity>()
                        .eq(SchoolEntity::getIsDeleted, 0)
                        .eq(SchoolEntity::getStatus, 1));
        return count != null ? count.intValue() : 0;
    }

    /**
     * 获取入驻企业总数
     */
    private Integer getCompanyCount() {
        Long count = companyMapper.selectCount(
                new LambdaQueryWrapper<CompanyEntity>()
                        .eq(CompanyEntity::getIsDeleted, 0));
        return count != null ? count.intValue() : 0;
    }

    /**
     * 获取学生总数
     */
    private Integer getStudentCount() {
        Long count = studentMapper.selectCount(new LambdaQueryWrapper<>());
        return count != null ? count.intValue() : 0;
    }

    /**
     * 获取教师总数
     */
    private Integer getTeacherCount() {
        Long count = teacherMapper.selectCount(new LambdaQueryWrapper<>());
        return count != null ? count.intValue() : 0;
    }

    /**
     * 获取待审核岗位数量
     */
    private Integer getPendingJobsCount() {
        Long count = jobMapper.selectCount(
                new LambdaQueryWrapper<JobEntity>()
                        .eq(JobEntity::getStatus, 0));  // 0=待审核
        return count != null ? count.intValue() : 0;
    }

    /**
     * 获取待审核创业项目数量
     */
    private Integer getPendingProjectsCount() {
        Long count = projectMapper.selectCount(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getStatus, "0"));  // 0=待审核
        return count != null ? count.intValue() : 0;
    }
}
