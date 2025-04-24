package com.bs.eaps;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 大学生就业帮扶系统启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan({ "com.bs.eaps.mapper" })
public class EapsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EapsApplication.class, args);
    }
}