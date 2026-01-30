package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.query.StudentQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.mapper.DictDataMapper;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.StudentService;
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
}
