package com.gxcj.controller.student;

import com.gxcj.entity.vo.job.CompanyDetailVo;
import com.gxcj.result.Result;
import com.gxcj.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/company")
public class MobileCompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<CompanyDetailVo> getCompanyDetail(@PathVariable("id") String companyId){
        CompanyDetailVo companyDetailVo = companyService.getCompanyDetail(companyId);
        return Result.success(companyDetailVo);
    }

}
