package com.etyc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 */
@ConfigurationProperties(prefix = "etyc.jwt")
@Component
@Data
public class JwtProperties {
	/**
	 * 公钥位置，Classpath
	 */
	public String publicKeyPath;
	/**
	 * 私钥位置，Classpath
	 */
	public String privateKeyPath;
}