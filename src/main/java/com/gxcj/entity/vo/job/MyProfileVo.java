package com.gxcj.entity.vo.job;

import lombok.Data;

@Data
public class MyProfileVo {
    // --- 核心身份信息 (通常不允许修改，或者只能改一次) ---
    private String id;           // 学生ID
    private String username;     // 账号 (学号)
    private String realName;     // 真实姓名
    private String schoolName;   // 学校名称
    private String major;        // 专业

    // --- 可编辑信息 ---
    private String avatar;       // 头像 URL
    private Integer gender;      // 性别 (0:未知, 1:男, 2:女)
    private String phone;        // 手机号
    private String email;        // 邮箱
    private String graduationYear; // 毕业年份 (2026)
    private String education;    // 学历 (本科/硕士)
}
