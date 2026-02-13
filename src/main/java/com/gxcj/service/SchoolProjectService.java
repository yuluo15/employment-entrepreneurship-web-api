package com.gxcj.service;

import com.gxcj.entity.dto.school.SchoolProjectAuditDto;
import com.gxcj.entity.query.school.SchoolProjectQuery;
import com.gxcj.entity.vo.school.SchoolProjectDetailVo;
import com.gxcj.entity.vo.school.SchoolProjectVo;
import com.gxcj.result.PageResult;

public interface SchoolProjectService {
    
    /**
     * 获取项目列表
     */
    PageResult<SchoolProjectVo> getProjectList(SchoolProjectQuery query, String userId);
    
    /**
     * 获取项目详情
     */
    SchoolProjectDetailVo getProjectDetail(String projectId, String userId);
    
    /**
     * 审核项目
     */
    void auditProject(SchoolProjectAuditDto dto, String userId);
}
