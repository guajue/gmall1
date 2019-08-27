package com.guajue.gmall.mangeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.guajue.gmall.mangeservice.mapper")
@ComponentScan("com.guajue.gmall")
public class GmallMangeServiceWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallMangeServiceWebApplication.class, args);
    }

}
