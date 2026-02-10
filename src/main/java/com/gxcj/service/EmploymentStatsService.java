package com.gxcj.service;

import com.gxcj.entity.vo.EmploymentStatsVo;

public interface EmploymentStatsService {
    /**
     * 获取就业统计数据
     * @param graduationYear 毕业年份
     * @return 就业统计数据
     */
    EmploymentStatsVo getEmploymentStats(Integer graduationYear);
}
