package com.gxcj.entity.dto;

import lombok.Data;

@Data
public class OfferDto {
    /**
     * 投递记录ID
     */
    private String deliveryId;
    
    /**
     * 入职时间 YYYY-MM-DD
     */
    private String entryDate;
    
    /**
     * 薪资待遇
     */
    private String salary;
    
    /**
     * 备注信息
     */
    private String notes;
}
