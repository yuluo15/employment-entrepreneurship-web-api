package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.CompanyDashboardVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.CompanyDashboardService;
import com.gxcj.stutas.JobDeliveryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyDashboardServiceImpl implements CompanyDashboardService {

    @Autowired
    private HrMapper hrMapper;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    @Autowired
    private JobMapper jobMapper;
    
    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;
    
    @Autowired
    private InterviewMapper interviewMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public CompanyDashboardVo getDashboard(String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId)
                .eq(HrEntity::getStatus, 1));
        
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR或账号已被禁用");
        }

        String companyId = hrEntity.getCompanyId();

        // 2. 获取企业信息
        CompanyEntity company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new BusinessException("企业信息不存在");
        }

        CompanyDashboardVo dashboard = new CompanyDashboardVo();
        dashboard.setCompanyName(company.getName());
        dashboard.setCompanyLogo(company.getLogo());
        dashboard.setHrName(hrEntity.getName());

        // 3. 统计在招职位数（status=1 且 audit=1）
        Long activeJobCount = jobMapper.selectCount(new LambdaQueryWrapper<JobEntity>()
                .eq(JobEntity::getCompanyId, companyId)
                .eq(JobEntity::getStatus, 1)
                .eq(JobEntity::getAudit, 1));
        dashboard.setActiveJobCount(activeJobCount.intValue());

        // 4. 统计待处理简历数（status='DELIVERED'）
        Long pendingResumeCount = jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getCompanyId, companyId)
                .eq(JobDeliveryEntity::getStatus, JobDeliveryStatusEnum.DELIVERED.getValue()));
        dashboard.setPendingResumeCount(pendingResumeCount.intValue());

        // 5. 统计今日面试数（面试日期=今天 且 status=0）
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        Long todayInterviewCount = interviewMapper.selectCount(new LambdaQueryWrapper<InterviewEntity>()
                .eq(InterviewEntity::getCompanyId, companyId)
                .eq(InterviewEntity::getStatus, 0)
                .ge(InterviewEntity::getInterviewTime, Timestamp.valueOf(todayStart))
                .le(InterviewEntity::getInterviewTime, Timestamp.valueOf(todayEnd)));
        dashboard.setTodayInterviewCount(todayInterviewCount.intValue());

        // 6. 统计本月简历数
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        
        Long monthResumeCount = jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getCompanyId, companyId)
                .ge(JobDeliveryEntity::getCreateTime, Timestamp.valueOf(monthStart)));
        dashboard.setMonthResumeCount(monthResumeCount.intValue());

        // 7. 获取待处理简历列表（最新5条）
        dashboard.setPendingResumes(getPendingResumes(companyId));

        // 8. 获取今日面试列表
        dashboard.setTodayInterviews(getTodayInterviews(companyId, todayStart, todayEnd));

        // 9. 获取简历投递趋势（近7天）
        dashboard.setResumeTrend(getResumeTrend(companyId));

        // 10. 获取职位投递排行TOP5
        dashboard.setJobRank(getJobRank(companyId));

        return dashboard;
    }

    /**
     * 获取待处理简历列表（最新5条）
     */
    private List<CompanyDashboardVo.PendingResumeItem> getPendingResumes(String companyId) {
        List<JobDeliveryEntity> deliveries = jobDeliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .eq(JobDeliveryEntity::getCompanyId, companyId)
                        .eq(JobDeliveryEntity::getStatus, JobDeliveryStatusEnum.DELIVERED.getValue())
                        .orderByDesc(JobDeliveryEntity::getCreateTime)
                        .last("LIMIT 5")
        );

        if (deliveries.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取学生和职位信息
        List<String> studentIds = deliveries.stream()
                .map(JobDeliveryEntity::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        List<String> jobIds = deliveries.stream()
                .map(JobDeliveryEntity::getJobId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, StudentEntity> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            studentMap = studentMapper.selectList(
                    new LambdaQueryWrapper<StudentEntity>()
                            .in(StudentEntity::getStudentId, studentIds))
                    .stream()
                    .collect(Collectors.toMap(StudentEntity::getStudentId, s -> s));
        }

        Map<String, JobEntity> jobMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            jobMap = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds))
                    .stream()
                    .collect(Collectors.toMap(JobEntity::getId, j -> j));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, StudentEntity> finalStudentMap = studentMap;
        Map<String, JobEntity> finalJobMap = jobMap;
        
        return deliveries.stream().map(delivery -> {
            CompanyDashboardVo.PendingResumeItem item = new CompanyDashboardVo.PendingResumeItem();
            item.setId(delivery.getId());
            item.setCreateTime(delivery.getCreateTime() != null ? 
                    sdf.format(delivery.getCreateTime()) : null);

            StudentEntity student = finalStudentMap.get(delivery.getStudentId());
            if (student != null) {
                item.setStudentName(student.getStudentName());
                UserEntity userEntity = userMapper.selectById(student.getUserId());
                item.setStudentAvatar(userEntity.getAvatar());
            }

            JobEntity job = finalJobMap.get(delivery.getJobId());
            if (job != null) {
                item.setJobName(job.getJobName());
            }

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 获取今日面试列表
     */
    private List<CompanyDashboardVo.TodayInterviewItem> getTodayInterviews(
            String companyId, LocalDateTime todayStart, LocalDateTime todayEnd) {
        
        List<InterviewEntity> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<InterviewEntity>()
                        .eq(InterviewEntity::getCompanyId, companyId)
                        .eq(InterviewEntity::getStatus, 0)
                        .ge(InterviewEntity::getInterviewTime, Timestamp.valueOf(todayStart))
                        .le(InterviewEntity::getInterviewTime, Timestamp.valueOf(todayEnd))
                        .orderByAsc(InterviewEntity::getInterviewTime)
        );

        if (interviews.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取学生和职位信息
        List<String> studentIds = interviews.stream()
                .map(InterviewEntity::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        List<String> jobIds = interviews.stream()
                .map(InterviewEntity::getJobId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, StudentEntity> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            studentMap = studentMapper.selectList(
                    new LambdaQueryWrapper<StudentEntity>()
                            .in(StudentEntity::getStudentId, studentIds))
                    .stream()
                    .collect(Collectors.toMap(StudentEntity::getStudentId, s -> s));
        }

        Map<String, JobEntity> jobMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            jobMap = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds))
                    .stream()
                    .collect(Collectors.toMap(JobEntity::getId, j -> j));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Map<String, StudentEntity> finalStudentMap = studentMap;
        Map<String, JobEntity> finalJobMap = jobMap;
        
        return interviews.stream().map(interview -> {
            CompanyDashboardVo.TodayInterviewItem item = new CompanyDashboardVo.TodayInterviewItem();
            item.setId(interview.getId());
            item.setInterviewTime(interview.getInterviewTime() != null ? 
                    sdf.format(interview.getInterviewTime()) : null);
            item.setType(interview.getType());

            StudentEntity student = finalStudentMap.get(interview.getStudentId());
            if (student != null) {
                item.setStudentName(student.getStudentName());
            }

            JobEntity job = finalJobMap.get(interview.getJobId());
            if (job != null) {
                item.setJobName(job.getJobName());
            }

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 获取简历投递趋势（近7天）
     */
    private CompanyDashboardVo.ResumeTrend getResumeTrend(String companyId) {
        CompanyDashboardVo.ResumeTrend trend = new CompanyDashboardVo.ResumeTrend();
        
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        
        // 查询近7天的简历数据
        LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();
        List<JobDeliveryEntity> deliveries = jobDeliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .eq(JobDeliveryEntity::getCompanyId, companyId)
                        .ge(JobDeliveryEntity::getCreateTime, Timestamp.valueOf(sevenDaysAgo))
        );

        // 按日期分组统计
        Map<String, Long> dateCountMap = deliveries.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getCreateTime().toLocalDateTime().toLocalDate().format(formatter),
                        Collectors.counting()
                ));

        // 生成近7天的完整数据（包括没有数据的日期）
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            dates.add(dateStr);
            counts.add(dateCountMap.getOrDefault(dateStr, 0L).intValue());
        }

        trend.setDates(dates);
        trend.setCounts(counts);
        
        return trend;
    }

    /**
     * 获取职位投递排行TOP5
     */
    private List<CompanyDashboardVo.JobRankItem> getJobRank(String companyId) {
        // 1. 获取在招且已通过审核的职位
        List<JobEntity> activeJobs = jobMapper.selectList(
                new LambdaQueryWrapper<JobEntity>()
                        .eq(JobEntity::getCompanyId, companyId)
                        .eq(JobEntity::getStatus, 1)
                        .eq(JobEntity::getAudit, 1)
        );

        if (activeJobs.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> jobIds = activeJobs.stream()
                .map(JobEntity::getId)
                .collect(Collectors.toList());

        // 2. 统计每个职位的投递数量
        List<JobDeliveryEntity> deliveries = jobDeliveryMapper.selectList(
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .eq(JobDeliveryEntity::getCompanyId, companyId)
                        .in(JobDeliveryEntity::getJobId, jobIds)
        );

        // 3. 按职位分组统计
        Map<String, Long> jobCountMap = deliveries.stream()
                .collect(Collectors.groupingBy(
                        JobDeliveryEntity::getJobId,
                        Collectors.counting()
                ));

        // 4. 构建职位名称映射
        Map<String, String> jobNameMap = activeJobs.stream()
                .collect(Collectors.toMap(JobEntity::getId, JobEntity::getJobName));

        // 5. 排序并取TOP5
        return jobCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    CompanyDashboardVo.JobRankItem item = new CompanyDashboardVo.JobRankItem();
                    item.setJobName(jobNameMap.get(entry.getKey()));
                    item.setCount(entry.getValue().intValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
