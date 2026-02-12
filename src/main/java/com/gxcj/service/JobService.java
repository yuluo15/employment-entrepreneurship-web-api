package com.gxcj.service;

import com.gxcj.controller.student.JobController;
import com.gxcj.entity.JobEntity;
import com.gxcj.entity.query.CompanyJobQuery;
import com.gxcj.entity.vo.job.JobDetailVo;
import com.gxcj.entity.vo.job.SearchResultVo;
import com.gxcj.result.PageResult;

public interface JobService {
    PageResult<SearchResultVo> search(JobController.SearchReq req);

    JobDetailVo getJobDetail(String jobId);
    
    /**
     * 企业HR发布新职位
     */
    String addJob(JobEntity jobEntity, String userId);
    
    /**
     * 企业HR更新职位
     */
    void updateJob(JobEntity jobEntity, String userId);
    
    /**
     * 企业HR获取职位详情（用于编辑）
     */
    JobEntity getJobForEdit(String jobId, String userId);
    
    /**
     * 企业HR获取职位列表（分页）
     */
    PageResult<JobEntity> getCompanyJobList(CompanyJobQuery query, String userId);
    
    /**
     * 企业HR删除职位
     */
    void deleteJob(String jobId, String userId);
    
    /**
     * 企业HR下架职位
     */
    void offlineJob(String jobId, String userId);
    
    /**
     * 企业HR上架职位
     */
    void onlineJob(String jobId, String userId);
}
