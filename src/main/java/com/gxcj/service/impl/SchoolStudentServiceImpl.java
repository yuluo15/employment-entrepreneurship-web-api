package com.gxcj.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.*;
import com.gxcj.entity.dto.school.SchoolStudentImportDto;
import com.gxcj.entity.query.school.SchoolStudentQuery;
import com.gxcj.entity.vo.school.SchoolStudentExportVo;
import com.gxcj.entity.vo.school.SchoolStudentImportResultVo;
import com.gxcj.entity.vo.school.SchoolStudentResumeVo;
import com.gxcj.entity.vo.school.SchoolStudentVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolStudentService;
import com.gxcj.utils.EntityHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolStudentServiceImpl implements SchoolStudentService {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private StudentResumeMapper studentResumeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public PageResult<SchoolStudentVo> getStudentList(SchoolStudentQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getStudentName()), 
                      StudentEntity::getStudentName, query.getStudentName())
                .like(StringUtils.isNotEmpty(query.getStudentNo()), 
                      StudentEntity::getStudentNo, query.getStudentNo())
                .like(StringUtils.isNotEmpty(query.getCollegeName()), 
                      StudentEntity::getCollegeName, query.getCollegeName())
                .like(StringUtils.isNotEmpty(query.getMajorName()), 
                      StudentEntity::getMajorName, query.getMajorName())
                .eq(query.getEnrollmentYear() != null, 
                    StudentEntity::getEnrollmentYear, query.getEnrollmentYear())
                .eq(StringUtils.isNotEmpty(query.getEmploymentStatus()), 
                    StudentEntity::getEmploymentStatus, query.getEmploymentStatus())
                .orderByDesc(StudentEntity::getCreateTime);
        
        // 分页查询
        Page<StudentEntity> page = studentMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SchoolStudentVo> voList = page.getRecords().stream().map(entity -> {
            SchoolStudentVo vo = new SchoolStudentVo();
            BeanUtils.copyProperties(entity, vo);
            vo.setGender(String.valueOf(entity.getGender()));
            vo.setCreateTime(entity.getCreateTime() != null ? sdf.format(entity.getCreateTime()) : null);
            vo.setUpdateTime(entity.getUpdateTime() != null ? sdf.format(entity.getUpdateTime()) : null);
            return vo;
        }).collect(Collectors.toList());
        
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public SchoolStudentVo getStudentDetail(String studentId, String userId) {
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SchoolStudentVo vo = new SchoolStudentVo();
        BeanUtils.copyProperties(student, vo);
        vo.setGender(String.valueOf(student.getGender()));
        vo.setCreateTime(student.getCreateTime() != null ? sdf.format(student.getCreateTime()) : null);
        vo.setUpdateTime(student.getUpdateTime() != null ? sdf.format(student.getUpdateTime()) : null);
        
        return vo;
    }

    @Override
    public SchoolStudentResumeVo getStudentResume(String studentId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 验证学生是否属于本校
        StudentEntity student = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getStudentId, studentId)
                .eq(StudentEntity::getSchoolId, schoolId));
        
        if (student == null) {
            throw new BusinessException("学生不存在或无权访问");
        }
        
        // 查询简历
        StudentResumeEntity resume = studentResumeMapper.selectOne(
                new LambdaQueryWrapper<StudentResumeEntity>()
                        .eq(StudentResumeEntity::getStudentId, studentId));
        
        if (resume == null) {
            throw new BusinessException("该学生暂未完善简历");
        }
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SchoolStudentResumeVo vo = new SchoolStudentResumeVo();
        BeanUtils.copyProperties(resume, vo);
        vo.setCreateTime(resume.getCreateTime() != null ? sdf.format(resume.getCreateTime()) : null);
        vo.setUpdateTime(resume.getUpdateTime() != null ? sdf.format(resume.getUpdateTime()) : null);
        
        // 转换JSONB字段
        if (resume.getEducationHistory() != null) {
            vo.setEducationHistory(resume.getEducationHistory().stream()
                    .map(item -> new SchoolStudentResumeVo.EducationItem(
                            item.getSchool(), item.getMajor(), item.getDegree(),
                            item.getStartDate(), item.getEndDate()))
                    .collect(Collectors.toList()));
        }
        
        if (resume.getInternshipExp() != null) {
            vo.setInternshipExp(resume.getInternshipExp().stream()
                    .map(item -> new SchoolStudentResumeVo.InternshipItem(
                            item.getCompany(), item.getRole(), item.getStartDate(),
                            item.getEndDate(), item.getDescription()))
                    .collect(Collectors.toList()));
        }
        
        if (resume.getProjectExp() != null) {
            vo.setProjectExp(resume.getProjectExp().stream()
                    .map(item -> new SchoolStudentResumeVo.ProjectItem(
                            item.getProjectName(), item.getRole(), item.getStartDate(),
                            item.getEndDate(), item.getDescription()))
                    .collect(Collectors.toList()));
        }
        
        if (resume.getCertificates() != null) {
            vo.setCertificates(resume.getCertificates().stream()
                    .map(item -> new SchoolStudentResumeVo.CertificateItem(
                            item.getName(), item.getIssuer(), item.getDate()))
                    .collect(Collectors.toList()));
        }
        
        return vo;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("学生信息导入模板", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            // 创建表头
            List<List<String>> head = new ArrayList<>();
            head.add(Arrays.asList("学号"));
            head.add(Arrays.asList("姓名"));
            head.add(Arrays.asList("性别"));
            head.add(Arrays.asList("学院"));
            head.add(Arrays.asList("专业"));
            head.add(Arrays.asList("班级"));
            head.add(Arrays.asList("学历"));
            head.add(Arrays.asList("入学年份"));
            head.add(Arrays.asList("毕业年份"));
            head.add(Arrays.asList("联系电话"));
            head.add(Arrays.asList("邮箱"));
            
            // 创建示例数据
            List<List<Object>> data = new ArrayList<>();
            data.add(Arrays.asList("2021001", "张三", "男", "计算机学院", "软件工程", "软件2101", "本科", 2021, 2025, "13800138000", "zhangsan@example.com"));
            data.add(Arrays.asList("2021002", "李四", "女", "计算机学院", "软件工程", "软件2101", "本科", 2021, 2025, "13800138001", "lisi@example.com"));
            
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("学生信息")
                    .doWrite(data);
        } catch (IOException e) {
            throw new BusinessException("模板下载失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolStudentImportResultVo importStudents(MultipartFile file, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        SchoolStudentImportResultVo result = new SchoolStudentImportResultVo();
        int successCount = 0;
        int failCount = 0;
        List<SchoolStudentImportResultVo.ImportFailItem> failList = new ArrayList<>();
        
        try {
            // 读取Excel
            List<SchoolStudentImportDto> dataList = EasyExcel.read(file.getInputStream())
                    .head(SchoolStudentImportDto.class)
                    .sheet()
                    .doReadSync();
            
            for (int i = 0; i < dataList.size(); i++) {
                SchoolStudentImportDto dto = dataList.get(i);
                int rowNum = i + 2; // Excel行号
                
                try {
                    // 数据校验
                    String validateError = validateStudentData(dto);
                    if (validateError != null) {
                        failList.add(new SchoolStudentImportResultVo.ImportFailItem(rowNum, dto.getStudentNo(), validateError));
                        failCount++;
                        continue;
                    }
                    
                    // 检查学号是否已存在
                    Long count = studentMapper.selectCount(new LambdaQueryWrapper<StudentEntity>()
                            .eq(StudentEntity::getStudentNo, dto.getStudentNo())
                            .eq(StudentEntity::getSchoolId, schoolId));
                    if (count > 0) {
                        failList.add(new SchoolStudentImportResultVo.ImportFailItem(rowNum, dto.getStudentNo(), "学号已存在"));
                        failCount++;
                        continue;
                    }
                    
                    // 创建用户账号
                    UserEntity user = new UserEntity();
                    user.setId(EntityHelper.uuid());
                    user.setLoginIdentity(dto.getStudentNo());
                    user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
                    user.setRealName(dto.getStudentName());
                    user.setRoleKey("ROLE_STUDENT");
                    user.setOwnerId(schoolId);
                    user.setStatus(1);
                    user.setGender("男".equals(dto.getGender()) ? 1 : 2);
                    user.setCreateTime(EntityHelper.now());
                    user.setUpdateTime(EntityHelper.now());
                    userMapper.insert(user);
                    
                    // 创建学生档案
                    StudentEntity student = new StudentEntity();
                    student.setStudentId(EntityHelper.uuid());
                    student.setUserId(user.getId());
                    student.setSchoolId(schoolId);
                    student.setStudentName(dto.getStudentName());
                    student.setStudentNo(dto.getStudentNo());
//                    student.setGender("男".equals(dto.getGender()) ? 1 : 2);
                    student.setGender("1");
                    student.setCollegeName(dto.getCollegeName());
                    student.setMajorName(dto.getMajorName());
                    student.setClassName(dto.getClassName());
                    student.setEducation(dto.getEducation());
                    student.setEnrollmentYear(dto.getEnrollmentYear());
                    student.setGraduationYear(dto.getGraduationYear());
                    student.setPhone(dto.getPhone());
                    student.setEmail(dto.getEmail());
                    student.setEmploymentStatus("0");
                    student.setCreateTime(EntityHelper.now());
                    student.setUpdateTime(EntityHelper.now());
                    studentMapper.insert(student);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    failList.add(new SchoolStudentImportResultVo.ImportFailItem(rowNum, dto.getStudentNo(), "系统错误：" + e.getMessage()));
                    failCount++;
                }
            }
            
            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setFailList(failList);
            
            return result;
            
        } catch (IOException e) {
            throw new BusinessException("文件读取失败：" + e.getMessage());
        }
    }

    @Override
    public void exportStudentData(SchoolStudentQuery query, String userId, HttpServletResponse response) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getStudentName()), 
                      StudentEntity::getStudentName, query.getStudentName())
                .like(StringUtils.isNotEmpty(query.getStudentNo()), 
                      StudentEntity::getStudentNo, query.getStudentNo())
                .like(StringUtils.isNotEmpty(query.getCollegeName()), 
                      StudentEntity::getCollegeName, query.getCollegeName())
                .like(StringUtils.isNotEmpty(query.getMajorName()), 
                      StudentEntity::getMajorName, query.getMajorName())
                .eq(query.getEnrollmentYear() != null, 
                    StudentEntity::getEnrollmentYear, query.getEnrollmentYear())
                .eq(StringUtils.isNotEmpty(query.getEmploymentStatus()), 
                    StudentEntity::getEmploymentStatus, query.getEmploymentStatus())
                .orderByDesc(StudentEntity::getCreateTime);
        
        List<StudentEntity> students = studentMapper.selectList(wrapper);
        
        // 转换为导出VO
        List<SchoolStudentExportVo> exportList = students.stream().map(student -> {
            SchoolStudentExportVo vo = new SchoolStudentExportVo();
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
//            vo.setGenderText(student.getGender() == 1 ? "男" : "女");
            vo.setGenderText("男");
            vo.setCollegeName(student.getCollegeName());
            vo.setMajorName(student.getMajorName());
            vo.setClassName(student.getClassName());
            vo.setEducation(student.getEducation());
            vo.setEnrollmentYear(student.getEnrollmentYear());
            vo.setGraduationYear(student.getGraduationYear());
            vo.setEmploymentStatusText(getEmploymentStatusText(student.getEmploymentStatus()));
            vo.setPhone(student.getPhone());
            vo.setEmail(student.getEmail());
            return vo;
        }).collect(Collectors.toList());
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = URLEncoder.encode("学生档案_" + timestamp, StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            EasyExcel.write(response.getOutputStream(), SchoolStudentExportVo.class)
                    .sheet("学生档案")
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

        if ("ROLE_SCHOOL".equals(roleEntity.getRoleName())) {
            return user.getOwnerId();
        }
        
        if ("ROLE_TEACHER".equals(roleEntity.getRoleName())) {
            TeacherEntity teacher = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>()
                    .eq(TeacherEntity::getUserId, userId));
            if (teacher != null) {
                return teacher.getSchoolId();
            }
        }
        
        throw new BusinessException("无权访问学生档案");
    }

    private String validateStudentData(SchoolStudentImportDto dto) {
        if (StringUtils.isBlank(dto.getStudentNo())) return "学号不能为空";
        if (StringUtils.isBlank(dto.getStudentName())) return "姓名不能为空";
        if (StringUtils.isBlank(dto.getGender())) return "性别不能为空";
        if (!"男".equals(dto.getGender()) && !"女".equals(dto.getGender())) return "性别格式错误，应填写：男 或 女";
        if (StringUtils.isBlank(dto.getCollegeName())) return "学院不能为空";
        if (StringUtils.isBlank(dto.getMajorName())) return "专业不能为空";
        if (StringUtils.isBlank(dto.getEducation())) return "学历不能为空";
        if (!Arrays.asList("专科", "本科", "硕士", "博士").contains(dto.getEducation())) return "学历格式错误";
        if (dto.getEnrollmentYear() == null) return "入学年份不能为空";
        if (dto.getGraduationYear() == null) return "毕业年份不能为空";
        if (dto.getEnrollmentYear() >= dto.getGraduationYear()) return "毕业年份必须大于入学年份";
        return null;
    }

    private String getEmploymentStatusText(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "0":
                return "待就业";
            case "1":
                return "已就业";
            case "2":
                return "升学";
            case "3":
                return "出国";
            case "4":
                return "创业";
            default:
                return "未知";
        }
    }
}
