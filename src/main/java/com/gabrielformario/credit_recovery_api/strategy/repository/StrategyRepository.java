package com.gabrielformario.credit_recovery_api.strategy.repository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
public class StrategyRepository {

    private static final Logger logger =
            LoggerFactory.getLogger(StrategyRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final StrategyDynamoDbMapper mapper;
    private final String tableName;

    public StrategyRepository(
            DynamoDbClient dynamoDbClient,
            StrategyDynamoDbMapper mapper,
            @Value("${aws.dynamodb.table-name}") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.mapper = mapper;
        this.tableName = tableName;
    }

    public void save(StrategyRequest request, StrategyResponse response) {
        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(mapper.toItem(request, response))
                    .build();

            dynamoDbClient.putItem(putItemRequest);

        } catch (RuntimeException exception) {
            logger.error(
                    "strategy persistence failed customerId={} tableName={}",
                    request.customerId(),
                    tableName,
                    exception
            );

            throw exception;
        }
    }

    public Optional<StrategyResponse> findByCustomerId(String customerId) {
        try {
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(mapper.toCustomerKey(customerId))
                    .build();

            GetItemResponse response =
                    dynamoDbClient.getItem(getItemRequest);

            if (!response.hasItem()) {
                return Optional.empty();
            }

            return Optional.of(
                    mapper.toResponse(response.item())
            );

        } catch (RuntimeException exception) {
            logger.error(
                    "strategy query failed customerId={} tableName={}",
                    customerId,
                    tableName,
                    exception
            );

            throw exception;
        }
    }
}