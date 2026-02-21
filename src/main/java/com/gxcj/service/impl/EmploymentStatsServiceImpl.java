package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.vo.EmploymentStatsVo;
import com.gxcj.mapper.DictDataMapper;
import com.gxcj.mapper.EmploymentStatsMapper;
import com.gxcj.service.EmploymentStatsService;
import com.gxcj.stutas.DictTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class EmploymentStatsServiceImpl implements EmploymentStatsService {

    @Autowired
    private EmploymentStatsMapper employmentStatsMapper;
    @Autowired
    private DictDataMapper dictDataMapper;

    @Override
    public EmploymentStatsVo getEmploymentStats(Integer graduationYear) {
        log.info("开始统计{}年就业数据", graduationYear);
        List<DictDataEntity> list = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(DictDataEntity::getDictType, DictTypeEnum.sys_industry));
        Map<String, String> map = list.stream().collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (x, y) -> x));

        // 1. 获取KPI指标
        EmploymentStatsVo.KpiData kpi = getKpiData(graduationYear);
        
        // 2. 获取各校就业率排行
        List<EmploymentStatsVo.SchoolRankItem> schoolRank = employmentStatsMapper.getSchoolRank(graduationYear);
        
        // 3. 获取就业状态分布
        List<EmploymentStatsVo.StatusDistributionItem> employmentStatus = 
                employmentStatsMapper.getEmploymentStatusDistribution(graduationYear);
        
        // 4. 获取薪资分布
        List<EmploymentStatsVo.SalaryDistributionItem> salaryDistribution = 
                employmentStatsMapper.getSalaryDistribution(graduationYear);
        
        // 5. 获取热门行业
        List<EmploymentStatsVo.IndustryItem> hotIndustries = 
                employmentStatsMapper.getHotIndustries(graduationYear);
        List<EmploymentStatsVo.IndustryItem> industryItemList = hotIndustries.stream().map(item -> {
            EmploymentStatsVo.IndustryItem industryItem = new EmploymentStatsVo.IndustryItem();
            BeanUtils.copyProperties(item, industryItem);
            industryItem.setIndustry(String.join(",", Arrays.stream(item.getIndustry().split(","))
                    .filter(map::containsKey).map(map::get).toList()));
            return industryItem;
        }).toList();

        // 6. 获取创业项目统计
        EmploymentStatsVo.EntrepreneurshipData entrepreneurship = getEntrepreneurshipData();
        
        // 组装返回数据
        return EmploymentStatsVo.builder()
                .kpi(kpi)
                .schoolRank(schoolRank != null ? schoolRank : new ArrayList<>())
                .employmentStatus(employmentStatus != null ? employmentStatus : new ArrayList<>())
                .salaryDistribution(salaryDistribution != null ? salaryDistribution : new ArrayList<>())
                .hotIndustries(industryItemList)
                .entrepreneurship(entrepreneurship)
                .build();
    }

    /**
     * 获取KPI指标数据
     */
    private EmploymentStatsVo.KpiData getKpiData(Integer graduationYear) {
        Map<String, Object> kpiMap = employmentStatsMapper.getKpiData(graduationYear);
        
        // 毕业生总数
        Integer totalGraduates = getIntValue(kpiMap, "totalgraduates");
        
        // 已就业人数
        Integer employedCount = getIntValue(kpiMap, "employedcount");
        
        // 就业率
        Double employmentRateNum = getDoubleValue(kpiMap, "employmentrate");
        String employmentRate = employmentRateNum != null ? employmentRateNum + "%" : "0.0%";
        
        // 创业人数
        Integer entrepreneurshipCount = getIntValue(kpiMap, "entrepreneurshipcount");
        
        // 创业率
        String entrepreneurshipRate = "0.0%";
        if (totalGraduates != null && totalGraduates > 0 && entrepreneurshipCount != null) {
            double rate = entrepreneurshipCount * 100.0 / totalGraduates;
            entrepreneurshipRate = String.format("%.1f%%", rate);
        }
        
        // 平均薪资（单位：k）
        Double avgSalaryNum = getDoubleValue(kpiMap, "avgsalary");
        String avgSalary = avgSalaryNum != null ? String.format("%.1f", avgSalaryNum) : "0";
        
        return EmploymentStatsVo.KpiData.builder()
                .totalGraduates(totalGraduates != null ? totalGraduates : 0)
                .employedCount(employedCount != null ? employedCount : 0)
                .employmentRate(employmentRate)
                .entrepreneurshipCount(entrepreneurshipCount != null ? entrepreneurshipCount : 0)
                .entrepreneurshipRate(entrepreneurshipRate)
                .avgSalary(avgSalary)
                .build();
    }

    /**
     * 获取创业项目统计数据
     */
    private EmploymentStatsVo.EntrepreneurshipData getEntrepreneurshipData() {
        Map<String, Object> statsMap = employmentStatsMapper.getEntrepreneurshipStats();
        List<EmploymentStatsVo.DomainDistributionItem> domainDistribution = 
                employmentStatsMapper.getDomainDistribution();
        
        return EmploymentStatsVo.EntrepreneurshipData.builder()
                .totalProjects(getIntValue(statsMap, "totalprojects"))
                .approvedProjects(getIntValue(statsMap, "approvedprojects"))
                .pendingProjects(getIntValue(statsMap, "pendingprojects"))
                .domainDistribution(domainDistribution != null ? domainDistribution : new ArrayList<>())
                .build();
    }

    /**
     * 从Map中安全获取Integer值
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        if (map == null) {
            return 0;
        }
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法转换为Integer: key={}, value={}", key, value);
            return 0;
        }
    }

    /**
     * 从Map中安全获取Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法转换为Double: key={}, value={}", key, value);
            return null;
        }
    }
}
