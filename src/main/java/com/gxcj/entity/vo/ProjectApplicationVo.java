package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class ProjectApplicationVo {
    private String id;
    private String projectId;
    private String projectName;
    private String projectLogo;
    private String leaderName;
    private String applicationReason;
    private String skills;
    private String status;
    private String replyMessage;
    private String createTime;
    private String replyTime;
}
