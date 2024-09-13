package com.etyc.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 网关校验路径
 */
@ConfigurationProperties(prefix = "etyc.auth")
@Data
public class AuthProperties {
	/**
	 * 不需要娇艳的路径
	 */
	public List<String> noAuthPatterns;
	/**
	 * 一定要校验的路径
	 */
	public List<String> forceAuthPatterns;
}
