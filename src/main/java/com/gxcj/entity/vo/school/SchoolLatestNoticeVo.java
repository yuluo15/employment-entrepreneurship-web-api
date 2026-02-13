package com.gxcj.entity.vo.school;

import lombok.Data;

@Data
public class SchoolLatestNoticeVo {
    private String noticeId;
    private String noticeTitle;
    private String noticeType;
    private Integer isTop;
    private String publishTime;
}
