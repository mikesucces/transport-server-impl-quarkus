package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.StaffProfileDto;
import ci.transit.system.transport.server.impl.persistence.staff.StaffProfile;

/**
 * Cette classe convertit les entites du module Personnel en DTOs.
 *
 * @author Transit
 *
 */
public final class StaffMapper {

    private StaffMapper() {
    }

    public static StaffProfileDto toDto(StaffProfile entity) {
        StaffProfileDto dto = new StaffProfileDto();
        dto.identifier = entity.getUuid();
        dto.keycloakSub = entity.getKeycloakSub();
        dto.username = entity.getUsername();
        dto.fullName = entity.getFullName();
        dto.phone = entity.getPhone();
        dto.email = entity.getEmail();
        dto.staffType = entity.getStaffType().name();
        dto.matricule = entity.getMatricule();
        dto.active = entity.isActive();
        dto.firstSeenAt = entity.getFirstSeenAt();
        dto.lastLoginAt = entity.getLastLoginAt();
        return dto;
    }
}
