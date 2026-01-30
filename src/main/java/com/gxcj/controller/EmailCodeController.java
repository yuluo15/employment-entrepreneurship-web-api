package com.gxcj.controller;

import com.gxcj.constant.SysConstant;
import com.gxcj.result.Result;
import com.gxcj.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @PostMapping("/code")
    public Result<String> sendEmail(@RequestParam("email") String email){
        SecureRandom secureRandom = new SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000);
        String codeStr = String.valueOf(code);

        stringRedisTemplate.opsForValue().set(SysConstant.REDIS_EMAIL_CODE + email, codeStr,5, TimeUnit.MINUTES);

        mailUtil.sendMail(email,codeStr);
        return Result.success("发送成功");
    }
}
