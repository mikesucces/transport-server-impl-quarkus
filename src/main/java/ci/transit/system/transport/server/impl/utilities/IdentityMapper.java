package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.AccessCodeDto;
import ci.transit.system.transport.server.impl.dto.LoginAttemptDto;
import ci.transit.system.transport.server.impl.dto.PassengerDto;
import ci.transit.system.transport.server.impl.persistence.identity.AccessCode;
import ci.transit.system.transport.server.impl.persistence.identity.LoginAttempt;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;

/**
 * Cette classe assure la conversion des entites du perimetre identite
 * en objets de transfert.
 *
 * @author Transit
 *
 */
public final class IdentityMapper {

    private IdentityMapper() {
    }

    public static PassengerDto toDto(Passenger entity) {
        PassengerDto dto = new PassengerDto();
        dto.identifier = entity.getUuid();
        dto.fullName = entity.getFullName();
        dto.phone = entity.getPhone();
        dto.status = entity.getStatus().name();
        return dto;
    }

    public static AccessCodeDto toDto(AccessCode entity) {
        AccessCodeDto dto = new AccessCodeDto();
        dto.identifier = entity.getUuid();
        dto.vehicleIdentifier = entity.getVehicle().getUuid();
        dto.vehicleNumber = entity.getVehicleNumber();
        dto.validFrom = entity.getValidFrom();
        dto.validUntil = entity.getValidUntil();
        dto.active = entity.isActive();
        return dto;
    }

    public static LoginAttemptDto toDto(LoginAttempt entity) {
        LoginAttemptDto dto = new LoginAttemptDto();
        dto.id = entity.getId();
        dto.identifier = entity.getIdentifier();
        dto.passengerIdentifier = entity.getPassenger() != null ? entity.getPassenger().getUuid() : null;
        dto.success = entity.isSuccess();
        dto.ipAddress = entity.getIpAddress();
        dto.attemptedAt = entity.getAttemptedAt();
        return dto;
    }
}
