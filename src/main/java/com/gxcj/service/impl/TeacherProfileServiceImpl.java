package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.context.UserContext;
import com.gxcj.entity.ProjectCommentEntity;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.vo.teacher.TeacherProfileVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.ProjectCommentMapper;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.TeacherMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.service.TeacherProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherProfileServiceImpl implements TeacherProfileService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SchoolMapper schoolMapper;

    @Autowired
    private ProjectCommentMapper projectCommentMapper;

    @Override
    public TeacherProfileVo getTeacherInfo() {
        String userId = UserContext.getUserId();

        // 查询教师基本信息
        LambdaQueryWrapper<TeacherEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherEntity::getUserId, userId);
        TeacherEntity teacher = teacherMapper.selectOne(wrapper);

        if (teacher == null) {
            throw new BusinessException("教师信息不存在");
        }

        // 查询用户信息（获取头像、手机号、邮箱）
        UserEntity user = userMapper.selectById(userId);

        // 查询学校信息
        SchoolEntity school = null;
        if (teacher.getSchoolId() != null) {
            school = schoolMapper.selectById(teacher.getSchoolId());
        }

        // 统计指导次数
        LambdaQueryWrapper<ProjectCommentEntity> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(ProjectCommentEntity::getTeacherId, teacher.getTeacherId());
        Long guidanceCount = projectCommentMapper.selectCount(commentWrapper);

        // 统计指导项目数（去重）
        List<ProjectCommentEntity> comments = projectCommentMapper.selectList(commentWrapper);
        long projectCount = comments.stream()
                .map(ProjectCommentEntity::getProjectId)
                .distinct()
                .count();

        // 计算平均评分（如果有rating字段的话，目前表中没有，使用默认值）
        Double ratingScore = teacher.getRatingScore() != null ? teacher.getRatingScore().doubleValue() : 5.0;

        // 组装VO
        TeacherProfileVo vo = new TeacherProfileVo();
        vo.setTeacherId(teacher.getTeacherId());
        vo.setUserId(teacher.getUserId());
        vo.setName(teacher.getName());
        vo.setEmployeeNo(teacher.getEmployeeNo());
        vo.setSchoolId(teacher.getSchoolId());
        vo.setCollegeName(teacher.getCollegeName());
        vo.setTitle(teacher.getTitle());
        vo.setExpertise(teacher.getExpertise());
        vo.setGuidanceCount(guidanceCount.intValue());
        vo.setProjectCount((int) projectCount);
        vo.setRatingScore(ratingScore);
        vo.setStatus(1); // 默认正常状态

        if (user != null) {
            vo.setAvatar(user.getAvatar());
            vo.setPhone(maskPhone(user.getPhone()));
            vo.setEmail(user.getEmail());
        }

        if (school != null) {
            vo.setSchoolName(school.getName());
        }

        if (teacher.getCreateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setCreateTime(sdf.format(teacher.getCreateTime()));
        }

        return vo;
    }

    /**
     * 手机号脱敏处理（中间4位显示为*）
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
