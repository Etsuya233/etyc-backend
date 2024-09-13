package com.etyc.config;

import com.etyc.properties.SwaggerProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(OpenAPI.class)
public class SwaggerConfig {

	@Resource
	private SwaggerProperties swaggerProperties;

	@Bean
	public OpenAPI openAPI(){
		return new OpenAPI().info(new Info()
				.contact(new Contact().email(swaggerProperties.getEmail()).name(swaggerProperties.getAuthor()))
				.version(swaggerProperties.getVersion())
				.title(swaggerProperties.getTitle())
				.description(swaggerProperties.getDescription())
				.license(new License().name(swaggerProperties.getLicense()))
		);
	}
}
