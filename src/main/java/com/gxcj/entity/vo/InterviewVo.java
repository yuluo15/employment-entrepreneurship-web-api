package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class InterviewVo {
    private String id;
    private String deliveryId;
    private String studentId;
    private String studentName;
    private String studentPhone;
    private String jobId;
    private String jobName;
    private String interviewTime;
    private Integer duration;
    private Integer type;
    private String location;
    private String notes;
    private Integer status;
    private Integer interviewScore;
    private String interviewComment;
    private String interviewResult;  // 面试结果：PASS/FAIL
}
