package com.gxcj.entity.vo.job;

import lombok.Data;

@Data
public class MyJoinedProjectVo {
    private String applicationId;
    private String projectId;
    private String projectName;
    private String logo;
    private String domain;
    private String leaderName;
    private Integer teamSize;
    private String status;
    private String applicationReason;
    private String replyMessage;
    private String applyTime;
    private String replyTime;
}
