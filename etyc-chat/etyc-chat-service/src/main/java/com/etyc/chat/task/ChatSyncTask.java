package com.etyc.chat.task;

import com.etyc.chat.domain.po.PersonalMsg;
import com.etyc.chat.mapper.PersonalMsgMapper;
import com.etyc.constant.RedisConstant;
import com.etyc.exceptions.EtycException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatSyncTask {

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PersonalMsgMapper personalMsgMapper;

	@XxlJob("personalMsgSyncTask")
	@SuppressWarnings("unchecked")
	public void personalMsgSyncTask(){
		log.info("---开始个人消息同步！---");
		XxlJobHelper.log("---开始个人消息同步！---");

		long startTime = System.currentTimeMillis();

		int scanCount = 5000;
		int fetchPerRound = 500;

		Set<String> processedKey = new HashSet<>(); //记录是否处理过

		ScanOptions scanOptions = ScanOptions.scanOptions()
				.match(RedisConstant.CHAT.RECENT_CHAT_ID_KEY_PREFIX + "*")
				.count(scanCount)
				.build();
		try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
			int lenOfPrefix = RedisConstant.CHAT.RECENT_CHAT_ID_KEY_PREFIX.length();
			while(cursor.hasNext()){
				int count = 0;
				Set<String> keys = new HashSet<>();
				Map<String, Set<String>> key2msgIds = new HashMap<>(); //uid1-uid2 -> [...msgId]
				while(cursor.hasNext() && count < fetchPerRound){
					String fullKey = cursor.next();
					String key = fullKey.substring(lenOfPrefix);
					if(!processedKey.contains(key)){
						keys.add(key);
						processedKey.add(key);
					}
					count++;
				}
				//zset
				keys.forEach(key -> {
					Set<String> msgIds = redisTemplate.opsForZSet().rangeByScore(
							RedisConstant.CHAT.RECENT_CHAT_ID_KEY_PREFIX + key, 0, Double.MAX_VALUE);
					key2msgIds.put(key, msgIds);
				});
				//hash
				List<Object> pipelineRet = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
					key2msgIds.forEach((key, msgIds) -> {
						byte[][] array = Arrays.stream(msgIds.toArray(new String[0]))
								.map(s -> (RedisConstant.CHAT.MESSAGE_PREFIX + s).getBytes(StandardCharsets.UTF_8))
								.toArray(byte[][]::new);
//						connection.hashCommands().hMGet((RedisConstant.CHAT.RECENT_CHAT_KEY) //这里不要key了。
//								.getBytes(StandardCharsets.UTF_8), array);
						connection.stringCommands().mGet(array);
					}); //list<list<string>>
					return null;
				});
				//处理消息
				List<PersonalMsg> msgList = new ArrayList<>();
				pipelineRet.forEach(l -> {
					((List<String>) l).forEach(msgJson -> {
						try {
							HashMap<String, Object> map = objectMapper.readValue(msgJson, HashMap.class);
							PersonalMsg msg = PersonalMsg.builder()
									.id((Long) map.get("id"))
									.sender((Long) map.get("sender"))
									.receiver((Long) map.get("receiver"))
									.content((String) map.get("content"))
									.type((Integer) map.get("type"))
									.meta((String) map.get("meta"))
									.build();
							Long timestamp = (Long) map.get("createTime");
							LocalDateTime createTime = LocalDateTime
									.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.of("+8"));//UTC+8
							msg.setCreateTime(createTime);
							msgList.add(msg);
						} catch (JsonProcessingException e) {
							log.error("personalMsgSyncTask时出现JSON错误。{}", msgJson);
						}
					});
				});
				//同步数据库
				personalMsgMapper.insert(msgList);
				//删除redis中的key
				redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
					key2msgIds.forEach((key, msgIds) -> {
						byte[][] array = Arrays.stream(msgIds.toArray(new String[0]))
								.map(s -> s.getBytes(StandardCharsets.UTF_8))
								.toArray(byte[][]::new); //msgIds的byte数组
						connection.zSetCommands().zRem(
								(RedisConstant.CHAT.RECENT_CHAT_ID_KEY_PREFIX + key).getBytes(StandardCharsets.UTF_8),
								array); //删除chat.recentId:[key] 里面的东西
//						connection.hashCommands().hDel(
//								(RedisConstant.CHAT.RECENT_CHAT_KEY).getBytes(StandardCharsets.UTF_8),
//								array); //删除chat.recent->[chatId] 里面的东西
					});
					return null;
				});
			}
		}

		long endTime = System.currentTimeMillis();

		XxlJobHelper.log("---结束个人消息同步！用时{}ms。---", endTime - startTime);
		log.info("---结束个人消息同步！用时{}ms。---", endTime - startTime);
	}

}
