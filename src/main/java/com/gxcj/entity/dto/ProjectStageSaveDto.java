package com.gxcj.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectStageSaveDto {
    private String projectId;
    private List<StageItem> stages;

    @Data
    public static class StageItem {
        private String stageId;
        private String stageName;
        private Integer stageOrder;
        private String description;
        private String status;
    }
}
