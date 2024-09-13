package com.etyc.chat.listener;

import cn.hutool.json.JSONUtil;
import com.etyc.chat.domain.dto.ListCacheUpdateDto;
import com.etyc.chat.domain.po.PersonalMsg;
import com.etyc.chat.mapper.PersonalMsgMapper;
import com.etyc.constant.MqConstant;
import com.etyc.constant.RedisConstant;
import com.etyc.entity.BaseContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatListener {

	private final PersonalMsgMapper personalMsgMapper;
	private final StringRedisTemplate redisTemplate;

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue("chat.cache.list"),
			exchange = @Exchange(name = MqConstant.CHAT_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
			key = MqConstant.CHAT_LIST_CACHE_KEY
	))
	public void cacheUpdate(ListCacheUpdateDto dto){
		Long userId = BaseContext.getUserId();
		log.debug("RabbitMQ: chat.cache.list -> {}", userId);
		List<Long> needDelete = dto.getNeedDelete();
		List<PersonalMsg> messages = dto.getMessages();
		redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
			if(needDelete != null && !needDelete.isEmpty()){
				connection.hashCommands().hDel(
						(RedisConstant.CHAT.CHAT_LIST_PREFIX + userId).getBytes(StandardCharsets.UTF_8),
						needDelete.stream()
								.map(id -> id.toString().getBytes(StandardCharsets.UTF_8))
								.toArray(byte[][]::new));
			}
			cacheMessagesInRedisPipeline(connection, messages);
			return null;
		});
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue("chat.cache.record"),
			exchange = @Exchange(name = MqConstant.CHAT_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
			key = MqConstant.CHAT_RECORD_CACHE_KEY
	))
	public void chatRecordCache(List<PersonalMsg> msg){
		redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
			cacheMessagesInRedisPipeline(connection, msg);
			return null;
		});
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue("chat.message.receive"),
			exchange = @Exchange(name = MqConstant.CHAT_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
			key = MqConstant.CHAT_MESSAGE_RECEIVED_KEY
	))
	public void chatMessagePersistence(PersonalMsg msg){
		Long sender = msg.getSender();
		Long receiver = msg.getReceiver();
		Long msgId = msg.getId();

		final String userA = String.valueOf(Math.min(sender, receiver));
		final String userB = String.valueOf(Math.max(sender, receiver));
		long timestamp = msg.getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
		String msgJson = JSONUtil.toJsonStr(msg);
		redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
			connection.zSetCommands().zAdd(
					(RedisConstant.CHAT.RECENT_CHAT_ID_KEY_PREFIX + userA + "-" + userB).getBytes(StandardCharsets.UTF_8),
					timestamp, String.valueOf(msgId).getBytes(StandardCharsets.UTF_8)); //一天内发送消息的Id
			connection.stringCommands().set(
					(RedisConstant.CHAT.MESSAGE_PREFIX + msgId).getBytes(StandardCharsets.UTF_8),
					msgJson.getBytes(StandardCharsets.UTF_8),
					Expiration.from(RedisConstant.CHAT.MESSAGE_TTL, TimeUnit.MILLISECONDS),
					RedisStringCommands.SetOption.UPSERT
			); //最近消息TTL 25小时
			connection.hashCommands().hIncrBy(
					(RedisConstant.CHAT.CHAT_UNREAD_PREFIX + receiver).getBytes(StandardCharsets.UTF_8),
					String.valueOf(sender).getBytes(StandardCharsets.UTF_8),
					1
			); //更新未读消息计数
			connection.hashCommands().hSet(
					(RedisConstant.CHAT.CHAT_LIST_PREFIX + receiver).getBytes(StandardCharsets.UTF_8),
					String.valueOf(sender).getBytes(StandardCharsets.UTF_8),
					(msgId + ":" + timestamp).getBytes(StandardCharsets.UTF_8)
			); //更新最近7天聊天过的人
			connection.hashCommands().hSet(
					(RedisConstant.CHAT.CHAT_LIST_PREFIX + sender).getBytes(StandardCharsets.UTF_8),
					String.valueOf(receiver).getBytes(StandardCharsets.UTF_8),
					(msgId + ":" + timestamp).getBytes(StandardCharsets.UTF_8)
			); //更新最近7天聊天过的人
			return null;
		});
	}

	private void cacheMessagesInRedisPipeline(RedisConnection connection, List<PersonalMsg> messages) {
		if(messages != null && !messages.isEmpty()){
			messages.forEach(message -> {
				String jsonStr = JSONUtil.toJsonStr(message);
				connection.stringCommands().set(
						(RedisConstant.CHAT.MESSAGE_PREFIX + message.getId()).getBytes(StandardCharsets.UTF_8),
						jsonStr.getBytes(StandardCharsets.UTF_8),
						Expiration.from(RedisConstant.CHAT.MESSAGE_TTL, TimeUnit.MILLISECONDS),
						RedisStringCommands.SetOption.UPSERT);
			});
		}
	}
}
