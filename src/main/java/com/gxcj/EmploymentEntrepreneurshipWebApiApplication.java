package com.gxcj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.gxcj.mapper")
@EnableScheduling  // 启用定时任务
public class EmploymentEntrepreneurshipWebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmploymentEntrepreneurshipWebApiApplication.class, args);
    }

}
