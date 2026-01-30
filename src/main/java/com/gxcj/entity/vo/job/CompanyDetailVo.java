package com.gxcj.entity.vo.job;

import lombok.Data;

import java.util.List;

@Data
public class CompanyDetailVo {
    private String id;
    private String name;
    private String logo;
    private List<String> industry;
    private String scale;
    private String fundingStage;         // fundingStage (融资状态)
    private String website;        // 官网 (如果没有字段，可写死或留空)
    private String address;
    private String description;          // description
    private List<String> images;   // 公司环境图 (目前数据库没这个字段，先返回空列表)
    private Integer jobCount;      // 在招职位数 (需要统计)
}
