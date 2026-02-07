package com.gxcj.stutas;

import lombok.Getter;

@Getter
public enum JobDeliveryStatusEnum {
    DELIVERED("DELIVERED", "已投递"),
    VIEWED("VIEWED","被查看"),
    INTERVIEW("INTERVIEW","面试"),
    OFFER("OFFER","录用"),
    REJECT("REJECT","不合适")
    ;
    private String value;
    private String description;
    JobDeliveryStatusEnum(String value, String description) {}
}
