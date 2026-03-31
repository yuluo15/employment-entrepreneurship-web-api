package com.gxcj.service;

import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.query.TeacherQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.entity.vo.teacher.TeacherListVo;
import com.gxcj.result.PageResult;

import java.util.List;

public interface TeacherService {
    PageResult<TeacherEntity> list(TeacherQuery teacherQuery);

    List<DictDataVo> getDictDataByDictType(String dictType);

    PageResult<TeacherListVo> getTeacherList(String keyword, String expertise, Integer pageNum, Integer pageSize);
}
