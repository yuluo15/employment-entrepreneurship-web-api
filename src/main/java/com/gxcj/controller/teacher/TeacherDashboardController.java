package com.gxcj.controller.teacher;

import com.gxcj.context.UserContext;
import com.gxcj.entity.vo.teacher.TeacherDashboardVo;
import com.gxcj.result.Result;
import com.gxcj.service.TeacherDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师端工作台接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
public class TeacherDashboardController {

    @Autowired
    private TeacherDashboardService dashboardService;

    /**
     * 获取教师工作台数据
     * 
     * @return 工作台数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<TeacherDashboardVo> getDashboard() {
        log.info("获取教师工作台数据");
        
        String userId = UserContext.getUserId();
        
        TeacherDashboardVo data = dashboardService.getDashboardData(userId);
        
        return Result.success(data);
    }
}
