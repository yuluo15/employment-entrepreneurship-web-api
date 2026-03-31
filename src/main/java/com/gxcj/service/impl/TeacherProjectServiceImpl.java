package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.controller.teacher.TeacherProjectController;
import com.gxcj.entity.*;
import com.gxcj.entity.query.TeacherProjectQuery;
import com.gxcj.entity.vo.teacher.TeacherProjectDetailVo;
import com.gxcj.entity.vo.teacher.TeacherProjectGuidanceVo;
import com.gxcj.entity.vo.teacher.TeacherProjectVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.TeacherProjectService;
import com.gxcj.stutas.DictTypeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherProjectServiceImpl implements TeacherProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SchoolMapper schoolMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ProjectCommentMapper projectCommentMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DictDataMapper dictDataMapper;

    @Override
    public PageResult<TeacherProjectVo> getProjects(TeacherProjectQuery query) {
        String userId = UserContext.getUserId();
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(DictDataEntity::getDictType, DictTypeEnum.sys_project_domain));
        Map<String, String> map = list.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

        // 获取教师信息
        TeacherEntity teacher = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>()
                .eq(TeacherEntity::getUserId, userId));
        String teacherSchoolId = teacher != null ? teacher.getSchoolId() : null;
        String teacherId = teacher != null ? teacher.getTeacherId() : null;

        LambdaQueryWrapper<ProjectEntity> wrapper = new LambdaQueryWrapper<>();

        // 范围筛选：my=我的项目，school=本校项目，all=全网项目
        if ("my".equals(query.getScope()) && StringUtils.hasText(teacherId)) {
            // 只查询自己指导的项目
            wrapper.eq(ProjectEntity::getMentorId, teacherId);
        } else if ("school".equals(query.getScope()) && StringUtils.hasText(teacherSchoolId)) {
            // 本校项目
            wrapper.eq(ProjectEntity::getSchoolId, teacherSchoolId);
        } else if (!StringUtils.hasText(query.getScope()) && StringUtils.hasText(teacherSchoolId)) {
            // 默认为school
            wrapper.eq(ProjectEntity::getSchoolId, teacherSchoolId);
        }
        // all 不添加任何范围限制

        // 关键词搜索（项目名称或学生姓名）
        if (StringUtils.hasText(query.getKeyword())) {
            // 先查询符合条件的学生
            LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.like(StudentEntity::getStudentName, query.getKeyword());
            List<StudentEntity> students = studentMapper.selectList(studentWrapper);
            List<String> userIds = students.stream()
                    .map(StudentEntity::getUserId)
                    .collect(Collectors.toList());

            // 项目名称或学生姓名匹配
            wrapper.and(w -> {
                w.like(ProjectEntity::getProjectName, query.getKeyword());
                if (!userIds.isEmpty()) {
                    w.or().in(ProjectEntity::getUserId, userIds);
                }
            });
        }

        // 状态筛选
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(ProjectEntity::getStatus, query.getStatus());
        }

        // 领域筛选
        if (StringUtils.hasText(query.getDomain())) {
            wrapper.like(ProjectEntity::getDomain, query.getDomain());
        }

        wrapper.orderByDesc(ProjectEntity::getCreateTime);

        Page<ProjectEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ProjectEntity> result = projectMapper.selectPage(page, wrapper);

        List<TeacherProjectVo> voList = new ArrayList<>();
        if (!result.getRecords().isEmpty()) {
            // 批量查询用户信息（通过userId获取学生信息）
            List<String> userIds = result.getRecords().stream()
                    .map(ProjectEntity::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.in(StudentEntity::getUserId, userIds);
            List<StudentEntity> students = studentMapper.selectList(studentWrapper);
            Map<String, StudentEntity> studentMap = students.stream()
                    .collect(Collectors.toMap(StudentEntity::getUserId, s -> s));

            // 批量查询学校信息
            List<String> schoolIds = result.getRecords().stream()
                    .map(ProjectEntity::getSchoolId)
                    .distinct()
                    .collect(Collectors.toList());
            List<SchoolEntity> schools = schoolMapper.selectBatchIds(schoolIds);
            Map<String, SchoolEntity> schoolMap = schools.stream()
                    .collect(Collectors.toMap(SchoolEntity::getId, s -> s));

            // 批量查询指导次数
            List<String> projectIds = result.getRecords().stream()
                    .map(ProjectEntity::getProjectId)
                    .collect(Collectors.toList());
            LambdaQueryWrapper<ProjectCommentEntity> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.in(ProjectCommentEntity::getProjectId, projectIds);
            List<ProjectCommentEntity> comments = projectCommentMapper.selectList(commentWrapper);
            Map<String, Long> guidanceCountMap = comments.stream()
                    .collect(Collectors.groupingBy(ProjectCommentEntity::getProjectId, Collectors.counting()));

            for (ProjectEntity project : result.getRecords()) {
                TeacherProjectVo vo = new TeacherProjectVo();
                vo.setProjectId(project.getProjectId());
                vo.setProjectName(project.getProjectName());
                vo.setLogo(project.getLogo());
                vo.setSchoolId(project.getSchoolId());
                vo.setDomain(String.join(",", Arrays.stream(project.getDomain().split(","))
                        .filter(map::containsKey).map(map::get).toList()));
                vo.setTeamSize(project.getTeamSize());
                vo.setStatus(project.getStatus());
                vo.setGuidanceCount(guidanceCountMap.getOrDefault(project.getProjectId(), 0L).intValue());
                vo.setCreateTime(formatRelativeTime(project.getCreateTime()));

                StudentEntity student = studentMap.get(project.getUserId());
                if (student != null) {
                    vo.setStudentId(student.getStudentId());
                    vo.setStudentName(student.getStudentName());
                    vo.setCollegeName(student.getCollegeName());
                }

                SchoolEntity school = schoolMap.get(project.getSchoolId());
                if (school != null) {
                    vo.setSchoolName(school.getName());
                }

                voList.add(vo);
            }

            // 排序：待指导项目（guidanceCount=0）优先显示
            voList.sort(Comparator.comparing((TeacherProjectVo vo) -> vo.getGuidanceCount() == 0 ? 0 : 1)
                    .thenComparing(Comparator.comparing(TeacherProjectVo::getCreateTime).reversed()));
        }

        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    public TeacherProjectDetailVo getProjectDetail(String projectId) {
        String userId = UserContext.getUserId();
        
        // 获取当前教师信息
        TeacherEntity currentTeacher = teacherMapper.selectOne(new LambdaQueryWrapper<TeacherEntity>()
                .eq(TeacherEntity::getUserId, userId));
        
        // 查询项目基本信息
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 验证权限：只能查看自己指导的项目或本校项目
        boolean isMentor = false;
        if (currentTeacher != null) {
            isMentor = currentTeacher.getTeacherId().equals(project.getMentorId());
            boolean isSchoolProject = currentTeacher.getSchoolId().equals(project.getSchoolId());
            
            if (!isMentor && !isSchoolProject) {
                throw new BusinessException("无权查看该项目");
            }
        }

        // 查询学生信息（通过userId）
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, project.getUserId());
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        // 查询用户信息（获取头像和手机号）
        UserEntity user = null;
        if (student != null) {
            user = userMapper.selectById(student.getUserId());
        }

        // 查询学校信息
        SchoolEntity school = null;
        if (project.getSchoolId() != null) {
            school = schoolMapper.selectById(project.getSchoolId());
        }

        // 组装VO
        TeacherProjectDetailVo vo = new TeacherProjectDetailVo();
        vo.setProjectId(project.getProjectId());
        vo.setProjectName(project.getProjectName());
        vo.setLogo(project.getLogo());
        vo.setDescription(project.getDescription());
        vo.setSchoolId(project.getSchoolId());
        vo.setDomain(project.getDomain());
        vo.setTeamSize(project.getTeamSize());
        vo.setStatus(project.getStatus());
        vo.setIsMentor(isMentor); // 设置是否是指导教师

        if (student != null) {
            vo.setStudentId(student.getStudentId());
            vo.setStudentName(student.getStudentName());
            vo.setCollegeName(student.getCollegeName());
        }

        if (user != null) {
            vo.setStudentAvatar(user.getAvatar());
            vo.setStudentPhone(user.getPhone());
        }

        if (school != null) {
            vo.setSchoolName(school.getName());
        }

        if (project.getCreateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setCreateTime(sdf.format(project.getCreateTime()));
        }

        return vo;
    }

    @Override
    public List<TeacherProjectGuidanceVo> getProjectGuidanceList(String projectId) {
        // 查询指导记录
        LambdaQueryWrapper<ProjectCommentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectCommentEntity::getProjectId, projectId)
                .orderByDesc(ProjectCommentEntity::getCreateTime);
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(wrapper);

        List<TeacherProjectGuidanceVo> voList = new ArrayList<>();
        if (!comments.isEmpty()) {
            // 批量查询教师信息
            List<String> teacherIds = comments.stream()
                    .map(ProjectCommentEntity::getTeacherId)
                    .distinct()
                    .collect(Collectors.toList());
            List<TeacherEntity> teachers = teacherMapper.selectBatchIds(teacherIds);
            Map<String, TeacherEntity> teacherMap = teachers.stream()
                    .collect(Collectors.toMap(TeacherEntity::getTeacherId, t -> t));

            for (ProjectCommentEntity comment : comments) {
                TeacherProjectGuidanceVo vo = new TeacherProjectGuidanceVo();
                vo.setId(comment.getId());
                vo.setProjectId(comment.getProjectId());
                vo.setTeacherId(comment.getTeacherId());
                vo.setContent(comment.getContent());
                vo.setCreateTime(formatRelativeTime(comment.getCreateTime()));

                TeacherEntity teacher = teacherMap.get(comment.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getName());
                    // 获取教师头像（从用户表）
                    UserEntity user = userMapper.selectById(teacher.getUserId());
                    if (user != null) {
                        vo.setTeacherAvatar(user.getAvatar());
                    }
                }

                voList.add(vo);
            }
        }

        return voList;
    }

    @Override
    public List<TeacherProjectController.DictDomainVo> getProjectDomain() {
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(DictDataEntity::getDictType, DictTypeEnum.sys_project_domain));
        List<TeacherProjectController.DictDomainVo> domainVos = list.stream().map(dictDataEntity -> {
            TeacherProjectController.DictDomainVo dictDomainVo = new TeacherProjectController.DictDomainVo();
            BeanUtils.copyProperties(dictDataEntity, dictDomainVo);
            return dictDomainVo;
        }).toList();
        return domainVos;
    }

    private String formatRelativeTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime createTime = timestamp.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(createTime, now);
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        }

        long hours = ChronoUnit.HOURS.between(createTime, now);
        if (hours < 24) {
            return hours + "小时前";
        }

        long days = ChronoUnit.DAYS.between(createTime, now);
        return days + "天前";
    }
}
