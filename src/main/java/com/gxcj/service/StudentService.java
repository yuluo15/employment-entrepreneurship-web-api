package com.gxcj.service;

import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.query.StudentQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.entity.vo.job.StudentProfileVo;
import com.gxcj.result.PageResult;
import jakarta.validation.Valid;

import java.util.List;

public interface StudentService {
    PageResult<StudentEntity> list(StudentQuery studentQuery);

    List<DictDataVo> getDictDataByDictType(String dictType);

    StudentProfileVo getProfileSummary();
}
