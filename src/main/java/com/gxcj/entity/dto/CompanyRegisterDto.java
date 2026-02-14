package com.gxcj.entity.dto;

import lombok.Data;

@Data
public class CompanyRegisterDto {
    private String adminAccount;    // 管理员邮箱
    private String emailCode;       // 邮箱验证码
    private String password;        // 密码
    private String name;            // 企业全称
    private String code;            // 统一社会信用代码
    private String industry;        // 所属行业
    private String scale;           // 人员规模
    private String licenseUrl;      // 营业执照图片URL
    private String contactPerson;   // 联系人姓名
    private String contactPhone;    // 联系电话
}
