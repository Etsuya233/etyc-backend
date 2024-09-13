package com.etyc.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgListEntry {
	private Long userId;
	private String content;
	private Integer unreadCount;
	private LocalDateTime createTime;
	private Long timestamp;
}
