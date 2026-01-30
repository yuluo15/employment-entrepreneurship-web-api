package com.gxcj.service;

import com.gxcj.entity.vo.job.ProjectDetailVo;

public interface ProjectService {
    ProjectDetailVo getProjectDetail(String projectId);
}
