package com.gxcj.entity.vo.school;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学生导入结果VO
 */
@Data
public class SchoolStudentImportResultVo {
    private Integer successCount;
    private Integer failCount;
    private List<ImportFailItem> failList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailItem {
        private Integer row;
        private String studentNo;
        private String reason;
    }
}
