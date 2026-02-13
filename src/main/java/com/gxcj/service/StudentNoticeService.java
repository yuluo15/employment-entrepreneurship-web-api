package com.gxcj.service;

import com.gxcj.entity.vo.StudentNoticeDetailVo;
import com.gxcj.entity.vo.StudentNoticeVo;
import com.gxcj.result.PageResult;

import java.util.List;

public interface StudentNoticeService {
    List<StudentNoticeVo> getHomeNotices();
    
    StudentNoticeDetailVo getNoticeDetail(String noticeId);
    
    PageResult<StudentNoticeVo> getNoticeList(Integer pageNum, Integer pageSize, String noticeType, String publisherType);
}
