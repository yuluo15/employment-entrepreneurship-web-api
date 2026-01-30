package com.gxcj.stutas;

import lombok.Getter;

@Getter
public enum DeleteStatusEnum {
    DELETE(1, "已删除"),
    NOT_DELETE(0, "未删除");

    private final Integer code;
    private final String desc;

    DeleteStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
