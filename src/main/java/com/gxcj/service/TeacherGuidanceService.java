package com.gxcj.service;

import com.gxcj.entity.dto.TeacherGuidanceAddDto;
import com.gxcj.entity.query.TeacherGuidanceQuery;
import com.gxcj.entity.vo.teacher.TeacherGuidanceStatsVo;
import com.gxcj.entity.vo.teacher.TeacherGuidanceVo;
import com.gxcj.result.PageResult;

public interface TeacherGuidanceService {
    TeacherGuidanceStatsVo getStats();
    
    PageResult<TeacherGuidanceVo> getList(TeacherGuidanceQuery query);
    
    String addGuidance(TeacherGuidanceAddDto dto);
}
