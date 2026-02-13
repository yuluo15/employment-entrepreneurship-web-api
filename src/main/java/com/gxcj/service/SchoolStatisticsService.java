package com.gxcj.service;

import com.gxcj.entity.vo.school.SchoolEmploymentStatsVo;
import com.gxcj.entity.vo.school.SchoolEntrepreneurshipStatsVo;

public interface SchoolStatisticsService {
    
    /**
     * 获取学校端就业统计数据
     * 
     * @param graduationYear 毕业年份
     * @param collegeName 学院名称（可选）
     * @param userId 用户ID
     * @return 就业统计数据
     */
    SchoolEmploymentStatsVo getEmploymentStats(Integer graduationYear, String collegeName, String userId);
    
    /**
     * 获取学校端创业统计数据
     * 
     * @param collegeName 学院名称（可选）
     * @param userId 用户ID
     * @return 创业统计数据
     */
    SchoolEntrepreneurshipStatsVo getEntrepreneurshipStats(String collegeName, String userId);
}
