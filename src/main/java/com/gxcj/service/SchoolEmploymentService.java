package com.gxcj.service;

import com.gxcj.entity.dto.school.SchoolEmploymentUpdateDto;
import com.gxcj.entity.query.school.SchoolEmploymentQuery;
import com.gxcj.entity.vo.school.SchoolEmploymentStatsVo;
import com.gxcj.entity.vo.school.SchoolEmploymentVo;
import com.gxcj.result.PageResult;
import jakarta.servlet.http.HttpServletResponse;

public interface SchoolEmploymentService {
    
    /**
     * 获取就业列表
     */
    PageResult<SchoolEmploymentVo> getEmploymentList(SchoolEmploymentQuery query, String userId);
    
    /**
     * 获取就业详情
     */
    SchoolEmploymentVo getEmploymentDetail(String studentId, String userId);
    
    /**
     * 更新就业信息
     */
    void updateEmploymentInfo(SchoolEmploymentUpdateDto dto, String userId);
    
    /**
     * 获取就业统计
     */
    SchoolEmploymentStatsVo getEmploymentStats(SchoolEmploymentQuery query, String userId);
    
    /**
     * 导出就业数据
     */
    void exportEmploymentData(SchoolEmploymentQuery query, String userId, HttpServletResponse response);
}
