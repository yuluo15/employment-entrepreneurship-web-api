package com.gxcj.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.RoleEntity;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.dto.school.SchoolTeacherImportDto;
import com.gxcj.entity.query.school.SchoolTeacherQuery;
import com.gxcj.entity.vo.school.SchoolTeacherImportResultVo;
import com.gxcj.entity.vo.school.SchoolTeacherVo;
import com.gxcj.entity.vo.TeacherExportVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.RoleMapper;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.TeacherMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolTeacherService;
import com.gxcj.utils.EntityHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
public class SchoolTeacherServiceImpl implements SchoolTeacherService {

    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private SchoolMapper schoolMapper;

    @Override
    public PageResult<SchoolTeacherVo> getTeacherList(SchoolTeacherQuery query, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<TeacherEntity> wrapper = new LambdaQueryWrapper<TeacherEntity>()
                .eq(TeacherEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getName()), 
                      TeacherEntity::getName, query.getName())
                .like(StringUtils.isNotEmpty(query.getEmployeeNo()), 
                      TeacherEntity::getEmployeeNo, query.getEmployeeNo())
                .like(StringUtils.isNotEmpty(query.getCollegeName()), 
                      TeacherEntity::getCollegeName, query.getCollegeName())
                .like(StringUtils.isNotEmpty(query.getTitle()), 
                      TeacherEntity::getTitle, query.getTitle())
                .orderByDesc(TeacherEntity::getCreateTime);
        
        // 分页查询
        Page<TeacherEntity> page = teacherMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SchoolTeacherVo> voList = page.getRecords().stream().map(entity -> {
            SchoolTeacherVo vo = new SchoolTeacherVo();
            BeanUtils.copyProperties(entity, vo);
            vo.setCreateTime(entity.getCreateTime() != null ? sdf.format(entity.getCreateTime()) : null);
            return vo;
        }).collect(Collectors.toList());
        
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public SchoolTeacherVo getTeacherDetail(String teacherId, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 查询教师（带权限校验）
        TeacherEntity teacher = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>()
                .eq(TeacherEntity::getTeacherId, teacherId)
                .eq(TeacherEntity::getSchoolId, schoolId));
        
        if (teacher == null) {
            throw new BusinessException("教师不存在或无权访问");
        }
        
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SchoolTeacherVo vo = new SchoolTeacherVo();
        BeanUtils.copyProperties(teacher, vo);
        vo.setCreateTime(teacher.getCreateTime() != null ? sdf.format(teacher.getCreateTime()) : null);
        vo.setUpdateTime(teacher.getUpdateTime() != null ? sdf.format(teacher.getUpdateTime()) : null);
        
        return vo;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("教师信息导入模板", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            // 创建表头
            List<List<String>> head = new ArrayList<>();
            head.add(Arrays.asList("工号"));
            head.add(Arrays.asList("姓名"));
            head.add(Arrays.asList("性别"));
            head.add(Arrays.asList("学院"));
            head.add(Arrays.asList("职称"));
            head.add(Arrays.asList("专业领域"));
            head.add(Arrays.asList("联系电话"));
            head.add(Arrays.asList("邮箱"));
            head.add(Arrays.asList("个人简介"));
            
            // 创建示例数据
            List<List<Object>> data = new ArrayList<>();
            data.add(Arrays.asList("T2020001", "王教授", "男", "计算机学院", "教授", "人工智能、机器学习", "13900139000", "wangprof@example.com", "从事人工智能研究20年"));
            data.add(Arrays.asList("T2020002", "李副教授", "女", "计算机学院", "副教授", "软件工程、数据库", "13900139001", "liprof@example.com", "专注软件工程领域研究"));
            
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("教师信息")
                    .doWrite(data);
        } catch (IOException e) {
            throw new BusinessException("模板下载失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolTeacherImportResultVo importTeachers(MultipartFile file, String userId) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        SchoolTeacherImportResultVo result = new SchoolTeacherImportResultVo();
        int successCount = 0;
        int failCount = 0;
        List<SchoolTeacherImportResultVo.ImportFailItem> failList = new ArrayList<>();
        
        try {
            // 读取Excel
            List<SchoolTeacherImportDto> dataList = EasyExcel.read(file.getInputStream())
                    .head(SchoolTeacherImportDto.class)
                    .sheet()
                    .doReadSync();
            
            for (int i = 0; i < dataList.size(); i++) {
                SchoolTeacherImportDto dto = dataList.get(i);
                int rowNum = i + 2; // Excel行号
                
                try {
                    // 数据校验
                    String validateError = validateTeacherData(dto);
                    if (validateError != null) {
                        failList.add(new SchoolTeacherImportResultVo.ImportFailItem(rowNum, dto.getEmployeeNo(), validateError));
                        failCount++;
                        continue;
                    }
                    
                    // 检查工号是否已存在
                    Long count = teacherMapper.selectCount(new LambdaQueryWrapper<TeacherEntity>()
                            .eq(TeacherEntity::getEmployeeNo, dto.getEmployeeNo())
                            .eq(TeacherEntity::getSchoolId, schoolId));
                    if (count > 0) {
                        failList.add(new SchoolTeacherImportResultVo.ImportFailItem(rowNum, dto.getEmployeeNo(), "工号已存在"));
                        failCount++;
                        continue;
                    }
                    
                    // 创建用户账号
                    UserEntity user = new UserEntity();
                    user.setId(EntityHelper.uuid());
                    user.setLoginIdentity(dto.getEmployeeNo());
                    user.setPassword(EntityHelper.encodedPassword("123456"));
                    user.setRealName(dto.getName());
                    user.setRoleKey("3");
                    user.setOwnerId(schoolId);
                    user.setStatus(1);
                    user.setGender("男".equals(dto.getGender()) ? 1 : 2);
                    user.setCreateTime(EntityHelper.now());
                    user.setUpdateTime(EntityHelper.now());
                    userMapper.insert(user);
                    
                    // 创建教师档案
                    TeacherEntity teacher = new TeacherEntity();
                    teacher.setTeacherId(EntityHelper.uuid());
                    teacher.setUserId(user.getId());
                    teacher.setSchoolId(schoolId);
                    teacher.setName(dto.getName());
                    teacher.setEmployeeNo(dto.getEmployeeNo());
                    teacher.setGender("男".equals(dto.getGender()) ? "1" : "2");
                    teacher.setCollegeName(dto.getCollegeName());
                    teacher.setTitle(dto.getTitle());
                    teacher.setExpertise(dto.getExpertise());
                    teacher.setIntro(dto.getIntro());
                    teacher.setPhone(dto.getPhone());
                    teacher.setEmail(dto.getEmail());
                    teacher.setGuidanceCount(0);
//                    teacher.setRatingScore(new BigDecimal("5.0"));
                    teacher.setRatingScore(5);
                    teacher.setCreateTime(EntityHelper.now());
                    teacher.setUpdateTime(EntityHelper.now());
                    teacherMapper.insert(teacher);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    failList.add(new SchoolTeacherImportResultVo.ImportFailItem(rowNum, dto.getEmployeeNo(), "系统错误：" + e.getMessage()));
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
    public void exportTeacherData(SchoolTeacherQuery query, String userId, HttpServletResponse response) {
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        // 查询学校信息
        SchoolEntity school = schoolMapper.selectById(schoolId);
        String schoolName = school != null ? school.getName() : "";
        
        // 构建查询条件
        LambdaQueryWrapper<TeacherEntity> wrapper = new LambdaQueryWrapper<TeacherEntity>()
                .eq(TeacherEntity::getSchoolId, schoolId)
                .like(StringUtils.isNotEmpty(query.getName()), 
                      TeacherEntity::getName, query.getName())
                .like(StringUtils.isNotEmpty(query.getEmployeeNo()), 
                      TeacherEntity::getEmployeeNo, query.getEmployeeNo())
                .like(StringUtils.isNotEmpty(query.getCollegeName()), 
                      TeacherEntity::getCollegeName, query.getCollegeName())
                .like(StringUtils.isNotEmpty(query.getTitle()), 
                      TeacherEntity::getTitle, query.getTitle())
                .orderByDesc(TeacherEntity::getCreateTime);
        
        List<TeacherEntity> teachers = teacherMapper.selectList(wrapper);
        
        // 转换为导出VO
        List<TeacherExportVo> exportList = teachers.stream().map(teacher -> 
            TeacherExportVo.builder()
                    .employeeNo(teacher.getEmployeeNo())
                    .name(teacher.getName())
                    .schoolName(schoolName)
                    .collegeName(teacher.getCollegeName())
                    .title(teacher.getTitle())
                    .expertise(teacher.getExpertise())
                    .guidanceCount(teacher.getGuidanceCount())
                    .phone(teacher.getPhone())
                    .email(teacher.getEmail())
                    .build()
        ).collect(Collectors.toList());
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = URLEncoder.encode("教师档案_" + timestamp, StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            EasyExcel.write(response.getOutputStream(), TeacherExportVo.class)
                    .sheet("教师档案")
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
        
        throw new BusinessException("无权访问教师档案");
    }

    private String validateTeacherData(SchoolTeacherImportDto dto) {
        if (StringUtils.isBlank(dto.getEmployeeNo())) return "工号不能为空";
        if (StringUtils.isBlank(dto.getName())) return "姓名不能为空";
        if (StringUtils.isBlank(dto.getGender())) return "性别不能为空";
        if (!"男".equals(dto.getGender()) && !"女".equals(dto.getGender())) return "性别格式错误，应填写：男 或 女";
        if (StringUtils.isBlank(dto.getCollegeName())) return "学院不能为空";
        if (StringUtils.isBlank(dto.getTitle())) return "职称不能为空";
        if (!Arrays.asList("教授", "副教授", "讲师", "助教").contains(dto.getTitle())) return "职称格式错误，应填写：教授、副教授、讲师、助教";
        return null;
    }
}
