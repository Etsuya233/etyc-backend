package com.etyc.user.service;

import com.etyc.user.domain.dto.BiFriendIdDto;
import com.etyc.user.domain.po.Friend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etyc.user.domain.po.User;
import com.etyc.user.domain.vo.UserLiteVo;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-20
 */
public interface IFriendService extends IService<Friend> {

	void addFriend(@Valid BiFriendIdDto biFriendIdDto);

	void removeFriend(@Valid BiFriendIdDto dto);

	List<User> getAllFriends();

	void cacheAllFriend(Long userId);

	List<Long> getAllFriendIds();

}
