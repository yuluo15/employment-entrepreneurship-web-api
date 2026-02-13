package com.gxcj.service;

import com.gxcj.entity.query.school.SchoolGuidanceQuery;
import com.gxcj.entity.vo.school.SchoolGuidanceDetailVo;
import com.gxcj.entity.vo.school.SchoolGuidanceVo;
import com.gxcj.result.PageResult;

public interface SchoolGuidanceService {
    
    /**
     * 获取指导记录列表
     */
    PageResult<SchoolGuidanceVo> getGuidanceList(SchoolGuidanceQuery query, String userId);
    
    /**
     * 获取指导记录详情
     */
    SchoolGuidanceDetailVo getGuidanceDetail(String id, String userId);
}
