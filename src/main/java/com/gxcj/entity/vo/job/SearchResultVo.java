package com.gxcj.entity.vo.job;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultVo {
    // --- 1. 核心区分 ---
    private String type;           // 枚举值: "JOB", "COMPANY", "PROJECT"
    private String id;

    // --- 2. 通用展示字段 (列表页三要素) ---
    private String title;          // 核心标题 (职位名 / 公司名 / 项目名)
    private String subTitle;       // 副标题 (公司名+规模 / 行业+规模 / 指导老师+规模)
    private String avatar;         // 图片 (HR头像 / 公司Logo / 项目Logo)
    private String nickName;       // 昵称 （ HR昵称 （昵称 · 职位） / 公司名称 / 项目负责人昵称）
    private String location;       // 地点 (城市 / 公司地址 / 学校)

    // --- 3. 核心特征 (右侧高亮或标签) ---
    private String highlight;      // 高亮文本 (薪资 "12k-20k" / 融资阶段 / 项目状态)
    private List<String> tags;     // 标签列表 (["双休","五险"] / ["互联网","大厂"] / ["互联网+","智慧农业"])

    // --- 4. 详情跳转需要的额外ID ---
    private String targetId;       // 额外跳转目标的ID (例如 可以点击hr的头像，跳转到 hr详情页）
    private String ownerId;        // 归属者ID (Job -> CompanyId, Project -> SchoolId)
}
