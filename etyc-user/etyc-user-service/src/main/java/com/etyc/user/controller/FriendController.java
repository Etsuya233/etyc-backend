package com.etyc.user.controller;


import com.etyc.entity.R;
import com.etyc.user.domain.dto.BiFriendIdDto;
import com.etyc.user.domain.po.User;
import com.etyc.user.domain.vo.UserLiteVo;
import com.etyc.user.service.IFriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-20
 */
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

	private final IFriendService friendService;

	/**
	 * 添加好友
	 * @param biFriendIdDto DTO
	 * @return 无
	 */
	@PutMapping
	public R<Void> addFriend(@RequestBody @Valid BiFriendIdDto biFriendIdDto){
		friendService.addFriend(biFriendIdDto);
		return R.ok();
	}

	/**
	 * 删除好友
	 * @param dto DTO
	 * @return 无
	 */
	@DeleteMapping
	public R<Void> removeFriend(@RequestBody @Valid BiFriendIdDto dto){
		friendService.removeFriend(dto);
		return R.ok();
	}

	/**
	 * 获取好友列表
	 * @return 好友列表 User
	 */
	@GetMapping
	public R<List<User>> getAllFriends(){
		List<User> friends = friendService.getAllFriends();
		return R.ok(friends);
	}

	@GetMapping("/cacheAll")
	public R<Void> cacheAllFriend(Long userId){
		friendService.cacheAllFriend(userId);
		return R.ok();
	}

	@GetMapping("/ids")
	public R<List<Long>> getAllFriendIds(){
		List<Long> ids = friendService.getAllFriendIds();
		return R.ok(ids);
	}

}
