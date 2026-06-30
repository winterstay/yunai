package com.yunai.yunai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.yunai.yunai.mapper")
public class YunAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(YunAiApplication.class, args);
	}

}
