package com.etyc.user.controller;


import com.etyc.entity.R;
import com.etyc.user.domain.dto.UserInfoDto;
import com.etyc.user.domain.po.User;
import com.etyc.user.domain.vo.LoginVo;
import com.etyc.user.domain.vo.UserLiteVo;
import com.etyc.user.service.IUserService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-19
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final IUserService userService;

	/**
	 * 用户注册
	 * @param user 用户实体
	 * @return 无
	 */
	@PostMapping("/register")
	public R<Void> register(@RequestBody @Valid User user, BindingResult errors){
		// 校验
		if(errors.hasErrors()){
			String errMsg = errors.getAllErrors().stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.joining("; "));
			return R.error(errMsg);
		}
		// 注册
		userService.register(user);
		return R.ok();
	}

	/**
	 * 手机号，用户名，邮箱搭配密码登录
	 * @param user 用户输入
	 * @return token等
	 */
	@PostMapping("/login")
	public R<LoginVo> loginWithPassword(@RequestBody User user){
		LoginVo vo = userService.loginWithPassword(user);
		return R.ok(vo);
	}

	/**
	 * 刷新token
	 * @param token 刷新令牌
	 * @return token等
	 */
	@PostMapping("/refresh")
	public R<LoginVo> refreshToken(@RequestBody String token){
		LoginVo vo = userService.refreshToken(token);
		return R.ok(vo);
	}

	@GetMapping("")
	public R<User> getUserInfo(@RequestParam Long id){
		User user = userService.getUserInfo(id);
		return R.ok(user);
	}

	@GetMapping("/current")
	public R<User> getCurrentUser(){
		User user = userService.getUserInfo(null);
		return R.ok(user);
	}

	@PostMapping("/batch/lite")
	public R<List<UserLiteVo>> getBatchUserLite(@RequestBody List<Long> ids){
		List<UserLiteVo> ret = userService.getBatchUserLite(ids);
		return R.ok(ret);
	}

	@PostMapping("/batch")
	public R<List<User>> getBatchUser(@RequestBody List<Long> ids){
		List<User> ret = userService.getBatchUser(ids);
		return R.ok(ret);
	}

	/**
	 * 修改用户信息
	 * @param dto dto
	 * @return 无
	 */
	@PutMapping("/info")
	public R<Void> updateUserInfo(@RequestBody UserInfoDto dto, BindingResult errors){
		if(errors.hasErrors()){
			String errMsg = errors.getAllErrors().stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.joining("; "));
			return R.error(errMsg);
		}
		userService.updateUserInfo(dto);
		return R.ok();
	}
}
