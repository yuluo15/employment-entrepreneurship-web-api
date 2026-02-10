package com.gxcj.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 创业项目VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrepProjectVo {
    private String projectId;
    private String projectName;
    private String logo;
    private String slogan;
    private String schoolId;
    private String schoolName;
    private String leaderName;
    private String mentorName;
    private Integer teamSize;
    private String domain;
    private String domainLabel;
    private String status;
    private String description;
    private String needs;
    private String auditReason;
    private Timestamp createTime;
}
