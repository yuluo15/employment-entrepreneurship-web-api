package com.gxcj.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DictDataVo {
    private String id;
    private Integer dictSort;
    private String dictLabel;
    private String dictValue;
}
