package com.etyc.config;

import feign.Feign;
import feign.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Feign.class)
public class DefaultFeignConfig {
	@Bean
	//注意，此时这个Bean并没有加入Spring的容器内。因为没加@Configuration
	public Logger.Level feignLoggerLevel(){
		return Logger.Level.FULL;
	}
}
