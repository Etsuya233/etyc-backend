package com.etyc.user.api.clients;

import com.etyc.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("etyc-user")
public interface FriendClient {

	@GetMapping("/friend/cacheAll")
	R<Void> cacheAllFriend(@RequestParam Long userId);

}
