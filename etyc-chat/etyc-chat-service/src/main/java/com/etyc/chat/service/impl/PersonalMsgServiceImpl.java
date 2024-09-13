package com.etyc.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.etyc.chat.domain.dto.ChatRecordDto;
import com.etyc.chat.domain.dto.ListCacheUpdateDto;
import com.etyc.chat.domain.dto.MsgListEntry;
import com.etyc.chat.domain.po.PersonalMsg;
import com.etyc.chat.mapper.PersonalMsgMapper;
import com.etyc.chat.service.IPersonalMsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etyc.constant.Constants;
import com.etyc.constant.MqConstant;
import com.etyc.constant.RedisConstant;
import com.etyc.entity.BaseContext;
import com.etyc.enums.ExceptionEnum;
import com.etyc.exceptions.EtycException;
import com.etyc.user.api.clients.FriendClient;
import com.etyc.utils.MinIOUtils;
import com.etyc.utils.SnowflakeGenerator;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PersonalMsgServiceImpl extends ServiceImpl<PersonalMsgMapper, PersonalMsg> implements IPersonalMsgService {

	private final SimpMessagingTemplate messagingTemplate;
	private final StringRedisTemplate redisTemplate;
	private final FriendClient friendClient;
	private final SnowflakeGenerator snowflakeGenerator;
	private final RabbitTemplate rabbitTemplate;
	private final MinioClient minioClient;
	private final MinIOUtils minIOUtils;

	@Override
	public void sendMsg(PersonalMsg msg, Principal principal) {
		//判断是不是好友
		final long sender = Long.parseLong(principal.getName());
		final long receiver = msg.getReceiver();
		if(!mutualFriends(sender, receiver)){
			throw new EtycException(ExceptionEnum.NOT_FRIEND);
		}

		//转发消息
		long msgId = snowflakeGenerator.next();
		LocalDateTime createTime = LocalDateTime.now();
		msg.setCreateTime(createTime);
		msg.setId(msgId);
		msg.setSender(sender);
		log.info("{} -> Msg: {} -> {}", sender, msg.getContent(), msg.getReceiver());
		messagingTemplate.convertAndSendToUser(
				msg.getSender().toString(),
				"/chat/receive", // -> /user/chat/receive
				msg
		);
		messagingTemplate.convertAndSendToUser(
				msg.getReceiver().toString(),
				"/chat/receive", // -> /user/chat/receive
				msg
		);

		//消息持久化
		rabbitTemplate.convertAndSend(MqConstant.CHAT_TOPIC_EXCHANGE, MqConstant.CHAT_MESSAGE_RECEIVED_KEY, msg);
	}

	private boolean mutualFriends(long sender, long receiver) {
		String key = RedisConstant.USER.FRIENDS_PREFIX + sender;
		if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
			friendClient.cacheAllFriend(sender);
		}
		Boolean member = redisTemplate.opsForSet().isMember(key, String.valueOf(receiver));
		//不是好友
		return Boolean.TRUE.equals(member);
	}

	@Override
	public void clearUnread(Long id, Principal principal) {
		String userId = principal.getName();
		redisTemplate.opsForHash().delete(RedisConstant.CHAT.CHAT_UNREAD_PREFIX + userId, String.valueOf(id));
	}

	@Override
	public List<MsgListEntry> getMsgList() {
		Long userId = BaseContext.getUserId();
		//获取最近聊天过的用户和最后一条消息ID
		Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisConstant.CHAT.CHAT_LIST_PREFIX + userId);
		//筛选出Redis中7天内没聊天过的用户，并将其存入needDelete。把7天内聊天过的用户存入map。
		Long currentTimestamp = System.currentTimeMillis();
		List<Long> needDelete = new ArrayList<>();
		HashMap<Long, String> map = new HashMap<>(); //uid msgId，后面将msgId转化为content
		entries.forEach((key, value) -> {
			Long uid = Long.parseLong((String) key);
			String valueStr = (String) value;
			int commaLocation = valueStr.indexOf(':');
			String msgId = valueStr.substring(0, commaLocation);
			Long timestamp = Long.parseLong(valueStr.substring(commaLocation + 1));
			if(currentTimestamp - timestamp > Constants.WEEK_TIMESTAMP_SPAN){
				needDelete.add(uid);
			} else {
				map.put(uid, msgId);
			}
		});
		//Redis查找聊天详情
		List<String> redisRet = redisTemplate.opsForValue().multiGet(
				map.values().stream()
						.map(k -> RedisConstant.CHAT.MESSAGE_PREFIX + k)
						.toList());
		if(redisRet == null) throw new EtycException(ExceptionEnum.OTHER_EXCEPTION);
		int i = 0;
		List<Long> needDb = new ArrayList<>();
		for(Long uid: map.keySet()){
			if(redisRet.get(i) != null) {
				map.put(uid, redisRet.get(i));
			} else {
				needDb.add(Long.parseLong(map.get(uid)));
			}
			i++;
		}
		//MySQL查Redis中没有的消息
		List<PersonalMsg> dbMsgList = null;
		if(!needDb.isEmpty()){
			dbMsgList = this.lambdaQuery()
					.in(PersonalMsg::getId, needDb)
					.list();
			Map<Long, String> dbMsgMap = dbMsgList.stream()
					.collect(Collectors.toMap(
							msg -> msg.getSender().equals(userId)? msg.getReceiver(): msg.getSender(),
							JSONUtil::toJsonStr));
			map.putAll(dbMsgMap);
		}
		//获取未读消息数目
		Map<Object, Object> unreadEntries = redisTemplate.opsForHash()
				.entries(RedisConstant.CHAT.CHAT_UNREAD_PREFIX + userId);
		//转化返回值
		List<MsgListEntry> ret = map.entrySet().stream().map(entry -> {
			String msgJson = entry.getValue();
			PersonalMsg msg = JSONUtil.toBean(msgJson, PersonalMsg.class);
			return new MsgListEntry(entry.getKey(), msg.getContent(),
					unreadEntries.get(entry.getKey().toString()) != null ?
							Integer.parseInt((String) unreadEntries.get(entry.getKey().toString())) : 0,
					msg.getCreateTime(), msg.getCreateTime().atZone(ZoneOffset.of("+8")).toInstant().toEpochMilli());
		}).collect(Collectors.toList());
		//更新缓存
		rabbitTemplate.convertAndSend(MqConstant.CHAT_TOPIC_EXCHANGE, MqConstant.CHAT_LIST_CACHE_KEY,
				new ListCacheUpdateDto(needDelete, dbMsgList));
		return ret;
	}

	@Override
	public List<PersonalMsg> getChatRecord(ChatRecordDto dto) { //TODO 这里根本没走Redis缓存，请测试速度
		Long userA = BaseContext.getUserId();
		Long userB = dto.getUserId();
		if(userA > userB){ //swap
			long temp = userB;
			userB = userA;
			userA = temp;
		}
		List<PersonalMsg> ret = new ArrayList<>(dto.getCount());
		//如果timestamp为空则查找最新消息
		LocalDateTime time = dto.getTime();
		long timestamp = System.currentTimeMillis();
		if(time == null){
			timestamp += 1000000;
			time = LocalDateTime.now().plusMinutes(10);
		}
		//先从Redis找
		Integer count = dto.getCount();
		String key = RedisConstant.CHAT.RECENT_CHAT_ID_KEY_PREFIX + userA + "-" + userB;
		Set<String> redisRet = redisTemplate.opsForZSet().reverseRangeByScore(key, 0, timestamp, 0, count);
		if(redisRet == null){
			throw new EtycException(ExceptionEnum.OTHER_EXCEPTION, String.format("getChatRecord：%s， redis返回结果为null！", key));
		}
		List<String> msgIdsKey = redisRet.stream().map(id -> RedisConstant.CHAT.MESSAGE_PREFIX + id).toList();
		List<String> redisMsgsJson = redisTemplate.opsForValue().multiGet(msgIdsKey);
		if(redisMsgsJson == null){
			throw new EtycException(ExceptionEnum.OTHER_EXCEPTION, String.format("getChatRecord：%s， redis返回结果为null！", "redisMsgsJson"));
		}
		redisMsgsJson.stream()
				.filter(Objects::nonNull)
				.forEach(s -> ret.add(JSONUtil.toBean(s, PersonalMsg.class)));
//		List<Long> needDb = ret.stream() //redis消息不命中的去MySQL找
//				.map(PersonalMsg::getId)
//				.filter(id -> !redisRet.contains(String.valueOf(id))).toList();
		//去MySQL找
//		List<PersonalMsg> needDbRet = this.lambdaQuery().in(PersonalMsg::getId, needDb).list();
		if(ret.size() < count){
//			ret.sort(PersonalMsg.oldFirstComparator);
			int limit = count - ret.size();
			List<PersonalMsg> dbRet = this.lambdaQuery()
					.eq(PersonalMsg::getSender, userA)
					.eq(PersonalMsg::getReceiver, userB)
					.lt(PersonalMsg::getCreateTime, time)
					.or()
					.eq(PersonalMsg::getSender, userB)
					.eq(PersonalMsg::getReceiver, userA)
					.lt(PersonalMsg::getCreateTime, time)
					.orderByDesc(PersonalMsg::getCreateTime)
					.last("limit " + limit)
					.list();
			ret.addAll(dbRet);
			//缓存消息
//			rabbitTemplate.convertAndSend(MqConstant.CHAT_TOPIC_EXCHANGE, MqConstant.CHAT_RECORD_CACHE_KEY, dbRet);
		}
		//排序返回
		ret.sort(PersonalMsg.oldFirstComparator);
		return ret;
	}

	@Override
	public void sendFile(MultipartFile file, Long receiver) {
		Long userId = BaseContext.getUserId();

		//检查是否是好友
		if(!mutualFriends(userId, receiver)){
			throw new EtycException(ExceptionEnum.NOT_FRIEND);
		}

		if(file == null || file.isEmpty()){
			throw new EtycException("文件不存在！");
		}

		if(file.getSize() >= 10 * 1024 * 1024){
			throw new EtycException("文件太大啦塞不下");
		}

		//上传文件至Minio
		String originalFilename = file.getOriginalFilename() == null?
				String.valueOf(System.currentTimeMillis()) : file.getOriginalFilename();
		String filename = userId + "_" + originalFilename;
		try {
			minIOUtils.putObject(Constants.OSS_FILE_BUCKET,
					filename, file.getContentType(), file.getInputStream(), file.getSize());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		//发送消息
		long msgId = snowflakeGenerator.next();
		PersonalMsg msg = PersonalMsg.builder()
				.id(msgId)
				.content(originalFilename)
				.meta("http://192.168.150.101:9000/etyc-file/" + filename)
				.type(1)
				.sender(userId)
				.receiver(receiver)
				.createTime(LocalDateTime.now())
				.build();
		log.info("{} -> File: {} -> {}", userId, msg.getContent(), msg.getReceiver());
		messagingTemplate.convertAndSendToUser(
				msg.getSender().toString(),
				"/chat/receive", // -> /user/chat/receive
				msg
		);
		messagingTemplate.convertAndSendToUser(
				msg.getReceiver().toString(),
				"/chat/receive", // -> /user/chat/receive
				msg
		);

		//消息持久化
		rabbitTemplate.convertAndSend(MqConstant.CHAT_TOPIC_EXCHANGE, MqConstant.CHAT_MESSAGE_RECEIVED_KEY, msg);
	}
}
