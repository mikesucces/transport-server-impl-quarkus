package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.AttendanceDto;
import ci.transit.system.transport.server.impl.persistence.attendance.Attendance;

/**
 * Cette classe convertit les entites du module Controle/Embarquement en DTOs.
 *
 * @author Transit
 *
 */
public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static AttendanceDto toDto(Attendance entity) {
        AttendanceDto dto = new AttendanceDto();
        dto.identifier = entity.getUuid();
        dto.rotationIdentifier = entity.getRotation().getUuid();
        dto.passengerIdentifier = entity.getPassenger().getUuid();
        dto.passengerFullName = entity.getPassenger().getFullName();
        dto.accessCodeIdentifier = entity.getAccessCode() != null ? entity.getAccessCode().getUuid() : null;
        dto.controllerId = entity.getControllerId();
        dto.boardedAt = entity.getBoardedAt();
        return dto;
    }
}
