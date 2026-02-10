package com.gxcj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.vo.HomeOverviewVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HomeMapper extends BaseMapper<SchoolEntity> {
    
    /**
     * 获取学校排行（按学生数量）
     */
    List<HomeOverviewVo.SchoolRankItem> getSchoolRank();
    
    /**
     * 获取创业领域分布
     */
    List<HomeOverviewVo.DomainDistributionItem> getDomainDistribution();
    
    /**
     * 获取最新入驻企业
     */
    List<HomeOverviewVo.LatestCompanyItem> getLatestCompanies();
}
