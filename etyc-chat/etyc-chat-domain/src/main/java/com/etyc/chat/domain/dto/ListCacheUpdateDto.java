package com.etyc.chat.domain.dto;

import com.etyc.chat.domain.po.PersonalMsg;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListCacheUpdateDto {
	private List<Long> needDelete;
	private List<PersonalMsg> messages;
}
