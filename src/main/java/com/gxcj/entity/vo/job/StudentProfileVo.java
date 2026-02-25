package com.gxcj.entity.vo.job;

import lombok.Data;

@Data
public class StudentProfileVo {
    // 基础信息
    private String name;
    private String avatar;
    private String schoolName;
    private String major;
    private Integer graduationYear; // e.g. "2026"

    // 统计数字
    private Integer deliveredCount;
    private Integer interviewCount;
    private Integer collectionCount;
    private Integer offerCount;

    // 简历信息
    private Integer resumeComplete; // 进度条数值
    private String resumeId;        // 方便前端跳编辑页

    // 创业信息
    private Integer projectCount;
    private Integer joinedProjectCount; // 我加入的项目数量
}
