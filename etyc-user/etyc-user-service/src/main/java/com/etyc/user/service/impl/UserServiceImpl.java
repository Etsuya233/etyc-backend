package com.etyc.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.etyc.constant.Constants;
import com.etyc.constant.RedisConstant;
import com.etyc.constant.UserStatus;
import com.etyc.entity.BaseContext;
import com.etyc.enums.ExceptionEnum;
import com.etyc.exceptions.EtycException;
import com.etyc.user.domain.dto.UserInfoDto;
import com.etyc.user.domain.po.User;
import com.etyc.user.domain.vo.LoginVo;
import com.etyc.user.domain.vo.UserLiteVo;
import com.etyc.user.mapper.UserMapper;
import com.etyc.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etyc.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-19
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;
	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public void register(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		try { //捕捉SQL异常
			boolean saved = this.save(user);
			if(!saved){
				throw new EtycException("注册失败！请重新尝试或尝试修改账户信息。");
			}
		} catch (Exception e){
			throw new EtycException("注册失败！请重新尝试或尝试修改账户信息。", e);
		}
	}

	@Override
	public LoginVo loginWithPassword(User user) {
		//检验
		if(user == null){
			throw new EtycException(ExceptionEnum.ARGUMENT_NOT_VALID);
		}

		//查询用户，前端统一用username来写用户凭证
		User currentUser;
		if(Pattern.matches(Constants.PHONE_REGEX, user.getUsername())){
			currentUser  = this.lambdaQuery().eq(User::getPhone, user.getPhone()).one();
		} else if(Pattern.matches(Constants.EMAIL_REGEX, user.getUsername())){
			currentUser = this.lambdaQuery().eq(User::getEmail, user.getUsername()).one();
		} else {
			currentUser = this.lambdaQuery().eq(User::getUsername, user.getUsername()).one();
		}
		if(currentUser == null){
			throw new EtycException(ExceptionEnum.USER_NOT_FOUND);
		}

		//检验密码
		if(!passwordEncoder.matches(user.getPassword(), currentUser.getPassword())){
			throw new EtycException(ExceptionEnum.USER_PASSWORD_ERROR);
		}
		currentUser.setPassword(null);

		//检验状态
		if(currentUser.getStatus() == UserStatus.DISABLED){
			throw new EtycException(user.getStatusDesc(), ExceptionEnum.USER_STATUS_ERROR.code);
		} else if(currentUser.getStatus() == UserStatus.DELETED){
			throw new EtycException(ExceptionEnum.USER_STATUS_DELETED);
		}

		//登录
		Map<String, Long> claims = Map.of("userId", currentUser.getId());
		String accessToken = jwtUtils.createToken(claims, Constants.ACCESS_TOKEN_TTL);
		String refreshToken = jwtUtils.createToken(claims, Constants.REFRESH_TOKEN_TTL);
		stringRedisTemplate.opsForValue().set(RedisConstant.AUTH.REFRESH_TOKEN_PREFIX + refreshToken,
				"", Constants.REFRESH_TOKEN_TTL, TimeUnit.DAYS); //刷新令牌只能用一次

		return new LoginVo(currentUser, accessToken, refreshToken);
	}

	@Override
	public LoginVo refreshToken(String token) {
		//查看令牌是被使用过
		String oldKey = RedisConstant.AUTH.REFRESH_TOKEN_PREFIX + token;
		if(Boolean.FALSE.equals(stringRedisTemplate.hasKey(oldKey))){
			throw new EtycException(ExceptionEnum.REFRESH_TOKEN_EXPIRED);
		}
		//检验
		Map<String, Object> claims;
		try {
			claims = jwtUtils.parseToken(token);
		} catch (Exception e){
			throw new EtycException(ExceptionEnum.LOGIN_EXPIRED);
		}
		//刷新
		stringRedisTemplate.delete(oldKey);
		String accessToken = jwtUtils.createToken(claims, Constants.ACCESS_TOKEN_TTL);
		String refreshToken = jwtUtils.createToken(claims, Constants.REFRESH_TOKEN_TTL);
		stringRedisTemplate.opsForValue().set(RedisConstant.AUTH.REFRESH_TOKEN_PREFIX + refreshToken,
				"", Constants.REFRESH_TOKEN_TTL, TimeUnit.DAYS); //刷新令牌只能用一次
		return new LoginVo(null, accessToken, refreshToken);
	}

	@Override
	public User getUserInfo(Long id) {
		if(id == null){
			id = BaseContext.getUserId();
		}
		if(id == null){
			throw new EtycException(ExceptionEnum.ARGUMENT_NOT_VALID);
		}
		User user = this.getById(id);
		user.setPassword(null);
		return user;
	}

	@Override
	public List<UserLiteVo> getBatchUserLite(List<Long> ids) {
		if(CollUtil.isEmpty(ids)) return List.of();
		return this.getBaseMapper().selectBatchUserLite(ids);
	}

	@Override
	public List<User> getBatchUser(List<Long> ids) {
		List<User> users = this.getBaseMapper().selectBatchIds(ids);
		users.forEach(u -> u.setPassword(null));
		return users;
	}

	@Override
	public void updateUserInfo(UserInfoDto dto) {
		Long userId = BaseContext.getUserId();
		User user = this.getById(userId);
		if(user == null) throw new EtycException(ExceptionEnum.USER_NOT_FOUND);

		if(StrUtil.isNotBlank(dto.getPassword()) && !passwordEncoder.matches(dto.getPassword(), user.getPassword())){
			throw new EtycException(ExceptionEnum.USER_PASSWORD_ERROR);
		}

		if(StrUtil.isBlank(dto.getPassword()) && StrUtil.isNotBlank(dto.getNewPassword())){
			throw new EtycException(ExceptionEnum.USER_PASSWORD_ERROR);
		}

		if(StrUtil.isBlank(dto.getPhone())){
			dto.setPhone(null);
		} else if(!Pattern.matches(Constants.PHONE_REGEX, dto.getPhone())){
			throw new EtycException("手机号码格式不对！");
		}

		if(StrUtil.isBlank(dto.getEmail())){
			dto.setEmail(null);
		} else if(!Pattern.matches(Constants.EMAIL_REGEX, dto.getPhone())){
			throw new EtycException("邮箱格式不对！");
		}

		LocalDate birthday = null;
		if(StrUtil.isBlank(dto.getBirthday())){
			dto.setBirthday(null);
		} else {
			try {
				birthday = LocalDate.parse(dto.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			} catch (DateTimeParseException e) {
				throw new EtycException("The birthday should be yyyy-MM-dd.");
			}
		}

		User update = BeanUtil.toBean(dto, User.class);
		update.setPassword(passwordEncoder.encode(dto.getPassword()));
		update.setBirthday(birthday);
		this.updateById(user);
	}

}
