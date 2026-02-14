package com.gxcj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.constant.SysConstant;
import com.gxcj.entity.UserEntity;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.Result;
import com.gxcj.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/sendEmail")
public class EmailCodeController {
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/code")
    public Result<String> sendEmail(@RequestParam("email") String email){
        // 1. 验证邮箱格式
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessException("邮箱格式不正确");
        }

        // 2. 检查邮箱是否已注册
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email);
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 3. 生成6位验证码
        SecureRandom secureRandom = new SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000);
        String codeStr = String.valueOf(code);

        // 4. 存储到Redis（5分钟有效）
        stringRedisTemplate.opsForValue().set(SysConstant.REDIS_EMAIL_CODE + email, codeStr,5, TimeUnit.MINUTES);

        // 5. 发送邮件
        mailUtil.sendMail(email,codeStr);
        return Result.success("发送成功");
    }
}
