package com.gxcj.service;

import com.gxcj.entity.vo.CompanyDashboardVo;

public interface CompanyDashboardService {
    
    /**
     * 获取企业工作台数据
     */
    CompanyDashboardVo getDashboard(String userId);
}
