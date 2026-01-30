package com.gxcj.controller.admin;

import com.gxcj.entity.CompanyEntity;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companyMgr")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<CompanyEntity>> list(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                                  @RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String code,
                                                  @RequestParam(required = false) Integer status
                                                  ){
        PageResult<CompanyEntity> companyPage = companyService.list(pageNum, pageSize, name, code, status);
        return Result.success(companyPage);
    }

//    @PostMapping("/create")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public Result<CompanyEntity> create(@RequestBody CompanyCreateReq req){
//        companyService.create(req);
//        return Result.success();
//    }

    @PostMapping("/audit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<CompanyEntity> auditCompany(@RequestBody @Valid AuditCompanyReq req){
        companyService.auditCompany(req);
        return Result.success();
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<CompanyEntity> update(@RequestBody @Valid CompanyCreateReq req){
        companyService.update(req);
        return Result.success();
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<CompanyEntity> delete(@PathVariable String id){
        companyService.delete(id);
        return Result.success();
    }

    @PostMapping("/updateStatus")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<CompanyEntity> updateStatus(@RequestBody @Valid AuditCompanyReq req){
        companyService.auditCompany(req);
        return Result.success();
    }


    @Data
    public static class CompanyCreateReq{
        private String id;
        @NotBlank
        private String logo;
        private String licenseUrl;
        @NotBlank
        private String name;
        @NotBlank
        private String code;
        @NotBlank
        private String adminAccount;
        @NotBlank
        private String password;
        private String industry;
        private String scale;
        private String contactPerson;
        @NotBlank
        private String contactPhone;
        private String email;
        private String address;
        private String description;
    }

    @Data
    public static class AuditCompanyReq{
        @NotBlank
        private String id;
        @NotNull
        private Integer status;
        private String reason;
    }


}
