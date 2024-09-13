package com.etyc.chat.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.io.grpc.NameResolver;
import com.etyc.constant.Constants;
import com.etyc.constant.RedisConstant;
import com.etyc.entity.BaseContext;
import com.etyc.exceptions.EtycException;
import com.etyc.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralChannelInterceptor implements ChannelInterceptor {

	private final JwtUtils jwtUtils;
	private final StringRedisTemplate redisTemplate;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor =
				MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		assert accessor != null;

		//如果是CONNECT操作，尝试获取UserId
		if(StompCommand.CONNECT.equals(accessor.getCommand())) {
			String userId = authenticateAndGetUserId(accessor);
//			BaseContext.setUserId(Long.valueOf(userId));
			log.info("用户{}已建立WS连接!", userId);
			accessor.setUser(() -> userId);

		//如果是发送消息
		} else if(StompCommand.SEND.equals(accessor.getCommand())) {
			//检查是否有聊天令牌
			Principal user = accessor.getUser();
			if(user == null) {
				throw new EtycException("用户未登录，无法访问WS！");
			}
			String userId = user.getName();
			String key = RedisConstant.CHAT.CHAT_USER_VALID_PREFIX + userId;
			if(Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
				//验证JWT
				authenticateAndGetUserId(accessor);
				//生成聊天令牌
				redisTemplate.opsForValue().set(key, "", RedisConstant.CHAT.CHAT_USER_VALID_TTL, TimeUnit.MILLISECONDS);
			}
		} else {
			log.info((accessor.getSessionId() + ": " + accessor.getCommand()));
		}

		return message;
	}

	private String authenticateAndGetUserId(StompHeaderAccessor accessor) {
		//JWT
		List<String> authorizations = accessor.getNativeHeader(Constants.AUTHORIZATION_HEADER);
		String token;
		if(CollUtil.isEmpty(authorizations)){
			throw new EtycException("WS登陆失败，用户Token为空！");
		}
		token = authorizations.getFirst().substring(7); //Bearer !!!
		//检验Token
		Map<String, Object> claims;
		try {
			claims = jwtUtils.parseToken(token);
		} catch (Exception e){
			throw new EtycException("WS登陆失败，用户Token校验失败！");
		}
		return claims.get(Constants.JWT_USER_KEY).toString();
	}
}
