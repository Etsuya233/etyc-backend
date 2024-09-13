package com.etyc.chat;

import com.etyc.config.DefaultFeignConfig;
import com.etyc.user.api.clients.FriendClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.etyc.chat.mapper")
@EnableScheduling
@EnableFeignClients(clients = {FriendClient.class}, defaultConfiguration = DefaultFeignConfig.class)
public class EtycChatApplication {
	public static void main(String[] args) {
		SpringApplication.run(EtycChatApplication.class, args);
	}
}
