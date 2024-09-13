package com.etyc.config;

import com.etyc.entity.BaseContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Slf4j
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitMqConfig {

	@Bean
	public MessageConverter messageConverter() {
		log.info("已注册RabbitMQ消息转换器：Jackson2JsonMessageConverter");
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter());
		factory.setAfterReceivePostProcessors(message -> {
			Long userId = message.getMessageProperties().getHeader("userId");
			BaseContext.setUserId(userId);
			return message;
		});
		log.info("已注册RabbitMQ接收消息后处理器");
		return factory;
	}
}
