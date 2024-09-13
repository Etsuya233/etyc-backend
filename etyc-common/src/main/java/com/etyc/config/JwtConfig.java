package com.etyc.config;

import com.etyc.properties.JwtProperties;
import com.etyc.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JwtConfig {
	@Bean
	public JwtUtils jwtUtils(JwtProperties jwtProperties){
		try {
			return new JwtUtils(jwtProperties.getPrivateKeyPath(), jwtProperties.getPublicKeyPath());
		} catch (Exception e){
			log.info("JWT工具类构建失败！");
			return null;
		}
	}
}