package com.tds.dos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DOS API启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan({"com.tds.dos.dal.mapper", "com.tds.dos.dal.msp.mapper"})
public class DosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DosApiApplication.class, args);
    }
}