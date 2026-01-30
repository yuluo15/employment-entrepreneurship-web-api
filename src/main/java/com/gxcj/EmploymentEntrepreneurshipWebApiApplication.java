package com.gxcj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gxcj.mapper")
public class EmploymentEntrepreneurshipWebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmploymentEntrepreneurshipWebApiApplication.class, args);
    }

}
