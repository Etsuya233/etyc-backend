package com.etyc.properties;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "etyc.minio")
@ConditionalOnClass(MinioClient.class)
public class MinIOProperties {
	private String endpoint;
	private String accessKey;
	private String secretKey;
}
