package com.gxcj.controller.company;

import com.gxcj.context.UserContext;
import com.gxcj.entity.CompanyEntity;
import com.gxcj.result.Result;
import com.gxcj.service.CompanyProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 企业端企业信息管理接口
 */
@RestController
@RequestMapping("/api/company/profile")
@Validated
public class CompanyProfileController {

    @Autowired
    private CompanyProfileService companyProfileService;

    /**
     * 1. 获取企业信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<CompanyEntity> getCompanyInfo() {
        CompanyEntity company = companyProfileService.getCompanyInfo(UserContext.getUserId());
        return Result.success(company);
    }

    /**
     * 2. 更新企业信息
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> updateCompanyInfo(@RequestBody @Valid CompanyUpdateReq req) {
        companyProfileService.updateCompanyInfo(req, UserContext.getUserId());
        return Result.success();
    }

    // ==================== 请求对象 ====================

    @Data
    public static class CompanyUpdateReq {
        @NotBlank(message = "企业ID不能为空")
        private String id;

        @NotBlank(message = "企业名称不能为空")
        @Size(min = 2, max = 100, message = "企业名称长度为2-100字符")
        private String name;

        @NotBlank(message = "统一社会信用代码不能为空")
        @Pattern(regexp = "^[0-9A-Z]{18}$", message = "统一社会信用代码格式不正确")
        private String code;

        @NotBlank(message = "所属行业不能为空")
        private String industry;

        @NotBlank(message = "企业规模不能为空")
        private String scale;

        private String fundingStage;  // 融资阶段

        private String website;

        private String logo;

        @NotBlank(message = "企业地址不能为空")
        private String address;

        @NotBlank(message = "联系人不能为空")
        private String contactPerson;

        @NotBlank(message = "联系电话不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String contactPhone;

        @NotBlank(message = "联系邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "企业简介不能为空")
        @Size(min = 20, max = 2000, message = "企业简介长度为20-2000字符")
        private String description;

        @Size(max = 1000, message = "福利待遇最多1000字符")
        private String tags;  // 标签（福利待遇）

        private String images;  // 公司环境图
    }
}
