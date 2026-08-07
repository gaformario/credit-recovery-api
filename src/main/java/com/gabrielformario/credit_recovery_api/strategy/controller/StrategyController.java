package com.gabrielformario.credit_recovery_api.strategy.controller;

import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.service.StrategyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/strategies")
public class StrategyController {

	private final StrategyService strategyService;

	public StrategyController(StrategyService strategyService) {
		this.strategyService = strategyService;
	}

	@PostMapping
	public ResponseEntity<StrategyResponse> generateStrategy(@Valid @RequestBody StrategyRequest request) {
		StrategyResponse response = strategyService.generateStrategy(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}")
	public ResponseEntity<StrategyResponse> findStrategyByCustomerId(@PathVariable String customerId) {
		return ResponseEntity.ok(strategyService.findStrategyByCustomerId(customerId));
	}
}
