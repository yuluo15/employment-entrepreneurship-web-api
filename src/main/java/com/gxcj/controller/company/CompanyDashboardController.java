package com.gxcj.controller.company;

import com.gxcj.context.UserContext;
import com.gxcj.entity.vo.CompanyDashboardVo;
import com.gxcj.result.Result;
import com.gxcj.service.CompanyDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业端工作台接口
 */
@RestController
@RequestMapping("/api/company")
public class CompanyDashboardController {

    @Autowired
    private CompanyDashboardService dashboardService;

    /**
     * 获取工作台数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<CompanyDashboardVo> getDashboard() {
        CompanyDashboardVo dashboard = dashboardService.getDashboard(UserContext.getUserId());
        return Result.success(dashboard);
    }
}
