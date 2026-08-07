package com.gabrielformario.credit_recovery_api.strategy.dto;

import com.gabrielformario.credit_recovery_api.strategy.domain.ProductType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record StrategyRequest(
		@NotBlank
		String customerId,

		@NotBlank
		String companyName,

		@NotNull
		@PositiveOrZero
		Integer daysOverdue,

		@NotNull
		@PositiveOrZero
		BigDecimal outstandingAmount,

		@NotNull
		@Min(0)
		@Max(1000)
		Integer creditScore,

		@NotNull
		ProductType productType
) {
}
