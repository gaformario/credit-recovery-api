package com.gabrielformario.credit_recovery_api.strategy.repository;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class StrategyRepository {

	private static final String TABLE_NAME = "credit-recovery-strategies";

	private final DynamoDbClient dynamoDbClient;

	public StrategyRepository(DynamoDbClient dynamoDbClient) {
		this.dynamoDbClient = dynamoDbClient;
	}

	public void save(StrategyRequest request, StrategyResponse response) {
		dynamoDbClient.putItem(PutItemRequest.builder()
				.tableName(TABLE_NAME)
				.item(toItem(request, response))
				.build());
	}

	public Optional<StrategyResponse> findByCustomerId(String customerId) {
		GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
				.tableName(TABLE_NAME)
				.key(Map.of("customerId", stringValue(customerId)))
				.build());

		if (!response.hasItem()) {
			return Optional.empty();
		}

		return Optional.of(toResponse(response.item()));
	}

	private Map<String, AttributeValue> toItem(StrategyRequest request, StrategyResponse response) {
		Map<String, AttributeValue> item = new LinkedHashMap<>();

		item.put("customerId", stringValue(request.customerId()));
		item.put("companyName", stringValue(request.companyName()));
		item.put("daysOverdue", numberValue(request.daysOverdue()));
		item.put("outstandingAmount", numberValue(request.outstandingAmount().toPlainString()));
		item.put("creditScore", numberValue(request.creditScore()));
		item.put("productType", stringValue(request.productType().name()));
		item.put("creditAction", stringValue(response.creditAction().name()));
		item.put("communicationChannel", stringValue(response.communicationChannel().name()));
		item.put("cardAction", stringValue(response.cardAction().name()));
		item.put("sendToPartnerOffice", booleanValue(response.sendToPartnerOffice()));
		item.put("digitalChannelNotification", booleanValue(response.digitalChannelNotification()));
		item.put("generatedAt", stringValue(response.generatedAt().toString()));

		return item;
	}

	private StrategyResponse toResponse(Map<String, AttributeValue> item) {
		return new StrategyResponse(
				item.get("customerId").s(),
				CreditAction.valueOf(item.get("creditAction").s()),
				CommunicationChannel.valueOf(item.get("communicationChannel").s()),
				CardAction.valueOf(item.get("cardAction").s()),
				item.get("sendToPartnerOffice").bool(),
				item.get("digitalChannelNotification").bool(),
				Instant.parse(item.get("generatedAt").s())
		);
	}

	private AttributeValue stringValue(String value) {
		return AttributeValue.builder().s(value).build();
	}

	private AttributeValue numberValue(Number value) {
		return numberValue(value.toString());
	}

	private AttributeValue numberValue(String value) {
		return AttributeValue.builder().n(value).build();
	}

	private AttributeValue booleanValue(boolean value) {
		return AttributeValue.builder().bool(value).build();
	}
}
