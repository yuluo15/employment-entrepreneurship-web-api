package com.gxcj.controller;

import com.gxcj.constant.SysConstant;
import com.gxcj.entity.vo.UserVo;
import com.gxcj.result.Result;
import com.gxcj.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 登录成功后返回token到前端
     * @param userReq
     * @return
     */
    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody UserReq userReq){
        UserVo userVo = userService.login(userReq);
        if (userVo == null){
            return Result.fail("账号或密码错误");
        }
        return Result.success(userVo);
    }

    @GetMapping("/logout")
    public Result<String> loginOut(@RequestParam String userId){
        stringRedisTemplate.delete(SysConstant.REDIS_TOKEN_HAND + userId);
        return Result.success();
    }

    @Data
    public static class UserReq{
        public String loginName;
        public String password;
    }
}
