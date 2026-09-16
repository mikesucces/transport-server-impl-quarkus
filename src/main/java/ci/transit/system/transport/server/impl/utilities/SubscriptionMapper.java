package ci.transit.system.transport.server.impl.utilities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import ci.transit.system.transport.server.impl.dto.PaymentDto;
import ci.transit.system.transport.server.impl.dto.SubscriptionDto;
import ci.transit.system.transport.server.impl.persistence.subscription.Payment;
import ci.transit.system.transport.server.impl.persistence.subscription.Subscription;

/**
 * Cette classe convertit les entites du module Abonnements/Paiements en DTOs.
 *
 * @author Transit
 *
 */
public final class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    public static SubscriptionDto toDto(Subscription entity) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.identifier = entity.getUuid();
        dto.passengerIdentifier = entity.getPassenger().getUuid();
        dto.passengerFullName = entity.getPassenger().getFullName();
        dto.plan = entity.getPlan().name();
        dto.status = entity.getStatus().name();
        dto.startsOn = entity.getStartsOn();
        dto.endsOn = entity.getEndsOn();
        dto.daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), entity.getEndsOn());
        return dto;
    }

    public static PaymentDto toDto(Payment entity) {
        PaymentDto dto = new PaymentDto();
        dto.identifier = entity.getUuid();
        dto.subscriptionIdentifier = entity.getSubscription().getUuid();
        dto.passengerIdentifier = entity.getSubscription().getPassenger().getUuid();
        dto.passengerFullName = entity.getSubscription().getPassenger().getFullName();
        dto.amount = entity.getAmount();
        dto.method = entity.getMethod().name();
        dto.collectedBy = entity.getCollectedBy();
        dto.paymentAccountId = entity.getPaymentAccount() != null ? entity.getPaymentAccount().getUuid() : null;
        dto.paidAt = entity.getPaidAt();
        return dto;
    }
}
