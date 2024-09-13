package com.etyc.user.service;

import com.etyc.user.domain.dto.UserInfoDto;
import com.etyc.user.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etyc.user.domain.vo.LoginVo;
import com.etyc.user.domain.vo.UserLiteVo;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-19
 */
public interface IUserService extends IService<User> {

	void register(User user);

	LoginVo loginWithPassword(User user);

	LoginVo refreshToken(String token);

	User getUserInfo(Long id);

	List<UserLiteVo> getBatchUserLite(List<Long> ids);

	List<User> getBatchUser(List<Long> ids);

	void updateUserInfo(UserInfoDto dto);

}
