package com.gabrielformario.credit_recovery_api.strategy.controller;

import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Strategies", description = "Geracao e consulta de estrategias de recuperacao de credito PJ")
public class StrategyController {

	private final StrategyService strategyService;

	public StrategyController(StrategyService strategyService) {
		this.strategyService = strategyService;
	}

	@PostMapping
	@Operation(summary = "Gerar estrategia de recuperacao")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Estrategia gerada e persistida com sucesso"),
			@ApiResponse(responseCode = "400", description = "Request invalido"),
			@ApiResponse(responseCode = "500", description = "Erro inesperado")
	})
	public ResponseEntity<StrategyResponse> generateStrategy(@Valid @RequestBody StrategyRequest request) {
		StrategyResponse response = strategyService.generateStrategy(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}")
	@Operation(summary = "Consultar estrategia por customerId")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Estrategia encontrada"),
			@ApiResponse(responseCode = "404", description = "Estrategia nao encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro inesperado")
	})
	public ResponseEntity<StrategyResponse> findStrategyByCustomerId(@PathVariable String customerId) {
		return ResponseEntity.ok(strategyService.findStrategyByCustomerId(customerId));
	}
}
