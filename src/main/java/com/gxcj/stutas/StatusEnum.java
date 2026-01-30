package com.gxcj.stutas;

import lombok.Getter;

@Getter
public enum StatusEnum {
    NORMAL(1, "正常"),
    DISABLED(0, "停用");

    private final Integer code;
    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
