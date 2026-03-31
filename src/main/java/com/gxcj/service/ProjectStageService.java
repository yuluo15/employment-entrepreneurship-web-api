package com.gxcj.service;

import com.gxcj.entity.dto.ProjectStageSaveDto;
import com.gxcj.entity.dto.StageStatusUpdateDto;
import com.gxcj.entity.vo.ProjectStageVo;
import com.gxcj.entity.vo.StageGuidanceVo;

import java.util.List;

public interface ProjectStageService {
    void saveStages(ProjectStageSaveDto dto);
    
    List<ProjectStageVo> getProjectStages(String projectId, boolean isTeacher);
    
    void updateStageStatus(String stageId, StageStatusUpdateDto dto);
    
    List<StageGuidanceVo> getStageGuidance(String stageId);
}
