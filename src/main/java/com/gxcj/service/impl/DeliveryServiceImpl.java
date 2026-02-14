package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.controller.company.CompanyDeliveryController;
import com.gxcj.entity.*;
import com.gxcj.entity.query.DeliveryQuery;
import com.gxcj.entity.query.InterviewQuery;
import com.gxcj.entity.query.TalentQuery;
import com.gxcj.entity.vo.DeliveryVo;
import com.gxcj.entity.vo.InterviewVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.DeliveryService;
import com.gxcj.stutas.JobDeliveryStatusEnum;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;
    @Autowired
    private HrMapper hrMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private StudentResumeMapper studentResumeMapper;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private InterviewMapper interviewMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public PageResult<DeliveryVo> getDeliveryList(DeliveryQuery query, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 构建查询条件
        LambdaQueryWrapper<JobDeliveryEntity> wrapper = new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getCompanyId, hrEntity.getCompanyId())
                .eq(StringUtils.isNotEmpty(query.getJobId()), 
                    JobDeliveryEntity::getJobId, query.getJobId())
                .eq(StringUtils.isNotEmpty(query.getStatus()), 
                    JobDeliveryEntity::getStatus, query.getStatus())
                .orderByDesc(JobDeliveryEntity::getCreateTime);

        // 3. 日期范围查询
        if (StringUtils.isNotEmpty(query.getStartDate())) {
            LocalDateTime startDateTime = LocalDate.parse(query.getStartDate()).atStartOfDay();
            wrapper.ge(JobDeliveryEntity::getCreateTime, Timestamp.valueOf(startDateTime));
        }
        if (StringUtils.isNotEmpty(query.getEndDate())) {
            LocalDateTime endDateTime = LocalDate.parse(query.getEndDate()).atTime(LocalTime.MAX);
            wrapper.le(JobDeliveryEntity::getCreateTime, Timestamp.valueOf(endDateTime));
        }

        // 4. 分页查询
        Page<JobDeliveryEntity> page = jobDeliveryMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }

        // 5. 关联查询学生、职位、简历信息
        List<String> studentIds = page.getRecords().stream()
                .map(JobDeliveryEntity::getStudentId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        List<String> jobIds = page.getRecords().stream()
                .map(JobDeliveryEntity::getJobId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        List<String> resumeIds = page.getRecords().stream()
                .map(JobDeliveryEntity::getResumeId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());

        // 使用 LambdaQueryWrapper 代替 selectBatchIds 避免空列表问题
        Map<String, StudentEntity> studentMap;
        if (!studentIds.isEmpty()) {
            List<StudentEntity> students = studentMapper.selectList(
                    new LambdaQueryWrapper<StudentEntity>()
                            .in(StudentEntity::getStudentId, studentIds));
            studentMap = students.stream()
                    .collect(Collectors.toMap(StudentEntity::getStudentId, s -> s));
        } else {
            studentMap = new HashMap<>();
        }

        Map<String, JobEntity> jobMap;
        if (!jobIds.isEmpty()) {
            List<JobEntity> jobs = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds));
            jobMap = jobs.stream()
                    .collect(Collectors.toMap(JobEntity::getId, j -> j));
        } else {
            jobMap = new HashMap<>();
        }

        Map<String, StudentResumeEntity> resumeMap;
        if (!resumeIds.isEmpty()) {
            List<StudentResumeEntity> resumes = studentResumeMapper.selectList(
                    new LambdaQueryWrapper<StudentResumeEntity>()
                            .in(StudentResumeEntity::getResumeId, resumeIds));
            resumeMap = resumes.stream()
                    .collect(Collectors.toMap(StudentResumeEntity::getResumeId, r -> r));
        } else {
            resumeMap = new HashMap<>();
        }

        // 6. 按学生姓名过滤（如果有）
        List<DeliveryVo> voList = page.getRecords().stream()
                .map(delivery -> {
                    StudentEntity student = studentMap.get(delivery.getStudentId());
                    JobEntity job = jobMap.get(delivery.getJobId());
                    StudentResumeEntity resume = resumeMap.get(delivery.getResumeId());

                    // 学生姓名过滤
                    if (StringUtils.isNotEmpty(query.getStudentName()) 
                        && student != null 
                        && !student.getStudentName().contains(query.getStudentName())) {
                        return null;
                    }

                    DeliveryVo vo = new DeliveryVo();
                    vo.setId(delivery.getId());
                    vo.setStudentId(delivery.getStudentId());
                    vo.setJobId(delivery.getJobId());
                    vo.setResumeId(delivery.getResumeId());
                    vo.setStatus(delivery.getStatus());
                    vo.setDeliveryTime(delivery.getCreateTime() != null ? 
                            delivery.getCreateTime().toString() : null);

                    if (student != null) {
                        vo.setStudentName(student.getStudentName());
                        vo.setStudentPhone(student.getPhone());
                        vo.setEmail(student.getEmail());
//                        vo.setGender(student.getGender());
                        vo.setGender(1);
                        vo.setMajor(student.getMajorName());
                        vo.setGraduationYear(student.getGraduationYear());
                        vo.setEducation(student.getEducation());
                        
                        // 获取学校名称
                        if (student.getSchoolId() != null) {
                            SchoolEntity school = schoolMapper.selectById(student.getSchoolId());
                            if (school != null) {
                                vo.setSchool(school.getName());
                            }
                        }
                    }

                    if (job != null) {
                        vo.setJobName(job.getJobName());
                    }

                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public StudentResumeEntity getResumeDetail(String resumeId, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询简历
        StudentResumeEntity resume = studentResumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }

        // 3. 验证权限：该简历是否投递给了当前公司
        Long count = jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getResumeId, resumeId)
                .eq(JobDeliveryEntity::getCompanyId, hrEntity.getCompanyId()));
        
        if (count == 0) {
            throw new BusinessException("无权限查看该简历");
        }

        return resume;
    }

    @Override
    public String arrangeInterview(CompanyDeliveryController.InterviewArrangeReq req, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询投递记录
        JobDeliveryEntity delivery = jobDeliveryMapper.selectById(req.getDeliveryId());
        if (delivery == null) {
            throw new BusinessException("投递记录不存在");
        }

        // 3. 权限验证
        if (!delivery.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限操作该投递记录");
        }

        // 4. 状态验证
        if (!JobDeliveryStatusEnum.DELIVERED.name().equals(delivery.getStatus())) {
            throw new BusinessException("只能为待处理的简历安排面试");
        }

        // 5. 解析面试时间
        LocalDateTime interviewTime;
        try {
            interviewTime = LocalDateTime.parse(req.getInterviewTime(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new BusinessException("面试时间格式不正确，应为：yyyy-MM-dd HH:mm:ss");
        }

        // 6. 验证面试时间必须晚于当前时间
        if (interviewTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("面试时间必须晚于当前时间");
        }

        // 7. 创建面试记录
        InterviewEntity interview = new InterviewEntity();
        interview.setId(EntityHelper.uuid());
        interview.setDeliveryId(req.getDeliveryId());
        interview.setCompanyId(hrEntity.getCompanyId());
        interview.setStudentId(delivery.getStudentId());
        interview.setJobId(delivery.getJobId());
        interview.setHrId(hrEntity.getHrId());
        interview.setInterviewTime(Timestamp.valueOf(interviewTime));
        interview.setDuration(req.getDuration());
        
        // 转换面试方式
        if ("ONSITE".equals(req.getType())) {
            interview.setType(1);
        } else if ("VIDEO".equals(req.getType())) {
            interview.setType(2);
        } else if ("PHONE".equals(req.getType())) {
            interview.setType(3);
        } else {
            throw new BusinessException("面试方式不正确");
        }
        
        interview.setLocation(req.getLocation());
        interview.setNotes(req.getNotes());
        interview.setStatus(0);  // 待面试
        interview.setCreateTime(EntityHelper.now());
        interview.setUpdateTime(EntityHelper.now());
        
        interviewMapper.insert(interview);

        // 8. 更新投递状态
        delivery.setStatus(JobDeliveryStatusEnum.INTERVIEW.getValue());
        delivery.setUpdateTime(EntityHelper.now());
        jobDeliveryMapper.updateById(delivery);

        // 9. 发送消息通知学生
        sendMessage(delivery.getStudentId(), "面试通知", 
                String.format("您投递的职位已安排面试，面试时间：%s", req.getInterviewTime()),
                2, interview.getId());

        return interview.getId();
    }

    @Override
    public void rejectDelivery(String deliveryId, String reason, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询投递记录
        JobDeliveryEntity delivery = jobDeliveryMapper.selectById(deliveryId);
        if (delivery == null) {
            throw new BusinessException("投递记录不存在");
        }

        // 3. 权限验证
        if (!delivery.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限操作该投递记录");
        }

        String value = JobDeliveryStatusEnum.DELIVERED.getValue();
        // 4. 状态验证
        if (!JobDeliveryStatusEnum.DELIVERED.name().equals(delivery.getStatus())) {
            throw new BusinessException("只能拒绝待处理的简历");
        }

        // 5. 更新投递状态
        delivery.setStatus(JobDeliveryStatusEnum.REJECT.getValue());
        delivery.setHandleReply(reason);
        delivery.setUpdateTime(EntityHelper.now());
        
        jobDeliveryMapper.updateById(delivery);
    }


    @Override
    public PageResult<InterviewVo> getInterviewList(InterviewQuery query, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 构建查询条件
        LambdaQueryWrapper<InterviewEntity> wrapper = new LambdaQueryWrapper<InterviewEntity>()
                .eq(InterviewEntity::getCompanyId, hrEntity.getCompanyId())
                .eq(query.getStatus() != null, InterviewEntity::getStatus, query.getStatus())
                .orderByDesc(InterviewEntity::getInterviewTime);

        // 3. 日期范围查询
        if (StringUtils.isNotEmpty(query.getStartDate())) {
            LocalDateTime startDateTime = LocalDate.parse(query.getStartDate()).atStartOfDay();
            wrapper.ge(InterviewEntity::getInterviewTime, Timestamp.valueOf(startDateTime));
        }
        if (StringUtils.isNotEmpty(query.getEndDate())) {
            LocalDateTime endDateTime = LocalDate.parse(query.getEndDate()).atTime(LocalTime.MAX);
            wrapper.le(InterviewEntity::getInterviewTime, Timestamp.valueOf(endDateTime));
        }

        // 4. 分页查询
        Page<InterviewEntity> page = interviewMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }

        // 5. 关联查询学生、职位信息
        List<String> studentIds = page.getRecords().stream()
                .map(InterviewEntity::getStudentId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        List<String> jobIds = page.getRecords().stream()
                .map(InterviewEntity::getJobId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());

        Map<String, StudentEntity> studentMap;
        if (!studentIds.isEmpty()) {
            List<StudentEntity> students = studentMapper.selectList(
                    new LambdaQueryWrapper<StudentEntity>()
                            .in(StudentEntity::getStudentId, studentIds));
            studentMap = students.stream()
                    .collect(Collectors.toMap(StudentEntity::getStudentId, s -> s));
        } else {
            studentMap = new HashMap<>();
        }

        Map<String, JobEntity> jobMap;
        if (!jobIds.isEmpty()) {
            List<JobEntity> jobs = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds));
            jobMap = jobs.stream()
                    .collect(Collectors.toMap(JobEntity::getId, j -> j));
        } else {
            jobMap = new HashMap<>();
        }

        // 6. 按学生姓名过滤（如果有）
        List<InterviewVo> voList = page.getRecords().stream()
                .map(interview -> {
                    StudentEntity student = studentMap.get(interview.getStudentId());
                    JobEntity job = jobMap.get(interview.getJobId());

                    // 学生姓名过滤
                    if (StringUtils.isNotEmpty(query.getStudentName()) 
                        && student != null 
                        && !student.getStudentName().contains(query.getStudentName())) {
                        return null;
                    }

                    InterviewVo vo = new InterviewVo();
                    vo.setId(interview.getId());
                    vo.setDeliveryId(interview.getDeliveryId());
                    vo.setStudentId(interview.getStudentId());
                    vo.setJobId(interview.getJobId());
                    vo.setInterviewTime(interview.getInterviewTime() != null ? 
                            interview.getInterviewTime().toString() : null);
                    vo.setDuration(interview.getDuration());
                    vo.setType(interview.getType());
                    vo.setLocation(interview.getLocation());
                    vo.setNotes(interview.getNotes());
                    vo.setStatus(interview.getStatus());
                    vo.setInterviewScore(interview.getInterviewScore());
                    vo.setInterviewComment(interview.getInterviewComment());

                    if (student != null) {
                        vo.setStudentName(student.getStudentName());
                        vo.setStudentPhone(student.getPhone());
                    }

                    if (job != null) {
                        vo.setJobName(job.getJobName());
                    }

                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public void completeInterview(CompanyDeliveryController.CompleteInterviewReq req, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询面试记录
        InterviewEntity interview = interviewMapper.selectById(req.getInterviewId());
        if (interview == null) {
            throw new BusinessException("面试记录不存在");
        }

        // 3. 权限验证
        if (!interview.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限操作该面试记录");
        }

        // 4. 状态验证
        if (interview.getStatus() != 0) {
            throw new BusinessException("只能完成待面试状态的面试");
        }

        // 5. 更新面试记录
        interview.setStatus(1);  // 已完成
        interview.setInterviewScore(req.getScore());
        interview.setInterviewComment(req.getComment());
        interview.setUpdateTime(EntityHelper.now());
        interviewMapper.updateById(interview);

        // 6. 根据面试结果更新投递状态
        JobDeliveryEntity delivery = jobDeliveryMapper.selectById(interview.getDeliveryId());
        if (delivery != null) {
            if ("PASS".equals(req.getResult())) {
                delivery.setStatus(JobDeliveryStatusEnum.OFFER.getValue());
                sendMessage(delivery.getStudentId(), "面试结果通知", 
                        "恭喜您，面试通过！", 2, interview.getId());
            } else if ("FAIL".equals(req.getResult())) {
                delivery.setStatus(JobDeliveryStatusEnum.REJECT.getValue());
                sendMessage(delivery.getStudentId(), "面试结果通知", 
                        "很遗憾，本次面试未通过", 2, interview.getId());
            }
            delivery.setUpdateTime(EntityHelper.now());
            jobDeliveryMapper.updateById(delivery);
        }
    }

    @Override
    public void cancelInterview(String interviewId, String reason, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询面试记录
        InterviewEntity interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在");
        }

        // 3. 权限验证
        if (!interview.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限操作该面试记录");
        }

        // 4. 状态验证
        if (interview.getStatus() != 0) {
            throw new BusinessException("只能取消待面试状态的面试");
        }

        // 5. 更新面试状态
        interview.setStatus(2);  // 已取消
        interview.setNotes(interview.getNotes() + "\n取消原因：" + reason);
        interview.setUpdateTime(EntityHelper.now());
        interviewMapper.updateById(interview);

        // 6. 投递状态回退为待处理
        JobDeliveryEntity delivery = jobDeliveryMapper.selectById(interview.getDeliveryId());
        if (delivery != null) {
            delivery.setStatus(JobDeliveryStatusEnum.DELIVERED.getValue());
            delivery.setUpdateTime(EntityHelper.now());
            jobDeliveryMapper.updateById(delivery);
        }

        // 7. 发送消息通知学生
        sendMessage(interview.getStudentId(), "面试取消通知", 
                "您的面试已被取消。取消原因：" + reason, 2, interviewId);
    }

    @Override
    public void evaluateInterview(CompanyDeliveryController.EvaluateInterviewReq req, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询面试记录
        InterviewEntity interview = interviewMapper.selectById(req.getInterviewId());
        if (interview == null) {
            throw new BusinessException("面试记录不存在");
        }

        // 3. 权限验证
        if (!interview.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限操作该面试记录");
        }

        // 4. 状态验证
        if (interview.getStatus() != 1) {
            throw new BusinessException("只能评价已完成的面试");
        }

        // 5. 更新评价
        interview.setInterviewScore(req.getScore());
        interview.setInterviewComment(req.getComment());
        interview.setUpdateTime(EntityHelper.now());
        interviewMapper.updateById(interview);
    }

    @Override
    public PageResult<DeliveryVo> getTalentList(TalentQuery query, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 构建查询条件（只查询已录用和已拒绝的）
        LambdaQueryWrapper<JobDeliveryEntity> wrapper = new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getCompanyId, hrEntity.getCompanyId())
                .in(JobDeliveryEntity::getStatus, 
                    JobDeliveryStatusEnum.OFFER.getValue(), 
                    JobDeliveryStatusEnum.REJECT.getValue())
                .eq(StringUtils.isNotEmpty(query.getJobId()), 
                    JobDeliveryEntity::getJobId, query.getJobId())
                .eq(StringUtils.isNotEmpty(query.getStatus()), 
                    JobDeliveryEntity::getStatus, query.getStatus())
                .orderByDesc(JobDeliveryEntity::getUpdateTime);

        // 3. 分页查询
        Page<JobDeliveryEntity> page = jobDeliveryMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }

        // 4. 关联查询（复用getDeliveryList的逻辑）
        List<String> studentIds = page.getRecords().stream()
                .map(JobDeliveryEntity::getStudentId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        List<String> jobIds = page.getRecords().stream()
                .map(JobDeliveryEntity::getJobId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());

        Map<String, StudentEntity> studentMap;
        if (!studentIds.isEmpty()) {
            List<StudentEntity> students = studentMapper.selectList(
                    new LambdaQueryWrapper<StudentEntity>()
                            .in(StudentEntity::getStudentId, studentIds));
            studentMap = students.stream()
                    .collect(Collectors.toMap(StudentEntity::getStudentId, s -> s));
        } else {
            studentMap = new HashMap<>();
        }

        Map<String, JobEntity> jobMap;
        if (!jobIds.isEmpty()) {
            List<JobEntity> jobs = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds));
            jobMap = jobs.stream()
                    .collect(Collectors.toMap(JobEntity::getId, j -> j));
        } else {
            jobMap = new HashMap<>();
        }

        // 5. 组装VO
        List<DeliveryVo> voList = page.getRecords().stream()
                .map(delivery -> {
                    StudentEntity student = studentMap.get(delivery.getStudentId());
                    JobEntity job = jobMap.get(delivery.getJobId());

                    // 学生姓名过滤
                    if (StringUtils.isNotEmpty(query.getStudentName()) 
                        && student != null 
                        && !student.getStudentName().contains(query.getStudentName())) {
                        return null;
                    }

                    DeliveryVo vo = new DeliveryVo();
                    vo.setId(delivery.getId());
                    vo.setStudentId(delivery.getStudentId());
                    vo.setJobId(delivery.getJobId());
                    vo.setResumeId(delivery.getResumeId());
                    vo.setStatus(delivery.getStatus());
                    vo.setDeliveryTime(delivery.getUpdateTime() != null ? 
                            delivery.getUpdateTime().toString() : null);

                    if (student != null) {
                        vo.setStudentName(student.getStudentName());
                        vo.setStudentPhone(student.getPhone());
                        vo.setEmail(student.getEmail());
//                        vo.setGender(student.getGender());
                        vo.setGender(1);
                        vo.setMajor(student.getMajorName());
                        vo.setGraduationYear(student.getGraduationYear());
                        vo.setEducation(student.getEducation());
                        
                        if (student.getSchoolId() != null) {
                            SchoolEntity school = schoolMapper.selectById(student.getSchoolId());
                            if (school != null) {
                                vo.setSchool(school.getName());
                            }
                        }
                    }

                    if (job != null) {
                        vo.setJobName(job.getJobName());
                    }

                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public Map<String, Long> getTalentStatistics(String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 统计数据
        Long offerCount = jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getCompanyId, hrEntity.getCompanyId())
                .eq(JobDeliveryEntity::getStatus, JobDeliveryStatusEnum.OFFER.getValue()));

        Long rejectedCount = jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getCompanyId, hrEntity.getCompanyId())
                .eq(JobDeliveryEntity::getStatus, JobDeliveryStatusEnum.REJECT.getValue()));

        Long totalCount = offerCount + rejectedCount;

        Map<String, Long> statistics = new HashMap<>();
        statistics.put("offerCount", offerCount);
        statistics.put("rejectedCount", rejectedCount);
        statistics.put("totalCount", totalCount);

        return statistics;
    }

    @Override
    public void sendOffer(CompanyDeliveryController.SendOfferReq req, String userId) {
        // 1. 获取HR信息
        HrEntity hrEntity = hrMapper.selectOne(new LambdaQueryWrapper<HrEntity>()
                .eq(HrEntity::getUserId, userId));
        if (hrEntity == null) {
            throw new BusinessException("您不是企业HR");
        }

        // 2. 查询投递记录
        JobDeliveryEntity delivery = jobDeliveryMapper.selectById(req.getDeliveryId());
        if (delivery == null) {
            throw new BusinessException("投递记录不存在");
        }

        // 3. 权限验证
        if (!delivery.getCompanyId().equals(hrEntity.getCompanyId())) {
            throw new BusinessException("无权限操作该投递记录");
        }

        // 4. 更新投递状态为已录用
        delivery.setStatus(JobDeliveryStatusEnum.OFFER.getValue());
        delivery.setUpdateTime(EntityHelper.now());
        jobDeliveryMapper.updateById(delivery);

        // 5. 发送Offer通知
        String offerContent = String.format("恭喜您获得Offer！\n入职时间：%s\n薪资：%s\n%s",
                req.getEntryDate(), req.getSalary(), 
                req.getNotes() != null ? "备注：" + req.getNotes() : "");
        
        sendMessage(delivery.getStudentId(), "Offer通知", offerContent, 3, req.getDeliveryId());
    }

    /**
     * 发送消息通知
     */
    private void sendMessage(String receiverId, String title, String content, Integer type, String refId) {
        MessageEntity message = new MessageEntity();
        message.setId(EntityHelper.uuid());
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(0);
        message.setRefId(refId);
        message.setCreateTime(EntityHelper.now());
        
        messageMapper.insert(message);
    }
}
