package com.gxcj.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.*;
import com.gxcj.entity.dto.school.SchoolEmploymentUpdateDto;
import com.gxcj.entity.query.school.SchoolEmploymentQuery;
import com.gxcj.entity.vo.school.SchoolEmploymentExportVo;
import com.gxcj.entity.vo.school.SchoolEmploymentStatsVo;
import com.gxcj.entity.vo.school.SchoolEmploymentVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolEmploymentService;
import com.gxcj.utils.EntityHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolEmploymentServiceImpl implements SchoolEmploymentService {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private JobDeliveryMapper deliveryMapper;
    
    @Autowired
    private JobMapper jobMapper;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public PageResult<SchoolEmploymentVo> getEmploymentList(SchoolEmploymentQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建学生查询条件
        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getStudentName()), 
                      StudentEntity::getStudentName, query.getStudentName())
                .like(StringUtils.isNotEmpty(query.getStudentNo()), 
                      StudentEntity::getStudentNo, query.getStudentNo())
                .eq(query.getGraduationYear() != null, 
                    StudentEntity::getGraduationYear, query.getGraduationYear())
                .eq(StringUtils.isNotEmpty(query.getEmploymentStatus()), 
                    StudentEntity::getEmploymentStatus, query.getEmploymentStatus())
                .orderByDesc(StudentEntity::getGraduationYear)
                .orderByAsc(StudentEntity::getStudentNo);
        
        // 分页查询学生
        Page<StudentEntity> page = studentMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        
        // 转换为VO并关联就业信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        List<SchoolEmploymentVo> voList = page.getRecords().stream().map(student -> {
            SchoolEmploymentVo vo = new SchoolEmploymentVo();
            vo.setStudentId(student.getStudentId());
            vo.setStudentName(student.getStudentName());
            vo.setStudentNo(student.getStudentNo());
            vo.setCollegeName(student.getCollegeName());
            vo.setMajorName(student.getMajorName());
            vo.setGraduationYear(student.getGraduationYear());
            vo.setEmploymentStatus(student.getEmploymentStatus() != null ? student.getEmploymentStatus() : "0");
            
            // 如果已就业，从投递记录获取就业信息
            if ("SIGNED".equals(student.getEmploymentStatus())) {
                JobDeliveryEntity delivery = deliveryMapper.selectOne(
                        new LambdaQueryWrapper<JobDeliveryEntity>()
                                .eq(JobDeliveryEntity::getStudentId, student.getStudentId())
                                .eq(JobDeliveryEntity::getStatus, "OFFER")
                                .orderByDesc(JobDeliveryEntity::getUpdateTime)
                                .last("LIMIT 1"));
                
                if (delivery != null) {
                    // 获取职位信息
                    JobEntity job = jobMapper.selectById(delivery.getJobId());
                    if (job != null) {
                        vo.setPosition(job.getJobName());
                        vo.setSalary(job.getSalaryRange());
                        vo.setWorkLocation(job.getCity());
                    }
                    
                    // 获取企业信息
                    CompanyEntity company = companyMapper.selectById(delivery.getCompanyId());
                    if (company != null) {
                        vo.setCompanyName(company.getName());
                    }
                    
                    vo.setEmploymentDate(delivery.getUpdateTime() != null ? 
                            sdf.format(delivery.getUpdateTime()) : null);
                    vo.setRemark(delivery.getHandleReply());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public SchoolEmploymentVo getEmploymentDetail(String studentId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 查询学生（带权限校验）
        StudentEntity student = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getStudentId, studentId)
                .eq(StudentEntity::getSchoolId, schoolId));
        
        if (student == null) {
            throw new BusinessException("学生不存在或无权访问");
        }
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        SchoolEmploymentVo vo = new SchoolEmploymentVo();
        vo.setStudentId(student.getStudentId());
        vo.setStudentName(student.getStudentName());
        vo.setStudentNo(student.getStudentNo());
        vo.setCollegeName(student.getCollegeName());
        vo.setMajorName(student.getMajorName());
        vo.setGraduationYear(student.getGraduationYear());
        vo.setEmploymentStatus(student.getEmploymentStatus() != null ? student.getEmploymentStatus() : "0");
        vo.setUpdateTime(student.getUpdateTime() != null ? sdfTime.format(student.getUpdateTime()) : null);
        
        // 如果已就业，从投递记录获取就业信息
        if ("SIGNED".equals(student.getEmploymentStatus())) {
            JobDeliveryEntity delivery = deliveryMapper.selectOne(
                    new LambdaQueryWrapper<JobDeliveryEntity>()
                            .eq(JobDeliveryEntity::getStudentId, studentId)
                            .eq(JobDeliveryEntity::getStatus, "OFFER")
                            .orderByDesc(JobDeliveryEntity::getUpdateTime)
                            .last("LIMIT 1"));
            
            if (delivery != null) {
                // 获取职位信息
                JobEntity job = jobMapper.selectById(delivery.getJobId());
                if (job != null) {
                    vo.setPosition(job.getJobName());
                    vo.setSalary(job.getSalaryRange());
                    vo.setWorkLocation(job.getCity());
                }
                
                // 获取企业信息
                CompanyEntity company = companyMapper.selectById(delivery.getCompanyId());
                if (company != null) {
                    vo.setCompanyName(company.getName());
                }
                
                vo.setEmploymentDate(delivery.getUpdateTime() != null ? 
                        sdf.format(delivery.getUpdateTime()) : null);
                vo.setRemark(delivery.getHandleReply());
            }
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmploymentInfo(SchoolEmploymentUpdateDto dto, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 验证学生是否属于本校
        StudentEntity student = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getStudentId, dto.getStudentId())
                .eq(StudentEntity::getSchoolId, schoolId));
        
        if (student == null) {
            throw new BusinessException("学生不存在或无权访问");
        }
        
        // 验证必填字段
        validateEmploymentData(dto);
        
        // 更新学生表的就业状态
        student.setEmploymentStatus(dto.getEmploymentStatus());
        student.setUpdateTime(EntityHelper.now());
        studentMapper.updateById(student);
        
        // 注意：就业详情信息由企业端在发放offer时自动设置
        // 学校端只能修改就业状态，不能直接修改就业详情
    }

    @Override
    public SchoolEmploymentStatsVo getEmploymentStats(SchoolEmploymentQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建学生查询条件
        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getStudentName()), 
                      StudentEntity::getStudentName, query.getStudentName())
                .like(StringUtils.isNotEmpty(query.getStudentNo()), 
                      StudentEntity::getStudentNo, query.getStudentNo())
                .eq(query.getGraduationYear() != null, 
                    StudentEntity::getGraduationYear, query.getGraduationYear())
                .eq(StringUtils.isNotEmpty(query.getEmploymentStatus()), 
                    StudentEntity::getEmploymentStatus, query.getEmploymentStatus());
        
        // 查询所有学生
        List<StudentEntity> students = studentMapper.selectList(wrapper);
        
        int totalCount = students.size();
        int employedCount = 0;
        
        for (StudentEntity student : students) {
            String status = student.getEmploymentStatus() != null ? student.getEmploymentStatus() : "UNEMPLOYED";
            if ("SIGNED".equals(status)) {
                employedCount++;
            }
        }
        
        // 计算就业率
        double employmentRate = 0.0;
        if (totalCount > 0) {
            employmentRate = Math.round(employedCount * 10000.0 / totalCount) / 100.0;
        }
        
        SchoolEmploymentStatsVo stats = new SchoolEmploymentStatsVo();
        stats.setTotalCount(totalCount);
        stats.setEmployedCount(employedCount);
        stats.setEmploymentRate(employmentRate);
        
        return stats;
    }

    @Override
    public void exportEmploymentData(SchoolEmploymentQuery query, String userId, HttpServletResponse response) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建学生查询条件
        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getStudentName()), 
                      StudentEntity::getStudentName, query.getStudentName())
                .like(StringUtils.isNotEmpty(query.getStudentNo()), 
                      StudentEntity::getStudentNo, query.getStudentNo())
                .eq(query.getGraduationYear() != null, 
                    StudentEntity::getGraduationYear, query.getGraduationYear())
                .eq(StringUtils.isNotEmpty(query.getEmploymentStatus()), 
                    StudentEntity::getEmploymentStatus, query.getEmploymentStatus())
                .orderByDesc(StudentEntity::getGraduationYear)
                .orderByAsc(StudentEntity::getStudentNo);
        
        List<StudentEntity> students = studentMapper.selectList(wrapper);
        
        // 转换为导出VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        List<SchoolEmploymentExportVo> exportList = students.stream().map(student -> {
            SchoolEmploymentExportVo vo = new SchoolEmploymentExportVo();
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
            vo.setCollegeName(student.getCollegeName());
            vo.setMajorName(student.getMajorName());
            vo.setGraduationYear(student.getGraduationYear());
            vo.setEmploymentStatusText(getEmploymentStatusText(student.getEmploymentStatus()));
            
            // 如果已就业，从投递记录获取就业信息
            if ("SIGNED".equals(student.getEmploymentStatus())) {
                JobDeliveryEntity delivery = deliveryMapper.selectOne(
                        new LambdaQueryWrapper<JobDeliveryEntity>()
                                .eq(JobDeliveryEntity::getStudentId, student.getStudentId())
                                .eq(JobDeliveryEntity::getStatus, "OFFER")
                                .orderByDesc(JobDeliveryEntity::getUpdateTime)
                                .last("LIMIT 1"));
                
                if (delivery != null) {
                    // 获取职位信息
                    JobEntity job = jobMapper.selectById(delivery.getJobId());
                    if (job != null) {
                        vo.setPosition(job.getJobName());
                        vo.setSalary(job.getSalaryRange());
                        vo.setWorkLocation(job.getCity());
                    }
                    
                    // 获取企业信息
                    CompanyEntity company = companyMapper.selectById(delivery.getCompanyId());
                    if (company != null) {
                        vo.setCompanyName(company.getName());
                    }
                    
                    vo.setEmploymentDate(delivery.getUpdateTime() != null ? 
                            sdf.format(delivery.getUpdateTime()) : null);
                    vo.setRemark(delivery.getHandleReply());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = URLEncoder.encode("就业情况_" + timestamp, StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            EasyExcel.write(response.getOutputStream(), SchoolEmploymentExportVo.class)
                    .sheet("就业情况")
                    .doWrite(exportList);
        } catch (IOException e) {
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

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
        
        throw new BusinessException("无权访问就业信息");
    }

    private void validateEmploymentData(SchoolEmploymentUpdateDto dto) {
        if (StringUtils.isBlank(dto.getStudentId())) {
            throw new BusinessException("学生ID不能为空");
        }
        if (StringUtils.isBlank(dto.getEmploymentStatus())) {
            throw new BusinessException("就业状态不能为空");
        }
        
        // 验证就业状态值
        if (!"UNEMPLOYED".equals(dto.getEmploymentStatus()) && !"SIGNED".equals(dto.getEmploymentStatus())) {
            throw new BusinessException("就业状态值错误!");
        }
    }

    private String getEmploymentStatusText(String status) {
        if (status == null) return "待就业";
        switch (status) {
            case "UNEMPLOYED":
                return "待就业";
            case "SIGNED":
                return "已就业";
            case "FURTHER_STUDY":
                return "升学";
            case "ABROAD":
                return "出国";
            case "ENTREPRENEURSHIP":
                return "创业";
            default:
                return "待就业";
        }
    }
}
