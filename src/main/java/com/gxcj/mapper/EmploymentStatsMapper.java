package com.gxcj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.vo.EmploymentStatsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface EmploymentStatsMapper extends BaseMapper<StudentEntity> {
    
    /**
     * 获取KPI指标数据
     */
    Map<String, Object> getKpiData(@Param("graduationYear") Integer graduationYear);
    
    /**
     * 获取各校就业率排行
     */
    List<EmploymentStatsVo.SchoolRankItem> getSchoolRank(@Param("graduationYear") Integer graduationYear);
    
    /**
     * 获取就业状态分布
     */
    List<EmploymentStatsVo.StatusDistributionItem> getEmploymentStatusDistribution(@Param("graduationYear") Integer graduationYear);
    
    /**
     * 获取薪资分布
     */
    List<EmploymentStatsVo.SalaryDistributionItem> getSalaryDistribution(@Param("graduationYear") Integer graduationYear);
    
    /**
     * 获取热门行业TOP5
     */
    List<EmploymentStatsVo.IndustryItem> getHotIndustries(@Param("graduationYear") Integer graduationYear);
    
    /**
     * 获取创业项目统计
     */
    Map<String, Object> getEntrepreneurshipStats();
    
    /**
     * 获取创业领域分布
     */
    List<EmploymentStatsVo.DomainDistributionItem> getDomainDistribution();
}
