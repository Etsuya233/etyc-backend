package com.etyc.chat.service;

import com.etyc.chat.domain.dto.ChatRecordDto;
import com.etyc.chat.domain.dto.MsgListEntry;
import com.etyc.chat.domain.po.PersonalMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-21
 */
public interface IPersonalMsgService extends IService<PersonalMsg> {
	void sendMsg(PersonalMsg msg, Principal principal);

	void clearUnread(Long id, Principal principal);

	List<MsgListEntry> getMsgList();

	List<PersonalMsg> getChatRecord(ChatRecordDto dto);

	void sendFile(MultipartFile file, Long receiver);
}
