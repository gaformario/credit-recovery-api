package com.gabrielformario.credit_recovery_api.strategy.service;

import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.engine.CreditStrategyEngine;
import com.gabrielformario.credit_recovery_api.strategy.exception.StrategyNotFoundException;
import com.gabrielformario.credit_recovery_api.strategy.repository.StrategyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StrategyService {

	private static final Logger logger = LoggerFactory.getLogger(StrategyService.class);

	private final CreditStrategyEngine creditStrategyEngine;
	private final StrategyRepository strategyRepository;

	public StrategyService(CreditStrategyEngine creditStrategyEngine, StrategyRepository strategyRepository) {
		this.creditStrategyEngine = creditStrategyEngine;
		this.strategyRepository = strategyRepository;
	}

	public StrategyResponse generateStrategy(StrategyRequest request) {
		long startedAt = System.nanoTime();
		String customerId = request.customerId();

		logger.info("strategy generation started customerId={}", customerId);

		StrategyResponse response;
		try {
			response = creditStrategyEngine.generate(request);
		}
		catch (RuntimeException exception) {
			long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
			logger.error("strategy generation failed customerId={} durationMs={}", customerId, durationMs, exception);
			throw exception;
		}

		strategyRepository.save(request, response);

		long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
		logger.info(
				"strategy generated customerId={} creditAction={} communicationChannel={} cardAction={} sendToPartnerOffice={} digitalChannelNotification={} durationMs={}",
				customerId,
				response.creditAction(),
				response.communicationChannel(),
				response.cardAction(),
				response.sendToPartnerOffice(),
				response.digitalChannelNotification(),
				durationMs
		);

		return response;
	}

	public StrategyResponse findStrategyByCustomerId(String customerId) {
		return strategyRepository.findByCustomerId(customerId)
				.map(strategy -> {
					logger.info("strategy query found customerId={}", customerId);
					return strategy;
				})
				.orElseThrow(() -> {
					logger.warn("strategy query not found customerId={}", customerId);
					return new StrategyNotFoundException(customerId);
				});
	}
}
