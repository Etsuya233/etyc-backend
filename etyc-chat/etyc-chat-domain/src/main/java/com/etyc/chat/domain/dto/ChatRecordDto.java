package com.etyc.chat.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRecordDto {
	private Long userId;
	private LocalDateTime time;
	private Integer count;
}
