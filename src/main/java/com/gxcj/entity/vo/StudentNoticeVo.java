package com.gxcj.entity.vo;

import lombok.Data;

@Data
public class StudentNoticeVo {
    private String noticeId;
    private String noticeTitle;
    private String noticeType;
    private String noticeTypeText;
    private String publishTime;
    private String publisherType;
    private String publisherName;
    private Integer isTop;
    private Integer viewCount;
}
