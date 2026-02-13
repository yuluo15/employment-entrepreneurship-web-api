package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolProjectVo {
    private String projectId;
    private String projectName;
    private String logo;
    private String studentName;
    private String studentNo;
    private String domain;
    private Integer teamSize;
    private Integer jobsCreated;
    private String status;
    private String createTime;
}
