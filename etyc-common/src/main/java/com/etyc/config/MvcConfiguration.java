package com.etyc.config;

import com.etyc.interceptor.HttpUserIdInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
@ConditionalOnClass(WebMvcConfigurer.class)
public class MvcConfiguration implements WebMvcConfigurer {

	@Resource
	private HttpUserIdInterceptor httpUserIdInterceptor;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		log.info("已配置跨域");
		registry.addMapping("/**")
				.allowedOriginPatterns("*")
				.allowCredentials(true)
				.allowedMethods("*")
				.maxAge(3600);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info("已配置MVC拦截器");
		registry.addInterceptor(httpUserIdInterceptor).addPathPatterns("/**");
	}
}
