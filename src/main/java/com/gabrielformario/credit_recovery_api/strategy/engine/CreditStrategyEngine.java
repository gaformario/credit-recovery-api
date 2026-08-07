package com.gabrielformario.credit_recovery_api.strategy.engine;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.ProductType;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CreditStrategyEngine {

	public StrategyResponse generate(StrategyRequest request) {
		int daysOverdue = request.daysOverdue();

		return new StrategyResponse(
				request.customerId(),
				resolveCreditAction(daysOverdue),
				resolveCommunicationChannel(daysOverdue),
				resolveCardAction(request.productType(), daysOverdue),
				shouldSendToPartnerOffice(daysOverdue),
				shouldNotifyDigitalChannel(daysOverdue),
				Instant.now()
		);
	}

	private CreditAction resolveCreditAction(int daysOverdue) {
		if (daysOverdue <= 30) {
			return CreditAction.NONE;
		}

		return CreditAction.NEGATIVATION;
	}

	private CommunicationChannel resolveCommunicationChannel(int daysOverdue) {
		if (daysOverdue <= 10) {
			return CommunicationChannel.EMAIL;
		}

		return CommunicationChannel.WHATSAPP;
	}

	private CardAction resolveCardAction(ProductType productType, int daysOverdue) {
		if (productType == ProductType.CREDIT_CARD && daysOverdue > 60) {
			return CardAction.TEMPORARY_BLOCK;
		}

		return CardAction.NONE;
	}

	private boolean shouldSendToPartnerOffice(int daysOverdue) {
		return daysOverdue > 60;
	}

	private boolean shouldNotifyDigitalChannel(int daysOverdue) {
		return daysOverdue > 10;
	}
}
