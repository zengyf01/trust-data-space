package com.tds.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 可信数据空间服务平台 - API启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.tds"})
@MapperScan(basePackages = {"com.tds.dal.mapper"})
public class TdsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TdsApiApplication.class, args);
    }
}