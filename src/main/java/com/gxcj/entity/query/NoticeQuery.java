package com.gxcj.entity.query;

import lombok.Data;

@Data
public class NoticeQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String noticeTitle;  // 标题（模糊查询）
    private String noticeType;   // 类型：1=通知, 2=公告, 3=政策, 4=新闻
    private Integer status;      // 状态：0=草稿, 1=已发布
}
