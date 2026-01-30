package com.gxcj.service;

import com.gxcj.controller.student.JobController;
import com.gxcj.entity.vo.job.JobDetailVo;
import com.gxcj.entity.vo.job.SearchResultVo;
import com.gxcj.result.PageResult;

public interface JobService {
    PageResult<SearchResultVo> search(JobController.SearchReq req);

    JobDetailVo getJobDetail(String jobId);
}
