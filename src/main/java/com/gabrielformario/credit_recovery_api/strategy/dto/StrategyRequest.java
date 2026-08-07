package com.gabrielformario.credit_recovery_api.strategy.dto;

import com.gabrielformario.credit_recovery_api.strategy.domain.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record StrategyRequest(
		@Schema(example = "PJ-12345", description = "Identificador do cliente PJ")
		@NotBlank
		String customerId,

		@Schema(example = "Empresa XPTO LTDA", description = "Nome da empresa")
		@NotBlank
		String companyName,

		@Schema(example = "45", description = "Quantidade de dias em atraso")
		@NotNull
		@PositiveOrZero
		Integer daysOverdue,

		@Schema(example = "15000.00", description = "Valor em aberto")
		@NotNull
		@PositiveOrZero
		BigDecimal outstandingAmount,

		@Schema(example = "420", description = "Score de credito na escala de 0 a 1000")
		@NotNull
		@Min(0)
		@Max(1000)
		Integer creditScore,

		@Schema(example = "CREDIT_CARD", description = "Tipo de produto do cliente")
		@NotNull
		ProductType productType
) {
}
