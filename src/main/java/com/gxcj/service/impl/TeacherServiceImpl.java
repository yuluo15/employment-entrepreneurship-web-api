package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.query.TeacherQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.mapper.DictDataMapper;
import com.gxcj.mapper.TeacherMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private DictDataMapper dictDataMapper;

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
}
