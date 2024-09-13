package com.etyc.user.domain.vo;

import com.etyc.user.domain.po.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {
	public User user;
	public String accessToken;
	public String refreshToken;
}
