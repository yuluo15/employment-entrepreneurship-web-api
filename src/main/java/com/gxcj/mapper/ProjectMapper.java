package com.gxcj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxcj.entity.ProjectEntity;

import java.util.List;

public interface ProjectMapper extends BaseMapper<ProjectEntity> {

    public List<ProjectEntity> selectProjectDetail(String projectId);
}
