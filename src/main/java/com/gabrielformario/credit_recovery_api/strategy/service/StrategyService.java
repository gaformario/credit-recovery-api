package com.gabrielformario.credit_recovery_api.strategy.service;

import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.engine.CreditStrategyEngine;
import com.gabrielformario.credit_recovery_api.strategy.exception.StrategyNotFoundException;
import com.gabrielformario.credit_recovery_api.strategy.repository.StrategyRepository;
import org.springframework.stereotype.Service;

@Service
public class StrategyService {

	private final CreditStrategyEngine creditStrategyEngine;
	private final StrategyRepository strategyRepository;

	public StrategyService(CreditStrategyEngine creditStrategyEngine, StrategyRepository strategyRepository) {
		this.creditStrategyEngine = creditStrategyEngine;
		this.strategyRepository = strategyRepository;
	}

	public StrategyResponse generateStrategy(StrategyRequest request) {
		StrategyResponse response = creditStrategyEngine.generate(request);
		strategyRepository.save(request, response);
		return response;
	}

	public StrategyResponse findStrategyByCustomerId(String customerId) {
		return strategyRepository.findByCustomerId(customerId)
				.orElseThrow(() -> new StrategyNotFoundException(customerId));
	}
}
