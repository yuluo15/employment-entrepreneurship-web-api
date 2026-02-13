package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.vo.school.SchoolDashboardVo;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学校端工作台接口
 */
@Slf4j
@RestController
@RequestMapping("/api/school")
public class SchoolDashboardController {

    @Autowired
    private SchoolDashboardService dashboardService;

    /**
     * 获取学校工作台数据
     * 
     * @return 工作台数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
    public Result<SchoolDashboardVo> getDashboard() {
        log.info("获取学校工作台数据");
        
        String userId = UserContext.getUserId();
        
        SchoolDashboardVo data = dashboardService.getDashboardData(userId);
        
        return Result.success(data);
    }
}
