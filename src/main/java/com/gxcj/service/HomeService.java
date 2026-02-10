package com.gxcj.service;

import com.gxcj.entity.vo.HomeOverviewVo;

/**
 * 首页服务接口
 */
public interface HomeService {
    
    /**
     * 获取首页概览数据
     */
    HomeOverviewVo getOverview();
}
