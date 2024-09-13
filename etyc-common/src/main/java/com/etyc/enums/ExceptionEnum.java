package com.etyc.enums;

public enum ExceptionEnum {
	//通用
	OTHER_EXCEPTION(1001, "其他错误"),
	ARGUMENT_NOT_VALID(1002, "非法参数"),

	//用户
	USER_NOT_FOUND(2001, "用户不存在"),
	USER_PASSWORD_ERROR(2002, "用户密码错误"),
	USER_STATUS_ERROR(2003, "用户状态异常"),
	USER_STATUS_DELETED(2004, "用户已删除"),

	//登陆状态
	ACCESS_TOKEN_EXPIRED(3001, "登陆令牌不存在或已过期"),
	REFRESH_TOKEN_EXPIRED(3002, "刷新令牌不存在或已过期"),
	LOGIN_EXPIRED(3003, "登陆已过期"),

	//好友状态
	NOT_FRIEND(4001, "你们还不是好友状态，无法发送消息。");

	public final int code;
	public final String msg;

	ExceptionEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
}
