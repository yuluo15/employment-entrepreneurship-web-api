package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.*;
import com.gxcj.entity.query.StudentQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.entity.vo.job.StudentProfileVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.StudentService;
import com.gxcj.stutas.JobDeliveryStatusEnum;
import com.gxcj.stutas.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private DictDataMapper dictDataMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private StudentResumeMapper studentResumeMapper;
    @Autowired
    private ProjectMapper projectMapper;

    public PageResult<StudentEntity> list(StudentQuery studentQuery) {
//        PageHelper.startPage(studentQuery.getPageNum(), studentQuery.getPageSize());
        Page<StudentEntity> page = studentMapper.selectPage(new Page<>(studentQuery.getPageNum(), studentQuery.getPageSize()), new LambdaQueryWrapper<StudentEntity>()
                .eq(StringUtils.isNotEmpty(studentQuery.getSchoolId()), StudentEntity::getSchoolId, studentQuery.getSchoolId())
                .eq(StringUtils.isNotEmpty(studentQuery.getStudentName()), StudentEntity::getStudentName, studentQuery.getStudentName())
                .eq(StringUtils.isNotEmpty(studentQuery.getMajorName()), StudentEntity::getMajorName, studentQuery.getMajorName())
                .eq(studentQuery.getGraduationYear() != null, StudentEntity::getGraduationYear, studentQuery.getGraduationYear())
                .eq(StringUtils.isNotEmpty(studentQuery.getEmploymentStatus()), StudentEntity::getEmploymentStatus, studentQuery.getEmploymentStatus()));
//        PageInfo<StudentEntity> pageInfo = new PageInfo<>(studentEntityList);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public List<DictDataVo> getDictDataByDictType(String dictType) {
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(StringUtils.isNotEmpty(dictType), DictDataEntity::getDictType, dictType)
                .eq(DictDataEntity::getStatus, StatusEnum.NORMAL.getCode()));
        List<DictDataVo> dictDataVos = list.stream().map(dictDataEntity -> DictDataVo.builder()
                .id(dictDataEntity.getId())
                .dictSort(dictDataEntity.getDictSort())
                .dictLabel(dictDataEntity.getDictLabel())
                .dictValue(dictDataEntity.getDictValue())
                .build()).toList();
        return dictDataVos;
    }

    @Override
    public StudentProfileVo getProfileSummary() {
        StudentProfileVo vo = new StudentProfileVo();

        StudentEntity student = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, UserContext.getUserId()));
        if (student == null){
            throw new BusinessException("学生档案不存在");
        }

        UserEntity userEntity = userMapper.selectById(UserContext.getUserId());

        vo.setName(student.getStudentName());
        vo.setAvatar(userEntity.getAvatar());
        vo.setMajor(student.getMajorName());
        vo.setGraduationYear(student.getGraduationYear());

        SchoolEntity school = schoolMapper.selectById(student.getSchoolId());
        vo.setSchoolName(school != null ? school.getName() : "未知学校");

        String studentId = student.getStudentId();

        vo.setDeliveredCount(jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getStudentId, studentId)).intValue());

        vo.setInterviewCount(jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getStudentId, studentId)
                .eq(JobDeliveryEntity::getStatus, JobDeliveryStatusEnum.INTERVIEW.getValue())).intValue());

        vo.setCollectionCount(collectionMapper.selectCount(new LambdaQueryWrapper<CollectionEntity>()
                .eq(CollectionEntity::getUserId, UserContext.getUserId())).intValue());

        vo.setOfferCount(jobDeliveryMapper.selectCount(new LambdaQueryWrapper<JobDeliveryEntity>()
                .eq(JobDeliveryEntity::getStatus, JobDeliveryStatusEnum.OFFER.getValue())).intValue());

        StudentResumeEntity resume = studentResumeMapper.selectOne(new LambdaQueryWrapper<StudentResumeEntity>()
                .eq(StudentResumeEntity::getStudentId, studentId));
        if (resume != null) {
            vo.setResumeComplete(resume.getResumeScore());
//            vo.setViewCount(resume.getViewCount());
            vo.setResumeId(resume.getResumeId());
        } else {
            vo.setResumeComplete(0);
//            vo.setViewCount(0);
        }

        Long projCount = projectMapper.selectCount(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getUserId, UserContext.getUserId()));
        vo.setProjectCount(projCount.intValue());

        return vo;
    }
}
