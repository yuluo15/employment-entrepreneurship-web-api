package com.gxcj.entity.vo.school;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class SchoolTeacherImportResultVo {
    private Integer successCount;
    private Integer failCount;
    private List<ImportFailItem> failList;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailItem {
        private Integer row;
        private String employeeNo;
        private String reason;
    }
}
