package com.gabrielformario.credit_recovery_api.strategy.engine;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.ProductType;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyRequest;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;

@Component
public class CreditStrategyEngine {

    private static final int HIGH_CREDIT_SCORE = 700;

    private static final int SHORT_DELAY_DAYS = 10;
    private static final int NEGATIVATION_THRESHOLD_DAYS = 30;
    private static final int PARTNER_OFFICE_THRESHOLD_DAYS = 60;
    private static final int CARD_BLOCK_THRESHOLD_DAYS = 60;

    private static final BigDecimal LOW_OUTSTANDING_AMOUNT =
            BigDecimal.valueOf(1000);

    public StrategyResponse generate(StrategyRequest request) {
        int daysOverdue = request.daysOverdue();

        return new StrategyResponse(
                request.customerId(),
                resolveCreditAction(daysOverdue, request.creditScore()),
                resolveCommunicationChannel(
                        daysOverdue,
                        request.outstandingAmount()
                ),
                resolveCardAction(request.productType(), daysOverdue),
                shouldSendToPartnerOffice(daysOverdue),
                shouldNotifyDigitalChannel(daysOverdue),
                Instant.now()
        );
    }

    private CreditAction resolveCreditAction(
            int daysOverdue,
            int creditScore
    ) {
        if (daysOverdue == 0 && creditScore >= HIGH_CREDIT_SCORE) {
            return CreditAction.POSITIVATION;
        }

        if (daysOverdue > NEGATIVATION_THRESHOLD_DAYS) {
            return CreditAction.NEGATIVATION;
        }

        return CreditAction.NONE;
    }

    private CommunicationChannel resolveCommunicationChannel(
            int daysOverdue,
            BigDecimal outstandingAmount
    ) {
        if (daysOverdue >= 1
                && daysOverdue <= SHORT_DELAY_DAYS
                && outstandingAmount.compareTo(LOW_OUTSTANDING_AMOUNT) <= 0) {
            return CommunicationChannel.SMS;
        }

        if (daysOverdue <= SHORT_DELAY_DAYS) {
            return CommunicationChannel.EMAIL;
        }

        return CommunicationChannel.WHATSAPP;
    }

    private CardAction resolveCardAction(
            ProductType productType,
            int daysOverdue
    ) {
        if (productType == ProductType.CREDIT_CARD
                && daysOverdue > CARD_BLOCK_THRESHOLD_DAYS) {
            return CardAction.TEMPORARY_BLOCK;
        }

        return CardAction.NONE;
    }

    private boolean shouldSendToPartnerOffice(int daysOverdue) {
        return daysOverdue > PARTNER_OFFICE_THRESHOLD_DAYS;
    }

    private boolean shouldNotifyDigitalChannel(int daysOverdue) {
        return daysOverdue > SHORT_DELAY_DAYS;
    }
}