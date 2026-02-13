package com.gxcj.controller.student;

import com.gxcj.entity.vo.StudentNoticeDetailVo;
import com.gxcj.entity.vo.StudentNoticeVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.StudentNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile")
public class StudentNoticeController {

    @Autowired
    private StudentNoticeService studentNoticeService;

    /**
     * 获取首页通知列表（最新5条）
     */
    @GetMapping("/home/notices")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<List<StudentNoticeVo>> getHomeNotices() {
        List<StudentNoticeVo> notices = studentNoticeService.getHomeNotices();
        return Result.success(notices);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/notice/{noticeId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<StudentNoticeDetailVo> getNoticeDetail(@PathVariable String noticeId) {
        StudentNoticeDetailVo detail = studentNoticeService.getNoticeDetail(noticeId);
        return Result.success(detail);
    }

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/notices")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<StudentNoticeVo>> getNoticeList(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String noticeType,
            @RequestParam(required = false) String publisherType) {
        PageResult<StudentNoticeVo> result = studentNoticeService.getNoticeList(pageNum, pageSize, noticeType, publisherType);
        return Result.success(result);
    }
}
