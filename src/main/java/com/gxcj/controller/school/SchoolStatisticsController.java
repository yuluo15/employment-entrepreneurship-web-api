package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.vo.school.SchoolEmploymentStatsVo;
import com.gxcj.entity.vo.school.SchoolEntrepreneurshipStatsVo;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学校端统计接口
 */
@Slf4j
@RestController
@RequestMapping("/api/school/statistics")
public class SchoolStatisticsController {

    @Autowired
    private SchoolStatisticsService statisticsService;

    /**
     * 获取就业统计数据
     * 
     * @param graduationYear 毕业年份
     * @param collegeName 学院名称（可选）
     * @return 就业统计数据
     */
    @GetMapping("/employment")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
    public Result<SchoolEmploymentStatsVo> getEmploymentStats(
            @RequestParam Integer graduationYear,
            @RequestParam(required = false) String collegeName) {
        
        log.info("获取学校端就业统计数据: graduationYear={}, collegeName={}", graduationYear, collegeName);
        
        String userId = UserContext.getUserId();
        
        SchoolEmploymentStatsVo stats = statisticsService.getEmploymentStats(
                graduationYear, collegeName, userId);
        
        return Result.success(stats);
    }

    /**
     * 获取创业统计数据
     * 
     * @param collegeName 学院名称（可选）
     * @return 创业统计数据
     */
    @GetMapping("/entrepreneurship")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
    public Result<SchoolEntrepreneurshipStatsVo> getEntrepreneurshipStats(
            @RequestParam(required = false) String collegeName) {
        
        log.info("获取学校端创业统计数据: collegeName={}", collegeName);
        
        String userId = UserContext.getUserId();
        
        SchoolEntrepreneurshipStatsVo stats = statisticsService.getEntrepreneurshipStats(
                collegeName, userId);
        
        return Result.success(stats);
    }
}
