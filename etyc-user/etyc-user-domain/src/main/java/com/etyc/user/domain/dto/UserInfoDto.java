package com.etyc.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Data
public class UserInfoDto {

	@NotBlank(message = "用户名不得为空！")
	@Length(min = 6, max = 30, message = "用户名只能在6-30字之间！")
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线、中划线！")
	private String username;

	@NotBlank(message = "昵称不得为空！")
	@Length(min = 1, max = 30, message = "昵称只能在1-30字之间！")
	private String nickname;

	private String password;

	@Length(min = 8, max = 30, message = "密码只能在8-30字之间！")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&,])[A-Za-z\\d@$!%*?&,]+$",
			message = "密码至少包含大写字母、小写字母、数字、特殊字符！")
	private String newPassword;

	private String phone;

	@Email
	private String email;

	private String birthday;

	@Range(min = 0, max = 2, message = "性别错误")
	private Integer sex;

	private String avatar;
}