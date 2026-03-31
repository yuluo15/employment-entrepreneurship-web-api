package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_project_stage")
public class ProjectStageEntity {
    @TableId
    private String stageId;
    private String projectId;
    private String stageName;
    private Integer stageOrder;
    private String description;
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private Timestamp startTime;
    private Timestamp completeTime;
    private Timestamp createTime;
    private Timestamp updateTime;
}
