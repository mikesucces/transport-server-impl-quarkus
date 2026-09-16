package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.RotationDto;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;

/**
 * Cette classe convertit les entites du module Rotations en DTOs.
 *
 * @author Transit
 *
 */
public final class RotationMapper {

    private RotationMapper() {
    }

    public static RotationDto toDto(Rotation entity) {
        RotationDto dto = new RotationDto();
        dto.identifier = entity.getUuid();
        dto.driverIdentifier = entity.getDriver().getUuid();
        dto.driverFullName = entity.getDriver().getFullName();
        dto.vehicleIdentifier = entity.getVehicle().getUuid();
        dto.plateNumber = entity.getVehicle().getPlateNumber();
        dto.status = entity.getStatus().name();
        dto.scheduledStart = entity.getScheduledStart();
        dto.scheduledEnd = entity.getScheduledEnd();
        dto.startedAt = entity.getStartedAt();
        dto.endedAt = entity.getEndedAt();
        dto.startMileageKm = entity.getStartMileageKm();
        dto.endMileageKm = entity.getEndMileageKm();
        dto.accessCodeReference = entity.getAccessCodeReference();
        dto.notes = entity.getNotes();
        return dto;
    }
}
