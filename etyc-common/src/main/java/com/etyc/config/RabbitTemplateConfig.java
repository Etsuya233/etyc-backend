package com.etyc.config;

import com.etyc.entity.BaseContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(RabbitTemplate.class)
@DependsOn("rabbitTemplate")
@Slf4j
public class RabbitTemplateConfig {

	private final RabbitTemplate rabbitTemplate;

	@PostConstruct
	public void postProcessor() {
		rabbitTemplate.addBeforePublishPostProcessors(new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setHeader("userId", BaseContext.getUserId());
				return message;
			}
		});
		log.info("已注册RabbitMQ消息后处理器");
	}
}
