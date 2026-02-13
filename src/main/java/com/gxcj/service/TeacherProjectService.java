package com.gxcj.service;

import com.gxcj.entity.query.TeacherProjectQuery;
import com.gxcj.entity.vo.teacher.TeacherProjectDetailVo;
import com.gxcj.entity.vo.teacher.TeacherProjectGuidanceVo;
import com.gxcj.entity.vo.teacher.TeacherProjectVo;
import com.gxcj.result.PageResult;

import java.util.List;

public interface TeacherProjectService {
    PageResult<TeacherProjectVo> getProjects(TeacherProjectQuery query);
    
    TeacherProjectDetailVo getProjectDetail(String projectId);
    
    List<TeacherProjectGuidanceVo> getProjectGuidanceList(String projectId);
}
