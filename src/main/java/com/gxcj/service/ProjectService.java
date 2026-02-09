package com.gxcj.service;

import com.gxcj.controller.student.MobileProjectController;
import com.gxcj.entity.vo.job.MyProjectVo;
import com.gxcj.entity.vo.job.ProjectDetailVo;
import com.gxcj.result.PageResult;

public interface ProjectService {
    ProjectDetailVo getProjectDetail(String projectId);

    PageResult<MyProjectVo> getMyProjectList(Integer pageNum, Integer pageSize);

    void save(MobileProjectController.ProjectForm projectForm);

    void delete(String projectId);
}
