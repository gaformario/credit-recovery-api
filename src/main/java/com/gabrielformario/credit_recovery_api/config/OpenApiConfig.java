package com.gabrielformario.credit_recovery_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI creditRecoveryOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Credit Recovery API")
						.description("API para geracao e consulta de estrategias de recuperacao de credito PJ.")
						.version("v1"));
	}
}
