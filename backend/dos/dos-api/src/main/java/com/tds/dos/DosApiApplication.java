package com.tds.dos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DOS API启动类
 */
@SpringBootApplication
@MapperScan({"com.tds.dos.dal.mapper", "com.tds.dos.msp.dal.mapper"})
public class DosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DosApiApplication.class, args);
    }
}