package com.gabrielformario.credit_recovery_api.strategy.dto;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record StrategyResponse(
		@Schema(example = "PJ-12345")
		String customerId,

		@Schema(example = "NEGATIVATION")
		CreditAction creditAction,

		@Schema(example = "WHATSAPP")
		CommunicationChannel communicationChannel,

		@Schema(example = "TEMPORARY_BLOCK")
		CardAction cardAction,

		@Schema(example = "true")
		boolean sendToPartnerOffice,

		@Schema(example = "true")
		boolean digitalChannelNotification,

		@Schema(example = "2026-08-07T15:00:00Z")
		Instant generatedAt
) {
}
