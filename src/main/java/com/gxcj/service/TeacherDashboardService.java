package com.gxcj.service;

import com.gxcj.entity.vo.teacher.TeacherDashboardVo;

public interface TeacherDashboardService {
    
    /**
     * 获取教师工作台数据
     * 
     * @param userId 用户ID
     * @return 工作台数据
     */
    TeacherDashboardVo getDashboardData(String userId);
}
