package com.gxcj.service;

import com.gxcj.entity.dto.ProjectApplyDto;
import com.gxcj.entity.dto.ProjectApplicationHandleDto;
import com.gxcj.entity.vo.ProjectApplicantVo;
import com.gxcj.entity.vo.ProjectApplicationVo;
import com.gxcj.entity.vo.job.MyJoinedProjectVo;
import com.gxcj.result.PageResult;

import java.util.Map;

public interface ProjectApplicationService {
    Map<String, String> applyProject(ProjectApplyDto dto);
    
    void cancelApplication(String applicationId);
    
    PageResult<ProjectApplicationVo> getMyApplications(Integer pageNum, Integer pageSize, String status);
    
    PageResult<ProjectApplicantVo> getProjectApplications(String projectId, Integer pageNum, Integer pageSize, String status);
    
    void handleApplication(String applicationId, ProjectApplicationHandleDto dto);
    
    PageResult<MyJoinedProjectVo> getMyJoinedProjects(Integer pageNum, Integer pageSize, String status);
}
