package com.etyc.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BiFriendIdDto {
	@NotNull(message = "用户ID不能为空")
	private Long userIdA;
	@NotNull(message = "用户ID不能为空")
	private Long userIdB;
}
