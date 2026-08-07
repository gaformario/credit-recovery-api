package com.gabrielformario.credit_recovery_api.strategy.service;

import com.gabrielformario.credit_recovery_api.strategy.domain.ProductType;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.engine.CreditStrategyEngine;
import com.gabrielformario.credit_recovery_api.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StrategyServiceTest {

	@Mock
	private StrategyRepository strategyRepository;

	private StrategyService strategyService;

	@BeforeEach
	void setUp() {
		strategyService = new StrategyService(new CreditStrategyEngine(), strategyRepository);
	}

	@Test
	void shouldPersistStrategyAfterGeneratingIt() {
		StrategyRequest request = new StrategyRequest(
				"PJ-12345",
				"Empresa XPTO LTDA",
				61,
				BigDecimal.valueOf(15000.00),
				420,
				ProductType.CREDIT_CARD
		);

		StrategyResponse response = strategyService.generateStrategy(request);

		assertEquals("PJ-12345", response.customerId());
		verify(strategyRepository).save(eq(request), eq(response));
	}
}
