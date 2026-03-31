package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class ProjectStageVo {
    private String stageId;
    private String stageName;
    private Integer stageOrder;
    private String description;
    private String status;
    private String startTime;
    private String completeTime;
    private Integer guidanceCount;
    private Boolean canGuidance; // 教师端使用，是否可以指导
}
