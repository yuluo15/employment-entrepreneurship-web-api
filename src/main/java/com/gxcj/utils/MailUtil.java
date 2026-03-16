package com.gxcj.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Async("taskExecutor")
    public void sendMail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("15937321163@163.com");
        message.setTo(toEmail);
        message.setSubject("【高校创业就业平台】注册验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效。");
        mailSender.send(message);
    }
}
