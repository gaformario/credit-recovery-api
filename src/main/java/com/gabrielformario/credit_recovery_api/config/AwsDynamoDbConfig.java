package com.gabrielformario.credit_recovery_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AwsDynamoDbConfig {

	@Bean
	public DynamoDbClient dynamoDbClient(@Value("${aws.region}") String awsRegion) {
		return DynamoDbClient.builder()
				.region(Region.of(awsRegion))
				.build();
	}
}
