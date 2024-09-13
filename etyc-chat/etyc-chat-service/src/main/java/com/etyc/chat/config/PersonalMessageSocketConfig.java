package com.etyc.chat.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.etyc.chat.interceptor.GeneralChannelInterceptor;
import com.etyc.constant.Constants;
import com.etyc.exceptions.EtycException;
import com.etyc.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@RequiredArgsConstructor
public class PersonalMessageSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final GeneralChannelInterceptor generalChannelInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/etyc")
				.setAllowedOriginPatterns("*")
				.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(generalChannelInterceptor);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		log.info("已注册消息中转");

		//启用简单消息代理,启用了一个内存中的简单消息代理，它负责将消息广播到相应的订阅者。
		//这个简单的代理不依赖于外部消息队列系统（如 RabbitMQ、ActiveMQ 等），而是直接在 Spring 应用程序中运行。
		registry.enableSimpleBroker("/chat");

		registry.setUserDestinationPrefix("/user");
	}

}
