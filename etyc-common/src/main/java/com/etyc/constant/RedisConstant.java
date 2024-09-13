package com.etyc.constant;

public interface RedisConstant {
	String TOKEN_PREFIX = "token:";

	interface AUTH {
		String REFRESH_TOKEN_PREFIX = TOKEN_PREFIX + "refresh:";
	}

	interface USER {
		String FRIENDS_PREFIX = "friends:";
		Long FRIENDS_CACHE_TTL = 1800000L; //30min
	}

	interface CHAT {
		String CHAT_USER_VALID_PREFIX = "chat:valid:";
		Long CHAT_USER_VALID_TTL = 600000L; // 10min
		String RECENT_CHAT_ID_KEY_PREFIX = "chat:recentId:";
		String RECENT_CHAT_KEY = "chat:recent";
		String CHAT_LIST_PREFIX = "chat:list:";
		String CHAT_UNREAD_PREFIX = "chat:unread:";
		String MESSAGE_PREFIX = "message:";
		Long MESSAGE_TTL = 90000000L; //25H
	}
}
