package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.*;
import com.gxcj.entity.query.TeacherQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.entity.vo.teacher.TeacherListVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private DictDataMapper dictDataMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public PageResult<TeacherEntity> list(TeacherQuery teacherQuery) {
//        PageHelper.startPage(teacherQuery.getPageNum(), teacherQuery.getPageSize());
        Page<TeacherEntity> page = teacherMapper.selectPage(new Page<>(teacherQuery.getPageNum(), teacherQuery.getPageSize()), new LambdaQueryWrapper<TeacherEntity>()
                .eq(StringUtils.isNotEmpty(teacherQuery.getSchoolId()), TeacherEntity::getSchoolId, teacherQuery.getSchoolId())
                .eq(StringUtils.isNotEmpty(teacherQuery.getName()), TeacherEntity::getName, teacherQuery.getName())
                .eq(StringUtils.isNotEmpty(teacherQuery.getTitle()), TeacherEntity::getTitle, teacherQuery.getTitle())
                .eq(StringUtils.isNotEmpty(teacherQuery.getCollegeName()), TeacherEntity::getCollegeName, teacherQuery.getCollegeName()));
//        PageInfo<TeacherEntity> pageInfo = new PageInfo<>(teacherEntities);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public List<DictDataVo> getDictDataByDictType(String dictType) {
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(StringUtils.isNotEmpty(dictType), DictDataEntity::getDictType, dictType));
        List<DictDataVo> dictDataVos = list.stream().map(dictDataEntity -> DictDataVo.builder()
                .id(dictDataEntity.getId())
                .dictSort(dictDataEntity.getDictSort())
                .dictLabel(dictDataEntity.getDictLabel())
                .dictValue(dictDataEntity.getDictValue()).build()).toList();
        return dictDataVos;
    }

    @Override
    public PageResult<TeacherListVo> getTeacherList(String keyword, String expertise, Integer pageNum, Integer pageSize) {
        String userId = UserContext.getUserId();

        // 获取当前学生的学校ID
        LambdaQueryWrapper<StudentEntity> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentEntity::getUserId, userId);
        StudentEntity student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 查询教师列表（本校教师）
        LambdaQueryWrapper<TeacherEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherEntity::getSchoolId, student.getSchoolId());

        // 关键词搜索（教师姓名或工号）
        if (StringUtils.isNotEmpty(keyword)) {
            wrapper.and(w -> w.like(TeacherEntity::getName, keyword)
                    .or().like(TeacherEntity::getEmployeeNo, keyword));
        }

        // 专业领域筛选
        if (StringUtils.isNotEmpty(expertise)) {
            wrapper.like(TeacherEntity::getExpertise, expertise);
        }

        wrapper.orderByDesc(TeacherEntity::getCreateTime);

        Page<TeacherEntity> page = new Page<>(pageNum, pageSize);
        Page<TeacherEntity> result = teacherMapper.selectPage(page, wrapper);

        List<TeacherListVo> voList = new ArrayList<>();
        if (!result.getRecords().isEmpty()) {
            // 批量查询用户信息（获取头像）
            List<String> userIds = result.getRecords().stream()
                    .map(TeacherEntity::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<UserEntity> users = userMapper.selectBatchIds(userIds);
            Map<String, UserEntity> userMap = users.stream()
                    .collect(Collectors.toMap(UserEntity::getId, u -> u));

            // 查询学校信息
            SchoolEntity school = schoolMapper.selectById(student.getSchoolId());

            // 批量查询每个教师指导的项目数量
            for (TeacherEntity teacher : result.getRecords()) {
                TeacherListVo vo = new TeacherListVo();
                vo.setTeacherId(teacher.getTeacherId());
                vo.setTeacherName(teacher.getName());
                vo.setTeacherNo(teacher.getEmployeeNo());
                vo.setTitle(teacher.getTitle());
                vo.setExpertise(teacher.getExpertise());
                vo.setExpertiseCode(teacher.getExpertise()); // 使用expertise作为code
                vo.setCollegeName(teacher.getCollegeName());
                vo.setIntro(teacher.getIntro());

                if (school != null) {
                    vo.setSchoolName(school.getName());
                }

                UserEntity user = userMap.get(teacher.getUserId());
                if (user != null) {
                    vo.setAvatar(user.getAvatar());
                }

                // 查询指导项目数量
                Long guidanceCount = projectMapper.selectCount(
                        new LambdaQueryWrapper<ProjectEntity>()
                                .eq(ProjectEntity::getMentorId, teacher.getTeacherId())
                );
                vo.setGuidanceCount(guidanceCount.intValue());

                voList.add(vo);
            }
        }

        return new PageResult<>(result.getTotal(), voList);
    }
}
