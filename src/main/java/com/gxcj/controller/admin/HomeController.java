package com.gxcj.controller.admin;

import com.gxcj.entity.vo.HomeOverviewVo;
import com.gxcj.result.Result;
import com.gxcj.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员首页接口
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * 获取首页概览数据
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<HomeOverviewVo> getOverview() {
        HomeOverviewVo overview = homeService.getOverview();
        return Result.success(overview);
    }
}
