package com.etyc.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.etyc.user.mapper")
@EnableFeignClients
public class EtycUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtycUserApplication.class, args);
	}

}
