package com.etyc.chat.controller;


import com.etyc.chat.domain.dto.ChatRecordDto;
import com.etyc.chat.domain.dto.MsgListEntry;
import com.etyc.chat.domain.po.PersonalMsg;
import com.etyc.chat.service.IPersonalMsgService;
import com.etyc.entity.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-21
 */
@RestController
@RequestMapping("/msg")
@Slf4j
public class PersonalMsgController {

	@Resource
	private IPersonalMsgService personalMsgService;

	@MessageMapping("/chat")
	public void sendMsg(PersonalMsg msg, Principal principal) {
		personalMsgService.sendMsg(msg, principal);
	}

	@MessageMapping("/clear")
	public void clearUnread(Long id, Principal principal){
		personalMsgService.clearUnread(id, principal);
	}

	/**
	 * 获取消息列表
	 * @return 消息列表（用户Id，消息内容，未读消息数）
	 */
	@GetMapping("/list")
	public R<List<MsgListEntry>> getMsgList(){
		List<MsgListEntry> msgList = personalMsgService.getMsgList();
		return R.ok(msgList);
	}

	/**
	 * 获取消息记录
	 * @param dto 消息记录dto
	 * @return 消息记录
	 */
	@PostMapping("/record")
	public R<List<PersonalMsg>> getChatRecord(@RequestBody ChatRecordDto dto){
		List<PersonalMsg> ret = personalMsgService.getChatRecord(dto);
		return R.ok(ret);
	}

	/**
	 * 文件上传
	 * @return MinIO路径
	 */
	@PostMapping("/file")
	public R<Void> sendFile(@RequestParam("file") MultipartFile file, @RequestParam("receiver") Long receiver){
		personalMsgService.sendFile(file, receiver);
		return R.ok();
	}

}
