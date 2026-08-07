package com.gabrielformario.credit_recovery_api.strategy.dto;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;

import java.time.Instant;

public record StrategyResponse(
		String customerId,
		CreditAction creditAction,
		CommunicationChannel communicationChannel,
		CardAction cardAction,
		boolean sendToPartnerOffice,
		boolean digitalChannelNotification,
		Instant generatedAt
) {
}
