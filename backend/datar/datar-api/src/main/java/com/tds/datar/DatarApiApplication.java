package com.tds.datar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.tds.datar")
@EnableDiscoveryClient
@MapperScan("com.tds.datar.dal.mapper")
public class DatarApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatarApiApplication.class, args);
    }
}