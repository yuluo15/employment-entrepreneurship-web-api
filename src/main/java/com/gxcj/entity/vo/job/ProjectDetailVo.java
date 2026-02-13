package com.gxcj.entity.vo.job;

import lombok.Data;

import java.util.List;

@Data
public class ProjectDetailVo {
    private String projectId;
    private String title;          // projectName
    private String logo;
    private String slogan;         // 用 description 截取或单独字段
    private String status;         // status (字典翻译: 孵化中)
    private List<String> tags;     // domain (领域) + status (状态)

    private String schoolName;     // 需要关联查 sys_school
    private String leaderName;     // 负责人 (查 sys_user 或 biz_student)
    private String leaderPhone;    // 负责人手机号
    private String mentorName;    // mentorName
    private Integer teamSize;     // teamSize

    private String description;      // 项目简介
    private String needs;          // 需求描述
    private Boolean isCollected;   // 是否收藏
    private Boolean isOwner;       // 是否是项目负责人
    private String applicationStatus;  // 申请状态：null=未申请，PENDING=待审核，APPROVED=已通过，REJECTED=已拒绝
}
