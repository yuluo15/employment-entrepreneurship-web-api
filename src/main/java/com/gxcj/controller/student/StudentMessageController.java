package com.gxcj.controller.student;

import com.gxcj.context.UserContext;
import com.gxcj.entity.query.MessageQuery;
import com.gxcj.entity.vo.MessageDetailVo;
import com.gxcj.entity.vo.MessageVo;
import com.gxcj.entity.vo.UnreadCountVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.MessageService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端消息通知接口
 */
@RestController
@RequestMapping("/api/mobile/messages")
@Validated
public class StudentMessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 1. 获取消息列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<MessageVo>> getMessages(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead) {

        String studentId = UserContext.getUserId();

        MessageQuery query = new MessageQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        if (type != null && type != 0){
            query.setType(type);
        }
        query.setIsRead(isRead);
        query.setReceiverId(studentId);

        PageResult<MessageVo> result = messageService.getMessages(query);
        return Result.success(result);
    }

    /**
     * 2. 获取消息详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<MessageDetailVo> getMessageDetail(@PathVariable String id) {
        String studentId = UserContext.getUserId();

        MessageDetailVo detail = messageService.getMessageDetail(id, studentId);
        return Result.success(detail);
    }

    /**
     * 3. 获取未读消息数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<UnreadCountVo> getUnreadCount() {
        String studentId = UserContext.getUserId();

        UnreadCountVo count = messageService.getUnreadCount(studentId);
        return Result.success(count);
    }

    /**
     * 4. 标记消息为已读
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> markAsRead(@PathVariable String id) {
        String studentId = UserContext.getUserId();

        int rows = messageService.markAsRead(id, studentId);
        if (rows == 0) {
            return Result.fail("消息不存在或已读");
        }

        return Result.success();
    }

    /**
     * 5. 批量标记已读
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> markAllAsRead(@RequestBody(required = false) ReadAllRequest request) {
        String studentId = UserContext.getUserId();
        Integer type = request != null ? request.getType() : null;

        int rows = messageService.markAllAsRead(studentId, type);
//        return Result.success("已标记" + rows + "条消息为已读");
        return Result.success();
    }

    /**
     * 6. 删除消息
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> deleteMessage(@PathVariable String id) {
        String studentId = UserContext.getUserId();

        int rows = messageService.deleteMessage(id, studentId);
        if (rows == 0) {
            return Result.fail("消息不存在");
        }

//        return Result.success("删除成功");
        return Result.success();
    }

    // ==================== 请求对象 ====================

    @Data
    public static class ReadAllRequest {
        private Integer type;  // 消息类型：1=系统通知, 2=面试通知, 3=Offer通知
    }
}
