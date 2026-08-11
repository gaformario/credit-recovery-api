package com.gabrielformario.credit_recovery_api.strategy.repository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Component
public class StrategyDynamoDbMapper {

    public Map<String, AttributeValue> toItem(
            StrategyRequest request,
            StrategyResponse response
    ) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();

        item.put(
                "customerId",
                stringValue(request.customerId())
        );

        item.put(
                "companyName",
                stringValue(request.companyName())
        );

        item.put(
                "daysOverdue",
                numberValue(request.daysOverdue())
        );

        item.put(
                "outstandingAmount",
                numberValue(request.outstandingAmount().toPlainString())
        );

        item.put(
                "creditScore",
                numberValue(request.creditScore())
        );

        item.put(
                "productType",
                stringValue(request.productType().name())
        );

        item.put(
                "creditAction",
                stringValue(response.creditAction().name())
        );

        item.put(
                "communicationChannel",
                stringValue(response.communicationChannel().name())
        );

        item.put(
                "cardAction",
                stringValue(response.cardAction().name())
        );

        item.put(
                "sendToPartnerOffice",
                booleanValue(response.sendToPartnerOffice())
        );

        item.put(
                "digitalChannelNotification",
                booleanValue(response.digitalChannelNotification())
        );

        item.put(
                "generatedAt",
                stringValue(response.generatedAt().toString())
        );

        return item;
    }

    public Map<String, AttributeValue> toCustomerKey(
            String customerId
    ) {
        return Map.of(
                "customerId",
                stringValue(customerId)
        );
    }

    public StrategyResponse toResponse(
            Map<String, AttributeValue> item
    ) {
        return new StrategyResponse(
                item.get("customerId").s(),
                CreditAction.valueOf(
                        item.get("creditAction").s()
                ),
                CommunicationChannel.valueOf(
                        item.get("communicationChannel").s()
                ),
                CardAction.valueOf(
                        item.get("cardAction").s()
                ),
                item.get("sendToPartnerOffice").bool(),
                item.get("digitalChannelNotification").bool(),
                Instant.parse(
                        item.get("generatedAt").s()
                )
        );
    }

    private AttributeValue stringValue(String value) {
        return AttributeValue.builder()
                .s(value)
                .build();
    }

    private AttributeValue numberValue(Number value) {
        return numberValue(value.toString());
    }

    private AttributeValue numberValue(String value) {
        return AttributeValue.builder()
                .n(value)
                .build();
    }

    private AttributeValue booleanValue(boolean value) {
        return AttributeValue.builder()
                .bool(value)
                .build();
    }
}