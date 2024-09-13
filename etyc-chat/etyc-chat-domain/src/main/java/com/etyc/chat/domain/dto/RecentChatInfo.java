package com.etyc.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentChatInfo {
	private Long msgId;
	private Long timestamp;
}
