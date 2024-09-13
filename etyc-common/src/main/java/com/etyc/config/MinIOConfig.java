package com.etyc.config;

import com.etyc.properties.MinIOProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(MinioClient.class)
@RequiredArgsConstructor
@Slf4j
public class MinIOConfig {

	private final MinIOProperties minIOProperties;

	@Bean
	public MinioClient minioClient(){
		log.info("已注册MinioClient！");
		return MinioClient.builder()
				.endpoint(minIOProperties.getEndpoint())
				.credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
				.build();
	}

}
