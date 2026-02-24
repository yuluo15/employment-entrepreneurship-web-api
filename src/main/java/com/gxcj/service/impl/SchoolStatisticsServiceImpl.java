package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.school.*;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.SchoolStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SchoolStatisticsServiceImpl implements SchoolStatisticsService {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private JobDeliveryMapper deliveryMapper;
    
    @Autowired
    private JobMapper jobMapper;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public SchoolEmploymentStatsVo getEmploymentStats(Integer graduationYear, String collegeName, String userId) {
        log.info("开始统计学校端就业数据: graduationYear={}, collegeName={}", graduationYear, collegeName);
        
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        SchoolEmploymentStatsVo vo = new SchoolEmploymentStatsVo();
        
        // 1. KPI指标
        vo.setKpi(getKpiData(schoolId, graduationYear, collegeName));
        
        // 2. 学院列表
        vo.setCollegeList(getCollegeList(schoolId, graduationYear));
        
        // 3. 各学院就业率统计
        vo.setCollegeStats(getCollegeStats(schoolId, graduationYear, collegeName));
        
        // 4. 各专业就业率统计 TOP10
        vo.setMajorStats(getMajorStats(schoolId, graduationYear, collegeName));
        
        // 5. 就业去向 TOP10
        vo.setTopCompanies(getTopCompanies(schoolId, graduationYear, collegeName));
        
        // 6. 薪资分布
        vo.setSalaryDistribution(getSalaryDistribution(schoolId, graduationYear, collegeName));
        
        // 7. 行业分布 TOP8
        vo.setIndustryDistribution(getIndustryDistribution(schoolId, graduationYear, collegeName));
        
        // 8. 月度就业趋势（最近6个月）
        vo.setMonthlyTrend(getMonthlyTrend(schoolId, graduationYear, collegeName));
        
        return vo;
    }

    @Override
    public SchoolEntrepreneurshipStatsVo getEntrepreneurshipStats(String collegeName, String userId) {
        log.info("开始统计学校端创业数据: collegeName={}", collegeName);
        
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        SchoolEntrepreneurshipStatsVo vo = new SchoolEntrepreneurshipStatsVo();
        
        // 1. KPI指标
        vo.setKpi(getEntrepKpiData(schoolId, collegeName));
        
        // 2. 学院列表
        vo.setCollegeList(getEntrepCollegeList(schoolId));
        
        // 3. 各学院项目数量统计
        vo.setCollegeStats(getEntrepCollegeStats(schoolId, collegeName));
        
        // 4. 项目状态分布
        vo.setStatusDistribution(getStatusDistribution(schoolId, collegeName));
        
        // 5. 项目领域分布
        vo.setDomainDistribution(getDomainDistribution(schoolId, collegeName));
        
        // 6. 团队规模分布
        vo.setTeamSizeDistribution(getTeamSizeDistribution(schoolId, collegeName));
        
        // 7. 月度项目趋势（最近6个月）
        vo.setMonthlyTrend(getEntrepMonthlyTrend(schoolId, collegeName));
        
        // 8. 成功率统计
        vo.setSuccessStats(getSuccessStats(schoolId, collegeName));
        
        return vo;
    }

    // ==================== 创业统计私有方法 ====================

    /**
     * 获取创业KPI指标数据
     */
    private SchoolEntrepreneurshipKpiVo getEntrepKpiData(String schoolId, String collegeName) {
        List<ProjectEntity> projects = getProjectList(schoolId, collegeName);
        
        int totalProjects = projects.size();
        int incubatingCount = 0;
        int pendingCount = 0;
        int totalJobs = 0;
        
        for (ProjectEntity project : projects) {
            if ("1".equals(project.getStatus())) {
                incubatingCount++;
            }
            if ("0".equals(project.getStatus())) {
                pendingCount++;
            }
            if (project.getJobsCreated() != null) {
                totalJobs += project.getJobsCreated();
            }
        }
        
        // 计算孵化率
        String incubatingRate = "0.0";
        if (totalProjects > 0) {
            double rate = Math.round(incubatingCount * 1000.0 / totalProjects) / 10.0;
            incubatingRate = String.format("%.1f", rate);
        }
        
        SchoolEntrepreneurshipKpiVo kpi = new SchoolEntrepreneurshipKpiVo();
        kpi.setTotalProjects(totalProjects);
        kpi.setIncubatingCount(incubatingCount);
        kpi.setIncubatingRate(incubatingRate);
        kpi.setPendingCount(pendingCount);
        kpi.setTotalJobs(totalJobs);
        
        return kpi;
    }

    /**
     * 获取创业项目的学院列表
     */
    private List<String> getEntrepCollegeList(String schoolId) {
        // 获取所有项目
        List<ProjectEntity> projects = projectMapper.selectList(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getSchoolId, schoolId)
        );
        
        // 获取项目创建者的学院信息
        Set<String> collegeSet = new HashSet<>();
        for (ProjectEntity project : projects) {
            StudentEntity student = studentMapper.selectOne(
                    new LambdaQueryWrapper<StudentEntity>()
                            .eq(StudentEntity::getUserId, project.getUserId())
            );
            if (student != null && StringUtils.isNotBlank(student.getCollegeName())) {
                collegeSet.add(student.getCollegeName());
            }
        }
        
        return collegeSet.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 获取各学院项目数量统计
     */
    private List<SchoolEntrepCollegeStatsVo> getEntrepCollegeStats(String schoolId, String collegeName) {
        List<ProjectEntity> projects = getProjectList(schoolId, collegeName);
        
        // 按学院分组统计
        Map<String, Integer> collegeMap = new HashMap<>();
        
        for (ProjectEntity project : projects) {
            StudentEntity student = studentMapper.selectOne(
                    new LambdaQueryWrapper<StudentEntity>()
                            .eq(StudentEntity::getUserId, project.getUserId())
            );
            if (student != null && StringUtils.isNotBlank(student.getCollegeName())) {
                String college = student.getCollegeName();
                collegeMap.put(college, collegeMap.getOrDefault(college, 0) + 1);
            }
        }
        
        // 转换为VO列表
        List<SchoolEntrepCollegeStatsVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : collegeMap.entrySet()) {
            SchoolEntrepCollegeStatsVo vo = new SchoolEntrepCollegeStatsVo();
            vo.setCollegeName(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        // 按项目数量降序排列
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        
        return result;
    }

    /**
     * 获取项目状态分布
     */
    private List<SchoolEntrepStatusDistributionVo> getStatusDistribution(String schoolId, String collegeName) {
        List<ProjectEntity> projects = getProjectList(schoolId, collegeName);
        
        // 统计各状态的项目数量
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        statusMap.put("待审核", 0);
        statusMap.put("孵化中", 0);
        statusMap.put("已驳回", 0);
        statusMap.put("已落地", 0);
        
        for (ProjectEntity project : projects) {
            String status = project.getStatus();
            if ("0".equals(status)) {
                statusMap.put("待审核", statusMap.get("待审核") + 1);
            } else if ("1".equals(status)) {
                statusMap.put("孵化中", statusMap.get("孵化中") + 1);
            } else if ("2".equals(status)) {
                statusMap.put("已驳回", statusMap.get("已驳回") + 1);
            } else if ("3".equals(status)) {
                statusMap.put("已落地", statusMap.get("已落地") + 1);
            }
        }
        
        // 转换为VO列表
        List<SchoolEntrepStatusDistributionVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            SchoolEntrepStatusDistributionVo vo = new SchoolEntrepStatusDistributionVo();
            vo.setStatusName(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 获取项目领域分布
     */
    private List<SchoolEntrepDomainDistributionVo> getDomainDistribution(String schoolId, String collegeName) {
        List<ProjectEntity> projects = getProjectList(schoolId, collegeName);
        
        // 按领域统计
        Map<String, Integer> domainMap = new HashMap<>();
        
        for (ProjectEntity project : projects) {
            if (StringUtils.isNotBlank(project.getDomain())) {
                domainMap.put(project.getDomain(), 
                        domainMap.getOrDefault(project.getDomain(), 0) + 1);
            }
        }
        
        // 转换为VO列表
        List<SchoolEntrepDomainDistributionVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : domainMap.entrySet()) {
            SchoolEntrepDomainDistributionVo vo = new SchoolEntrepDomainDistributionVo();
            vo.setDomain(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        // 按项目数量降序排列
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        
        return result;
    }

    /**
     * 获取团队规模分布
     */
    private List<SchoolEntrepTeamSizeDistributionVo> getTeamSizeDistribution(String schoolId, String collegeName) {
        List<ProjectEntity> projects = getProjectList(schoolId, collegeName);
        
        // 统计各规模区间的项目数量
        Map<String, Integer> sizeMap = new LinkedHashMap<>();
        sizeMap.put("1-3人", 0);
        sizeMap.put("4-6人", 0);
        sizeMap.put("7-10人", 0);
        sizeMap.put("10人以上", 0);
        
        for (ProjectEntity project : projects) {
            Integer teamSize = project.getTeamSize();
            if (teamSize == null) {
                teamSize = 1;
            }
            
            if (teamSize <= 3) {
                sizeMap.put("1-3人", sizeMap.get("1-3人") + 1);
            } else if (teamSize <= 6) {
                sizeMap.put("4-6人", sizeMap.get("4-6人") + 1);
            } else if (teamSize <= 10) {
                sizeMap.put("7-10人", sizeMap.get("7-10人") + 1);
            } else {
                sizeMap.put("10人以上", sizeMap.get("10人以上") + 1);
            }
        }
        
        // 转换为VO列表
        List<SchoolEntrepTeamSizeDistributionVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sizeMap.entrySet()) {
            SchoolEntrepTeamSizeDistributionVo vo = new SchoolEntrepTeamSizeDistributionVo();
            vo.setRange(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 获取月度项目趋势（最近6个月）
     */
    private List<SchoolEntrepMonthlyTrendVo> getEntrepMonthlyTrend(String schoolId, String collegeName) {
        // 计算6个月前的日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        Date sixMonthsAgo = cal.getTime();
        
        // 获取最近6个月的项目
        List<ProjectEntity> allProjects = getProjectList(schoolId, collegeName);
        List<ProjectEntity> recentProjects = allProjects.stream()
                .filter(p -> p.getCreateTime() != null && p.getCreateTime().after(sixMonthsAgo))
                .collect(Collectors.toList());
        
        // 按月份统计
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Map<String, SchoolEntrepMonthlyTrendVo> monthMap = new TreeMap<>();
        
        for (ProjectEntity project : recentProjects) {
            if (project.getCreateTime() != null) {
                String month = sdf.format(project.getCreateTime());
                
                SchoolEntrepMonthlyTrendVo vo = monthMap.get(month);
                if (vo == null) {
                    vo = new SchoolEntrepMonthlyTrendVo();
                    vo.setMonth(month);
                    vo.setNewCount(0);
                    vo.setApprovedCount(0);
                    monthMap.put(month, vo);
                }
                
                vo.setNewCount(vo.getNewCount() + 1);
                
                // 统计审核通过的项目（孵化中或已落地）
                if ("1".equals(project.getStatus()) || "3".equals(project.getStatus())) {
                    vo.setApprovedCount(vo.getApprovedCount() + 1);
                }
            }
        }
        
        return new ArrayList<>(monthMap.values());
    }

    /**
     * 获取成功率统计
     */
    private SchoolEntrepSuccessStatsVo getSuccessStats(String schoolId, String collegeName) {
        List<ProjectEntity> projects = getProjectList(schoolId, collegeName);
        
        int approvedCount = 0;  // 审核通过项目数（孵化中 + 已落地）
        int submittedCount = 0; // 提交审核项目数（非待审核）
        int landedCount = 0;    // 已落地项目数
        int totalTeamSize = 0;  // 团队规模总和
        
        for (ProjectEntity project : projects) {
            String status = project.getStatus();
            
            // 统计提交审核的项目（非待审核）
            if (!"0".equals(status)) {
                submittedCount++;
            }
            
            // 统计审核通过的项目
            if ("1".equals(status) || "3".equals(status)) {
                approvedCount++;
            }
            
            // 统计已落地的项目
            if ("3".equals(status)) {
                landedCount++;
            }
            
            // 累计团队规模
            if (project.getTeamSize() != null) {
                totalTeamSize += project.getTeamSize();
            } else {
                totalTeamSize += 1;
            }
        }
        
        // 计算审核通过率
        String approvalRate = "0.0";
        if (submittedCount > 0) {
            double rate = Math.round(approvedCount * 1000.0 / submittedCount) / 10.0;
            approvalRate = String.format("%.1f", rate);
        }
        
        // 计算项目落地率
        String landingRate = "0.0";
        if (approvedCount > 0) {
            double rate = Math.round(landedCount * 1000.0 / approvedCount) / 10.0;
            landingRate = String.format("%.1f", rate);
        }
        
        // 计算平均团队规模
        Double avgTeamSize = 0.0;
        if (projects.size() > 0) {
            avgTeamSize = Math.round(totalTeamSize * 10.0 / projects.size()) / 10.0;
        }
        
        SchoolEntrepSuccessStatsVo stats = new SchoolEntrepSuccessStatsVo();
        stats.setApprovalRate(approvalRate);
        stats.setApprovedCount(approvedCount);
        stats.setSubmittedCount(submittedCount);
        stats.setLandingRate(landingRate);
        stats.setLandedCount(landedCount);
        stats.setAvgTeamSize(avgTeamSize);
        
        return stats;
    }

    /**
     * 获取项目列表（带学院筛选）
     */
    private List<ProjectEntity> getProjectList(String schoolId, String collegeName) {
        // 先获取所有本校的项目
        List<ProjectEntity> allProjects = projectMapper.selectList(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getSchoolId, schoolId)
        );
        
        // 如果没有学院筛选，直接返回
        if (StringUtils.isBlank(collegeName)) {
            return allProjects;
        }
        
        // 根据学院筛选
        List<ProjectEntity> filteredProjects = new ArrayList<>();
        for (ProjectEntity project : allProjects) {
            StudentEntity student = studentMapper.selectOne(
                    new LambdaQueryWrapper<StudentEntity>()
                            .eq(StudentEntity::getUserId, project.getUserId())
            );
            if (student != null && collegeName.equals(student.getCollegeName())) {
                filteredProjects.add(project);
            }
        }
        
        return filteredProjects;
    }

    // ==================== 就业统计私有方法 ====================

    /**
     * 获取KPI指标数据
     */
    private SchoolEmploymentStatsKpiVo getKpiData(String schoolId, Integer graduationYear, String collegeName) {
        // 查询学生列表
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        
        int totalGraduates = students.size();
        int employedCount = 0;
        
        for (StudentEntity student : students) {
            String status = student.getEmploymentStatus() != null ? student.getEmploymentStatus() : "UNEMPLOYED";
            if ("SIGNED".equals(status)) {
                employedCount++;
            }
        }
        
        // 计算就业率
        String employmentRate = "0.0";
        if (totalGraduates > 0) {
            double rate = Math.round(employedCount * 1000.0 / totalGraduates) / 10.0;
            employmentRate = String.format("%.1f", rate);
        }
        
        // 统计创业项目
        LambdaQueryWrapper<ProjectEntity> projectWrapper = new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getSchoolId, schoolId);
        int entrepreneurshipCount = projectMapper.selectCount(projectWrapper).intValue();
        
        LambdaQueryWrapper<ProjectEntity> incubatingWrapper = new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getSchoolId, schoolId)
                .eq(ProjectEntity::getStatus, "1");
        int incubatingCount = projectMapper.selectCount(incubatingWrapper).intValue();
        
        // 计算平均薪资
        String avgSalary = calculateAvgSalary(schoolId, graduationYear, collegeName);
        
        SchoolEmploymentStatsKpiVo kpi = new SchoolEmploymentStatsKpiVo();
        kpi.setTotalGraduates(totalGraduates);
        kpi.setEmployedCount(employedCount);
        kpi.setEmploymentRate(employmentRate);
        kpi.setEntrepreneurshipCount(entrepreneurshipCount);
        kpi.setIncubatingCount(incubatingCount);
        kpi.setAvgSalary(avgSalary);
        
        return kpi;
    }

    /**
     * 获取学院列表
     */
    private List<String> getCollegeList(String schoolId, Integer graduationYear) {
        List<StudentEntity> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getSchoolId, schoolId)
                        .eq(StudentEntity::getGraduationYear, graduationYear)
                        .isNotNull(StudentEntity::getCollegeName)
        );
        
        return students.stream()
                .map(StudentEntity::getCollegeName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取各学院就业率统计
     */
    private List<SchoolCollegeStatsVo> getCollegeStats(String schoolId, Integer graduationYear, String collegeName) {
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        
        // 按学院分组统计
        Map<String, List<StudentEntity>> collegeMap = students.stream()
                .filter(s -> StringUtils.isNotBlank(s.getCollegeName()))
                .collect(Collectors.groupingBy(StudentEntity::getCollegeName));
        
        List<SchoolCollegeStatsVo> result = new ArrayList<>();
        
        for (Map.Entry<String, List<StudentEntity>> entry : collegeMap.entrySet()) {
            String college = entry.getKey();
            List<StudentEntity> collegeStudents = entry.getValue();
            
            int totalCount = collegeStudents.size();
            int employedCount = 0;
            
            for (StudentEntity student : collegeStudents) {
                String status = student.getEmploymentStatus() != null ? student.getEmploymentStatus() : "0";
                if ("1".equals(status)) {
                    employedCount++;
                }
            }
            
            double employmentRate = 0.0;
            if (totalCount > 0) {
                employmentRate = Math.round(employedCount * 1000.0 / totalCount) / 10.0;
            }
            
            SchoolCollegeStatsVo vo = new SchoolCollegeStatsVo();
            vo.setCollegeName(college);
            vo.setTotalCount(totalCount);
            vo.setEmployedCount(employedCount);
            vo.setEmploymentRate(employmentRate);
            
            result.add(vo);
        }
        
        // 按就业率降序排列
        result.sort((a, b) -> Double.compare(b.getEmploymentRate(), a.getEmploymentRate()));
        
        return result;
    }

    /**
     * 获取各专业就业率统计 TOP10
     */
    private List<SchoolMajorStatsVo> getMajorStats(String schoolId, Integer graduationYear, String collegeName) {
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        
        // 按专业分组统计
        Map<String, List<StudentEntity>> majorMap = students.stream()
                .filter(s -> StringUtils.isNotBlank(s.getMajorName()))
                .collect(Collectors.groupingBy(StudentEntity::getMajorName));
        
        List<SchoolMajorStatsVo> result = new ArrayList<>();
        
        for (Map.Entry<String, List<StudentEntity>> entry : majorMap.entrySet()) {
            String major = entry.getKey();
            List<StudentEntity> majorStudents = entry.getValue();
            
            int totalCount = majorStudents.size();
            int employedCount = 0;
            
            for (StudentEntity student : majorStudents) {
                String status = student.getEmploymentStatus() != null ? student.getEmploymentStatus() : "0";
                if ("1".equals(status)) {
                    employedCount++;
                }
            }
            
            double employmentRate = 0.0;
            if (totalCount > 0) {
                employmentRate = Math.round(employedCount * 1000.0 / totalCount) / 10.0;
            }
            
            SchoolMajorStatsVo vo = new SchoolMajorStatsVo();
            vo.setMajorName(major);
            vo.setTotalCount(totalCount);
            vo.setEmployedCount(employedCount);
            vo.setEmploymentRate(employmentRate);
            
            result.add(vo);
        }
        
        // 按就业率降序排列，取前10
        result.sort((a, b) -> Double.compare(b.getEmploymentRate(), a.getEmploymentRate()));
        
        return result.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 获取就业去向 TOP10（学生就业最多的公司）
     */
    private List<SchoolCompanyStatsVo> getTopCompanies(String schoolId, Integer graduationYear, String collegeName) {
        // 获取已就业的学生ID列表
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        List<String> employedStudentIds = students.stream()
                .filter(s -> "1".equals(s.getEmploymentStatus()))
                .map(StudentEntity::getStudentId)
                .collect(Collectors.toList());
        
        if (employedStudentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些学生的offer投递记录
        List<JobDeliveryEntity> deliveries = deliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .in(JobDeliveryEntity::getStudentId, employedStudentIds)
                        .eq(JobDeliveryEntity::getStatus, "OFFER")
        );
        
        // 按公司ID分组统计
        Map<String, Long> companyCountMap = deliveries.stream()
                .collect(Collectors.groupingBy(JobDeliveryEntity::getCompanyId, Collectors.counting()));
        
        // 获取公司信息并构建结果
        List<SchoolCompanyStatsVo> result = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : companyCountMap.entrySet()) {
            CompanyEntity company = companyMapper.selectById(entry.getKey());
            if (company != null) {
                SchoolCompanyStatsVo vo = new SchoolCompanyStatsVo();
                vo.setCompanyName(company.getName());
                vo.setCount(entry.getValue().intValue());
                result.add(vo);
            }
        }
        
        // 按人数降序排列，取前10
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        
        return result.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 获取薪资分布
     */
    private List<SchoolSalaryDistributionVo> getSalaryDistribution(String schoolId, Integer graduationYear, String collegeName) {
        // 获取已就业的学生ID列表
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        List<String> employedStudentIds = students.stream()
                .filter(s -> "1".equals(s.getEmploymentStatus()))
                .map(StudentEntity::getStudentId)
                .collect(Collectors.toList());
        
        if (employedStudentIds.isEmpty()) {
            return initEmptySalaryDistribution();
        }
        
        // 查询这些学生的offer投递记录
        List<JobDeliveryEntity> deliveries = deliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .in(JobDeliveryEntity::getStudentId, employedStudentIds)
                        .eq(JobDeliveryEntity::getStatus, "OFFER")
        );
        
        // 统计各薪资区间的人数
        Map<String, Integer> salaryMap = new LinkedHashMap<>();
        salaryMap.put("5K以下", 0);
        salaryMap.put("5-8K", 0);
        salaryMap.put("8-12K", 0);
        salaryMap.put("12-20K", 0);
        salaryMap.put("20K以上", 0);
        
        for (JobDeliveryEntity delivery : deliveries) {
            JobEntity job = jobMapper.selectById(delivery.getJobId());
            if (job != null && StringUtils.isNotBlank(job.getSalaryRange())) {
                String range = parseSalaryRange(job.getSalaryRange());
                if (salaryMap.containsKey(range)) {
                    salaryMap.put(range, salaryMap.get(range) + 1);
                }
            }
        }
        
        // 转换为VO列表
        List<SchoolSalaryDistributionVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : salaryMap.entrySet()) {
            SchoolSalaryDistributionVo vo = new SchoolSalaryDistributionVo();
            vo.setRange(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 获取行业分布 TOP8
     */
    private List<SchoolIndustryDistributionVo> getIndustryDistribution(String schoolId, Integer graduationYear, String collegeName) {
        // 获取已就业的学生ID列表
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        List<String> employedStudentIds = students.stream()
                .filter(s -> "1".equals(s.getEmploymentStatus()))
                .map(StudentEntity::getStudentId)
                .collect(Collectors.toList());
        
        if (employedStudentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些学生的offer投递记录
        List<JobDeliveryEntity> deliveries = deliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .in(JobDeliveryEntity::getStudentId, employedStudentIds)
                        .eq(JobDeliveryEntity::getStatus, "OFFER")
        );
        
        // 按行业统计
        Map<String, Integer> industryMap = new HashMap<>();
        
        for (JobDeliveryEntity delivery : deliveries) {
            CompanyEntity company = companyMapper.selectById(delivery.getCompanyId());
            if (company != null && StringUtils.isNotBlank(company.getIndustry())) {
                industryMap.put(company.getIndustry(), 
                        industryMap.getOrDefault(company.getIndustry(), 0) + 1);
            }
        }
        
        // 转换为VO列表并排序
        List<SchoolIndustryDistributionVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : industryMap.entrySet()) {
            SchoolIndustryDistributionVo vo = new SchoolIndustryDistributionVo();
            vo.setIndustry(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        // 按人数降序排列，取前8
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        
        return result.stream().limit(8).collect(Collectors.toList());
    }

    /**
     * 获取月度就业趋势（最近6个月）
     */
    private List<SchoolMonthlyTrendVo> getMonthlyTrend(String schoolId, Integer graduationYear, String collegeName) {
        // 获取已就业的学生ID列表
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        List<String> employedStudentIds = students.stream()
                .filter(s -> "1".equals(s.getEmploymentStatus()))
                .map(StudentEntity::getStudentId)
                .collect(Collectors.toList());
        
        if (employedStudentIds.isEmpty()) {
            return initEmptyMonthlyTrend();
        }
        
        // 计算6个月前的日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        Date sixMonthsAgo = cal.getTime();
        
        // 查询最近6个月的offer投递记录
        List<JobDeliveryEntity> deliveries = deliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .in(JobDeliveryEntity::getStudentId, employedStudentIds)
                        .eq(JobDeliveryEntity::getStatus, "OFFER")
                        .ge(JobDeliveryEntity::getUpdateTime, sixMonthsAgo)
        );
        
        // 按月份统计
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Map<String, Integer> monthMap = new TreeMap<>();
        
        for (JobDeliveryEntity delivery : deliveries) {
            if (delivery.getUpdateTime() != null) {
                String month = sdf.format(delivery.getUpdateTime());
                monthMap.put(month, monthMap.getOrDefault(month, 0) + 1);
            }
        }
        
        // 转换为VO列表
        List<SchoolMonthlyTrendVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthMap.entrySet()) {
            SchoolMonthlyTrendVo vo = new SchoolMonthlyTrendVo();
            vo.setMonth(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取学生列表（带筛选条件）
     */
    private List<StudentEntity> getStudentList(String schoolId, Integer graduationYear, String collegeName) {
        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getSchoolId, schoolId)
                .eq(StudentEntity::getGraduationYear, graduationYear);
        
        if (StringUtils.isNotBlank(collegeName)) {
            wrapper.eq(StudentEntity::getCollegeName, collegeName);
        }
        
        return studentMapper.selectList(wrapper);
    }

    /**
     * 计算平均薪资
     */
    private String calculateAvgSalary(String schoolId, Integer graduationYear, String collegeName) {
        List<StudentEntity> students = getStudentList(schoolId, graduationYear, collegeName);
        List<String> employedStudentIds = students.stream()
                .filter(s -> "1".equals(s.getEmploymentStatus()))
                .map(StudentEntity::getStudentId)
                .collect(Collectors.toList());
        
        if (employedStudentIds.isEmpty()) {
            return "0";
        }
        
        List<JobDeliveryEntity> deliveries = deliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .in(JobDeliveryEntity::getStudentId, employedStudentIds)
                        .eq(JobDeliveryEntity::getStatus, "OFFER")
        );
        
        double totalSalary = 0;
        int count = 0;
        
        for (JobDeliveryEntity delivery : deliveries) {
            JobEntity job = jobMapper.selectById(delivery.getJobId());
            if (job != null && StringUtils.isNotBlank(job.getSalaryRange())) {
                Double avgSalary = parseSalaryAvg(job.getSalaryRange());
                if (avgSalary != null) {
                    totalSalary += avgSalary;
                    count++;
                }
            }
        }
        
        if (count == 0) {
            return "0";
        }
        
        return String.format("%.0f", totalSalary / count);
    }

    /**
     * 解析薪资范围，返回平均值（单位：元）
     * 例如："8-12K" -> 10000
     */
    private Double parseSalaryAvg(String salaryRange) {
        if (StringUtils.isBlank(salaryRange)) {
            return null;
        }
        
        try {
            // 匹配格式：8-12K
            if (salaryRange.matches("\\d+-\\d+K")) {
                String[] parts = salaryRange.replace("K", "").split("-");
                double min = Double.parseDouble(parts[0]) * 1000;
                double max = Double.parseDouble(parts[1]) * 1000;
                return (min + max) / 2;
            }
        } catch (Exception e) {
            log.warn("解析薪资范围失败: {}", salaryRange, e);
        }
        
        return null;
    }

    /**
     * 解析薪资范围，返回所属区间
     * 例如："8-12K" -> "8-12K"
     */
    private String parseSalaryRange(String salaryRange) {
        if (StringUtils.isBlank(salaryRange)) {
            return "5K以下";
        }
        
        try {
            // 匹配格式：8-12K
            if (salaryRange.matches("\\d+-\\d+K")) {
                String[] parts = salaryRange.replace("K", "").split("-");
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                
                if (max < 5) {
                    return "5K以下";
                } else if (min >= 5 && max <= 8) {
                    return "5-8K";
                } else if (min >= 8 && max <= 12) {
                    return "8-12K";
                } else if (min >= 12 && max <= 20) {
                    return "12-20K";
                } else if (min >= 20) {
                    return "20K以上";
                }
                
                // 跨区间的情况，取中间值判断
                int avg = (min + max) / 2;
                if (avg < 5) {
                    return "5K以下";
                } else if (avg < 8) {
                    return "5-8K";
                } else if (avg < 12) {
                    return "8-12K";
                } else if (avg < 20) {
                    return "12-20K";
                } else {
                    return "20K以上";
                }
            }
        } catch (Exception e) {
            log.warn("解析薪资范围失败: {}", salaryRange, e);
        }
        
        return "5K以下";
    }

    /**
     * 初始化空的薪资分布数据
     */
    private List<SchoolSalaryDistributionVo> initEmptySalaryDistribution() {
        List<SchoolSalaryDistributionVo> result = new ArrayList<>();
        String[] ranges = {"5K以下", "5-8K", "8-12K", "12-20K", "20K以上"};
        
        for (String range : ranges) {
            SchoolSalaryDistributionVo vo = new SchoolSalaryDistributionVo();
            vo.setRange(range);
            vo.setCount(0);
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 初始化空的月度趋势数据
     */
    private List<SchoolMonthlyTrendVo> initEmptyMonthlyTrend() {
        List<SchoolMonthlyTrendVo> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            
            SchoolMonthlyTrendVo vo = new SchoolMonthlyTrendVo();
            vo.setMonth(sdf.format(cal.getTime()));
            vo.setCount(0);
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 根据用户ID获取学校ID
     */
    private String getSchoolIdByUserId(String userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        RoleEntity roleEntity = roleMapper.selectById(user.getRoleKey());
        
        if ("ROLE_SCHOOL".equals(roleEntity.getRoleName()) || "ROLE_SCHOOL_ADMIN".equals(roleEntity.getRoleName())) {
            return user.getOwnerId();
        }
        
        if ("ROLE_TEACHER".equals(roleEntity.getRoleName())) {
            TeacherEntity teacher = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>()
                    .eq(TeacherEntity::getUserId, userId));
            if (teacher != null) {
                return teacher.getSchoolId();
            }
        }
        
        throw new BusinessException("无权访问统计数据");
    }
}
