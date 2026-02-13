package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.school.*;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.service.SchoolDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SchoolDashboardServiceImpl implements SchoolDashboardService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public SchoolDashboardVo getDashboardData(String userId) {
        log.info("获取学校工作台数据: userId={}", userId);
        
        // 获取学校ID
        String schoolId = getSchoolIdByUserId(userId);
        
        SchoolDashboardVo vo = new SchoolDashboardVo();
        
        // 1. 学校名称
        vo.setSchoolName(getSchoolName(schoolId));
        
        // 2. 学生总数
        vo.setStudentCount(getStudentCount(schoolId));
        
        // 3. 教师总数
        vo.setTeacherCount(getTeacherCount(schoolId));
        
        // 4. 就业率（当届毕业生）
        vo.setEmploymentRate(getEmploymentRate(schoolId));
        
        // 5. 孵化中的创业项目数量
        vo.setIncubatingProjectCount(getIncubatingProjectCount(schoolId));
        
        // 6. 待审核项目列表（最新5个）
        vo.setPendingProjects(getPendingProjects(schoolId));
        
        // 7. 最新通知公告（最新5条）
        vo.setLatestNotices(getLatestNotices(schoolId));
        
        // 8. 近6个月就业率趋势
        vo.setEmploymentTrend(getEmploymentTrend(schoolId));
        
        // 9. 创业项目状态分布
        vo.setProjectStatus(getProjectStatus(schoolId));
        
        // 10. 各学院就业情况（TOP 10）
        vo.setCollegeStats(getCollegeStats(schoolId));
        
        return vo;
    }

    // ==================== 私有方法 ====================

    /**
     * 获取学校名称
     */
    private String getSchoolName(String schoolId) {
        SchoolEntity school = schoolMapper.selectById(schoolId);
        return school != null ? school.getName() : "";
    }

    /**
     * 获取学生总数
     */
    private Integer getStudentCount(String schoolId) {
        return studentMapper.selectCount(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getSchoolId, schoolId)
        ).intValue();
    }

    /**
     * 获取教师总数
     */
    private Integer getTeacherCount(String schoolId) {
        return teacherMapper.selectCount(
                new LambdaQueryWrapper<TeacherEntity>()
                        .eq(TeacherEntity::getSchoolId, schoolId)
        ).intValue();
    }

    /**
     * 获取就业率（当届毕业生）
     */
    private String getEmploymentRate(String schoolId) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        List<StudentEntity> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getSchoolId, schoolId)
                        .eq(StudentEntity::getGraduationYear, currentYear)
        );
        
        if (students.isEmpty()) {
            return "0.0%";
        }
        
        long employedCount = students.stream()
                .filter(s -> "1".equals(s.getEmploymentStatus()))
                .count();
        
        double rate = Math.round(employedCount * 1000.0 / students.size()) / 10.0;
        return String.format("%.1f%%", rate);
    }

    /**
     * 获取孵化中的创业项目数量
     */
    private Integer getIncubatingProjectCount(String schoolId) {
        return projectMapper.selectCount(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getSchoolId, schoolId)
                        .eq(ProjectEntity::getStatus, "1")
        ).intValue();
    }

    /**
     * 获取待审核项目列表（最新5个）
     */
    private List<SchoolPendingProjectVo> getPendingProjects(String schoolId) {
        List<ProjectEntity> projects = projectMapper.selectList(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getSchoolId, schoolId)
                        .eq(ProjectEntity::getStatus, "0")
                        .orderByDesc(ProjectEntity::getCreateTime)
                        .last("LIMIT 5")
        );
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return projects.stream().map(project -> {
            SchoolPendingProjectVo vo = new SchoolPendingProjectVo();
            vo.setProjectId(project.getProjectId());
            vo.setProjectName(project.getProjectName());
            vo.setLogo(project.getLogo());
            vo.setDomain(project.getDomain());
            vo.setCreateTime(project.getCreateTime() != null ? 
                    sdf.format(project.getCreateTime()) : null);
            
            // 获取学生姓名
            StudentEntity student = studentMapper.selectOne(
                    new LambdaQueryWrapper<StudentEntity>()
                            .eq(StudentEntity::getUserId, project.getUserId())
            );
            if (student != null) {
                vo.setStudentName(student.getStudentName());
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取最新通知公告（最新5条）
     */
    private List<SchoolLatestNoticeVo> getLatestNotices(String schoolId) {
        List<NoticeEntity> notices = noticeMapper.selectList(
                new LambdaQueryWrapper<NoticeEntity>()
                        .eq(NoticeEntity::getStatus, 1)
                        .and(w -> w
                                .eq(NoticeEntity::getPublisherType, "admin")
                                .or()
                                .and(w2 -> w2.eq(NoticeEntity::getPublisherType, "school")
                                            .eq(NoticeEntity::getPublisherId, schoolId))
                        )
                        .orderByDesc(NoticeEntity::getIsTop)
                        .orderByDesc(NoticeEntity::getPublishTime)
                        .last("LIMIT 5")
        );
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return notices.stream().map(notice -> {
            SchoolLatestNoticeVo vo = new SchoolLatestNoticeVo();
            vo.setNoticeId(notice.getNoticeId());
            vo.setNoticeTitle(notice.getNoticeTitle());
            vo.setNoticeType(notice.getNoticeType());
            vo.setIsTop(notice.getIsTop());
            vo.setPublishTime(notice.getPublishTime() != null ? 
                    sdf.format(notice.getPublishTime()) : null);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取近6个月就业率趋势
     */
    private SchoolEmploymentTrendVo getEmploymentTrend(String schoolId) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        // 生成近6个月的月份列表
        List<String> months = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            months.add(sdf.format(cal.getTime()));
        }
        
        // 获取当届毕业生
        List<StudentEntity> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getSchoolId, schoolId)
                        .eq(StudentEntity::getGraduationYear, currentYear)
        );
        
        // 计算每个月的就业率
        List<Double> rates = new ArrayList<>();
        
        for (String month : months) {
            if (students.isEmpty()) {
                rates.add(0.0);
            } else {
                long employedCount = students.stream()
                        .filter(s -> "1".equals(s.getEmploymentStatus()))
                        .count();
                
                double rate = Math.round(employedCount * 1000.0 / students.size()) / 10.0;
                rates.add(rate);
            }
        }
        
        SchoolEmploymentTrendVo vo = new SchoolEmploymentTrendVo();
        vo.setMonths(months);
        vo.setRates(rates);
        
        return vo;
    }

    /**
     * 获取创业项目状态分布
     */
    private List<SchoolProjectStatusVo> getProjectStatus(String schoolId) {
        List<ProjectEntity> projects = projectMapper.selectList(
                new LambdaQueryWrapper<ProjectEntity>()
                        .eq(ProjectEntity::getSchoolId, schoolId)
        );
        
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
        List<SchoolProjectStatusVo> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            SchoolProjectStatusVo vo = new SchoolProjectStatusVo();
            vo.setStatusName(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 获取各学院就业情况（TOP 10）
     */
    private List<SchoolCollegeEmploymentVo> getCollegeStats(String schoolId) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        List<StudentEntity> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getSchoolId, schoolId)
                        .eq(StudentEntity::getGraduationYear, currentYear)
                        .isNotNull(StudentEntity::getCollegeName)
        );
        
        // 按学院分组统计
        Map<String, List<StudentEntity>> collegeMap = students.stream()
                .filter(s -> StringUtils.isNotBlank(s.getCollegeName()))
                .collect(Collectors.groupingBy(StudentEntity::getCollegeName));
        
        List<SchoolCollegeEmploymentVo> result = new ArrayList<>();
        
        for (Map.Entry<String, List<StudentEntity>> entry : collegeMap.entrySet()) {
            String collegeName = entry.getKey();
            List<StudentEntity> collegeStudents = entry.getValue();
            
            long employedCount = collegeStudents.stream()
                    .filter(s -> "1".equals(s.getEmploymentStatus()))
                    .count();
            
            double rate = 0.0;
            if (!collegeStudents.isEmpty()) {
                rate = Math.round(employedCount * 1000.0 / collegeStudents.size()) / 10.0;
            }
            
            SchoolCollegeEmploymentVo vo = new SchoolCollegeEmploymentVo();
            vo.setCollegeName(collegeName);
            vo.setEmploymentRate(rate);
            
            result.add(vo);
        }
        
        // 按就业率降序排列，取前10
        result.sort((a, b) -> Double.compare(b.getEmploymentRate(), a.getEmploymentRate()));
        
        return result.stream().limit(10).collect(Collectors.toList());
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
        
        throw new BusinessException("无权访问工作台");
    }
}
