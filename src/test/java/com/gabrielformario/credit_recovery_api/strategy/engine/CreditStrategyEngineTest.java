package com.gabrielformario.credit_recovery_api.strategy.engine;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.ProductType;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditStrategyEngineTest {

	private final CreditStrategyEngine creditStrategyEngine = new CreditStrategyEngine();

	@Test
	void shouldGenerateStrategyForUpToTenDaysOverdue() {
		StrategyResponse strategy = creditStrategyEngine.generate(requestWithDaysOverdue(10, ProductType.LOAN));

		assertEquals(CreditAction.NONE, strategy.creditAction());
		assertEquals(CommunicationChannel.EMAIL, strategy.communicationChannel());
		assertEquals(CardAction.NONE, strategy.cardAction());
		assertFalse(strategy.sendToPartnerOffice());
		assertFalse(strategy.digitalChannelNotification());
		assertNotNull(strategy.generatedAt());
	}

	@Test
	void shouldGenerateStrategyForElevenToThirtyDaysOverdue() {
		StrategyResponse strategy = creditStrategyEngine.generate(requestWithDaysOverdue(30, ProductType.LOAN));

		assertEquals(CreditAction.NONE, strategy.creditAction());
		assertEquals(CommunicationChannel.WHATSAPP, strategy.communicationChannel());
		assertEquals(CardAction.NONE, strategy.cardAction());
		assertFalse(strategy.sendToPartnerOffice());
		assertTrue(strategy.digitalChannelNotification());
	}

	@Test
	void shouldGenerateStrategyForThirtyOneToSixtyDaysOverdue() {
		StrategyResponse strategy = creditStrategyEngine.generate(requestWithDaysOverdue(60, ProductType.OVERDRAFT));

		assertEquals(CreditAction.NEGATIVATION, strategy.creditAction());
		assertEquals(CommunicationChannel.WHATSAPP, strategy.communicationChannel());
		assertEquals(CardAction.NONE, strategy.cardAction());
		assertFalse(strategy.sendToPartnerOffice());
		assertTrue(strategy.digitalChannelNotification());
	}

	@Test
	void shouldGenerateStrategyForMoreThanSixtyDaysOverdue() {
		StrategyResponse strategy = creditStrategyEngine.generate(requestWithDaysOverdue(61, ProductType.LOAN));

		assertEquals(CreditAction.NEGATIVATION, strategy.creditAction());
		assertEquals(CommunicationChannel.WHATSAPP, strategy.communicationChannel());
		assertEquals(CardAction.NONE, strategy.cardAction());
		assertTrue(strategy.sendToPartnerOffice());
		assertTrue(strategy.digitalChannelNotification());
	}

	@Test
	void shouldBlockCardTemporarilyWhenCreditCardHasMoreThanSixtyDaysOverdue() {
		StrategyResponse strategy = creditStrategyEngine.generate(requestWithDaysOverdue(61, ProductType.CREDIT_CARD));

		assertEquals(CreditAction.NEGATIVATION, strategy.creditAction());
		assertEquals(CommunicationChannel.WHATSAPP, strategy.communicationChannel());
		assertEquals(CardAction.TEMPORARY_BLOCK, strategy.cardAction());
		assertTrue(strategy.sendToPartnerOffice());
		assertTrue(strategy.digitalChannelNotification());
	}

	private StrategyRequest requestWithDaysOverdue(int daysOverdue, ProductType productType) {
		return new StrategyRequest(
				"PJ-12345",
				"Empresa XPTO LTDA",
				daysOverdue,
				BigDecimal.valueOf(15000.00),
				420,
				productType
		);
	}
}
