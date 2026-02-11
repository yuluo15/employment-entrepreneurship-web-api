package com.gxcj.service;

import com.gxcj.entity.query.JobAuditQuery;
import com.gxcj.entity.vo.JobAuditVo;
import com.gxcj.result.PageResult;

public interface JobAuditService {
    /**
     * 获取岗位审核列表
     */
    PageResult<JobAuditVo> getJobAuditList(JobAuditQuery query);

    /**
     * 审核岗位
     */
    void auditJob(String jobId, Integer audit, String reason);

    /**
     * 下架岗位
     */
    void offlineJob(String jobId);
}
