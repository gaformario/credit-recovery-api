package com.gabrielformario.credit_recovery_api.strategy.service;

import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.engine.CreditStrategyEngine;
import org.springframework.stereotype.Service;

@Service
public class StrategyService {

	private final CreditStrategyEngine creditStrategyEngine;

	public StrategyService(CreditStrategyEngine creditStrategyEngine) {
		this.creditStrategyEngine = creditStrategyEngine;
	}

	public StrategyResponse generateStrategy(StrategyRequest request) {
		return creditStrategyEngine.generate(request);
	}
}
