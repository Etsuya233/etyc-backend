package com.etyc.user.service.impl;

import com.etyc.constant.RedisConstant;
import com.etyc.entity.BaseContext;
import com.etyc.enums.ExceptionEnum;
import com.etyc.exceptions.EtycException;
import com.etyc.user.domain.dto.BiFriendIdDto;
import com.etyc.user.domain.po.Friend;
import com.etyc.user.domain.po.User;
import com.etyc.user.domain.vo.UserLiteVo;
import com.etyc.user.mapper.FriendMapper;
import com.etyc.user.mapper.UserMapper;
import com.etyc.user.service.IFriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-20
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

	private final StringRedisTemplate redisTemplate;
	private final UserMapper userMapper;

	@Override
	public void addFriend(BiFriendIdDto biFriendIdDto) {
		long userIdA = biFriendIdDto.getUserIdA(), userIdB = biFriendIdDto.getUserIdB();
		if(userIdA == userIdB) throw new EtycException(ExceptionEnum.ARGUMENT_NOT_VALID, "不能添加自己为好友！");

		//数据统一
		if(userIdA > userIdB) {
			long temp = userIdA;
			userIdA = userIdB;
			userIdB = temp;
		}

		//添加好友
		Friend friend = new Friend(userIdA, userIdB);
		try {
			this.save(friend);
		} catch (Exception e){
			throw new EtycException("不能重复添加好友！", e);
		}

		//清空缓存
		redisTemplate.delete(RedisConstant.USER.FRIENDS_PREFIX + userIdA);
		redisTemplate.delete(RedisConstant.USER.FRIENDS_PREFIX + userIdB);
	}

	@Override
	public void removeFriend(BiFriendIdDto dto) {
		long userIdA = dto.getUserIdA(), userIdB = dto.getUserIdB();
		if(userIdA == userIdB) return;

		//数据统一
		if(userIdA > userIdB) {
			long temp = userIdA;
			userIdA = userIdB;
			userIdB = temp;
		}

		//尝试删除
		Friend record = this.lambdaQuery()
				.eq(Friend::getUserIdA, userIdA)
				.eq(Friend::getUserIdB, userIdB)
				.one();
		if(record == null) throw new EtycException("好友不存在！");
		this.removeById(record);

		//清空缓存
		redisTemplate.delete(RedisConstant.USER.FRIENDS_PREFIX + userIdA);
		redisTemplate.delete(RedisConstant.USER.FRIENDS_PREFIX + userIdB);
	}

	@Override
	public List<User> getAllFriends() {
		Long userId = BaseContext.getUserId();
		List<Long> ids = cacheAndGetAllFriendIds(userId);

		//获取好友信息
		List<User> friends = this.userMapper.selectBatchIds(ids);
		Collections.sort(friends);
		return friends;
	}

	@Override
	public void cacheAllFriend(Long userId) {
		cacheAndGetAllFriendIds(userId);
	}

	@Override
	public List<Long> getAllFriendIds() {
		Long userId = BaseContext.getUserId();
		return cacheAndGetAllFriendIds(userId);
	}

	private List<Long> cacheAndGetAllFriendIds(Long userId){
		//获取好友列表
		List<Long> friendIdList = null;
		String key = RedisConstant.USER.FRIENDS_PREFIX + userId; //TODO 并发安全问题 下面4行
		if(Boolean.TRUE.equals(redisTemplate.hasKey(key))){
			Set<String> friendIdSet = redisTemplate.opsForSet().members(key);
			friendIdList = friendIdSet.stream().map(Long::parseLong).toList();
		} else {
			List<Friend> friends = this.lambdaQuery()
					.eq(Friend::getUserIdA, userId)
					.or()
					.eq(Friend::getUserIdB, userId)
					.list();
			friendIdList = friends.stream()
					.map(f -> f.getUserIdA().equals(userId) ? f.getUserIdB() : f.getUserIdA())
					.toList();
			List<String> friendIdStrList = friendIdList.stream().map(Object::toString).toList();
			redisTemplate.opsForSet().add(key, friendIdStrList.toArray(new String[0]));
		}
		redisTemplate.expire(key, RedisConstant.USER.FRIENDS_CACHE_TTL, TimeUnit.MILLISECONDS);

		return friendIdList;
	}

}
