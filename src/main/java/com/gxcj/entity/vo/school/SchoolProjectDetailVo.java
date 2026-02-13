package com.gxcj.entity.vo.school;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolProjectDetailVo extends SchoolProjectVo {
    private String userId;
    private String schoolId;
    private String slogan;
    private String description;
    private String needs;
    private String mentorId;
    private String mentorName;
    private String mentorComment;
    private String auditReason;
    private String auditTime;
    private String updateTime;
}
