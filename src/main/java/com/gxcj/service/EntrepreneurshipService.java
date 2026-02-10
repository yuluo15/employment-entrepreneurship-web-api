package com.gxcj.service;

import com.gxcj.entity.vo.EntrepProjectVo;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 创业项目库服务接口
 */
public interface EntrepreneurshipService {
    
    /**
     * 获取项目列表
     */
    PageResult<EntrepProjectVo> getProjectList(Integer pageNum, Integer pageSize, 
                                                String projectName, String schoolId, 
                                                String domain, String status);
    
    /**
     * 审核项目
     */
    void auditProject(String projectId, Integer status, String reason);
    
    /**
     * 项目落地
     */
    void completeProject(String projectId);
    
    /**
     * 获取学校列表
     */
    List<SchoolNameVo> getSchoolList();
    
    /**
     * 获取项目领域字典
     */
    Map<String, String> getDomains();
}
