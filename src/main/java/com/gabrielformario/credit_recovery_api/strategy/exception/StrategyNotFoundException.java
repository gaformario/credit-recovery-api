package com.gabrielformario.credit_recovery_api.strategy.exception;

public class StrategyNotFoundException extends RuntimeException {

	public StrategyNotFoundException(String customerId) {
		super("Strategy not found for customerId: " + customerId);
	}
}
