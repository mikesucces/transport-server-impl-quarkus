package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.PaymentAccountDto;
import ci.transit.system.transport.server.impl.persistence.payment.PaymentAccount;

/**
 * Cette classe convertit les entites du module Moyens de paiement en DTOs.
 *
 * @author Transit
 *
 */
public final class PaymentAccountMapper {

    private PaymentAccountMapper() {
    }

    public static PaymentAccountDto toDto(PaymentAccount entity) {
        PaymentAccountDto dto = new PaymentAccountDto();
        dto.identifier = entity.getUuid();
        dto.passengerIdentifier = entity.getPassenger().getUuid();
        dto.method = entity.getMethod().name();
        dto.label = entity.getLabel();
        dto.reference = entity.getReference();
        dto.defaultAccount = entity.isDefaultAccount();
        dto.active = entity.isActive();
        return dto;
    }
}
