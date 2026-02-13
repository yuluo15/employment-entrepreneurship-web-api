package com.gxcj.service;

import com.gxcj.entity.vo.school.SchoolDashboardVo;

public interface SchoolDashboardService {
    
    /**
     * 获取学校工作台数据
     * 
     * @param userId 用户ID
     * @return 工作台数据
     */
    SchoolDashboardVo getDashboardData(String userId);
}
