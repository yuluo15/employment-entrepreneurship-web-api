package com.gxcj.entity.dto.school;

import lombok.Data;

@Data
public class SchoolProjectAuditDto {
    private String projectId;
    private String status;  // 1=通过, 2=驳回
    private String auditReason;
}
