package com.gxcj.controller.admin;

import com.gxcj.entity.vo.EmploymentStatsVo;
import com.gxcj.result.Result;
import com.gxcj.service.EmploymentStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
@RequestMapping("/api/employment/stats")
public class EmploymentStatsController {

    @Autowired
    private EmploymentStatsService employmentStatsService;

    /**
     * 就业数据统计概览
     * @param graduationYear 毕业年份，可选，默认当前年份
     * @return 就业统计数据
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<EmploymentStatsVo> getOverview(
            @RequestParam(required = false) Integer graduationYear) {
        
        // 如果未传入年份，使用当前年份
        if (graduationYear == null) {
            graduationYear = Year.now().getValue();
        }
        
        EmploymentStatsVo stats = employmentStatsService.getEmploymentStats(graduationYear);
        return Result.success(stats);
    }
}
