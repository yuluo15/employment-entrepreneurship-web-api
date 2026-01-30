package com.gxcj.stutas;

import lombok.Getter;

@Getter
public enum CompanyStatusEnum {
    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核驳回"),
    FROZEN(9, "已冻结");

    private final int code;
    private final String desc;

    CompanyStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
