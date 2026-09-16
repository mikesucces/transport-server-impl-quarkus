package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.StaffProfileDto;
import ci.transit.system.transport.server.impl.dto.StaffProfileRequest;
import ci.transit.system.transport.server.impl.ennumerations.StaffType;
import ci.transit.system.transport.server.impl.persistence.staff.StaffProfile;
import ci.transit.system.transport.server.impl.repository.DriverRepository;
import ci.transit.system.transport.server.impl.repository.StaffProfileRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.StaffMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StaffProfileService {

    @Inject
    StaffProfileRepository staffProfileRepository;

    @Inject
    DriverRepository driverRepository;

    public List<StaffProfileDto> findAll(String staffType) {
        List<StaffProfile> profiles = (staffType == null || staffType.isBlank())
            ? staffProfileRepository.findAll()
            : staffProfileRepository.findByStaffType(parseStaffType(staffType));
        return profiles.stream().map(StaffMapper::toDto).collect(Collectors.toList());
    }

    public StaffProfileDto findById(UUID identifier) {
        return StaffMapper.toDto(requireStaffProfile(identifier));
    }

    public StaffProfileDto create(StaffProfileRequest request) {
        if (staffProfileRepository.findByKeycloakSub(request.keycloakSub).isPresent()) {
            throw ApiException.badRequest("Ce sub Keycloak est deja utilise");
        }
        if (request.matricule != null && !request.matricule.isBlank()
                && staffProfileRepository.findByMatricule(request.matricule.trim()).isPresent()) {
            throw ApiException.badRequest("Ce matricule est deja utilise");
        }
        StaffProfile profile = new StaffProfile();
        apply(profile, request);
        return StaffMapper.toDto(staffProfileRepository.save(profile));
    }

    public StaffProfileDto update(UUID identifier, StaffProfileRequest request) {
        StaffProfile profile = requireStaffProfile(identifier);
        staffProfileRepository.findByKeycloakSub(request.keycloakSub).ifPresent(other -> {
            if (!other.getUuid().equals(identifier)) {
                throw ApiException.badRequest("Ce sub Keycloak est deja utilise");
            }
        });
        if (request.matricule != null && !request.matricule.isBlank()) {
            staffProfileRepository.findByMatricule(request.matricule.trim()).ifPresent(other -> {
                if (!other.getUuid().equals(identifier)) {
                    throw ApiException.badRequest("Ce matricule est deja utilise");
                }
            });
        }
        apply(profile, request);
        profile.setUpdatedAt(Instant.now());
        return StaffMapper.toDto(staffProfileRepository.save(profile));
    }

    public StaffProfileDto updateActive(UUID identifier, boolean active) {
        StaffProfile profile = requireStaffProfile(identifier);
        profile.setActive(active);
        profile.setUpdatedAt(Instant.now());
        return StaffMapper.toDto(staffProfileRepository.save(profile));
    }

    public void delete(UUID identifier) {
        requireStaffProfile(identifier);
        if (driverRepository.findByStaffProfile(identifier).isPresent()) {
            throw ApiException.badRequest("Impossible de supprimer un profil lie a un chauffeur");
        }
        staffProfileRepository.delete(identifier);
    }

    private void apply(StaffProfile profile, StaffProfileRequest request) {
        profile.setKeycloakSub(request.keycloakSub);
        profile.setUsername(request.username.trim());
        profile.setFullName(request.fullName.trim());
        profile.setPhone(request.phone);
        profile.setEmail(request.email);
        profile.setStaffType(parseStaffType(request.staffType));
        profile.setMatricule(request.matricule);
        if (request.active != null) {
            profile.setActive(request.active);
        }
    }

    private StaffType parseStaffType(String value) {
        try {
            return StaffType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Type de personnel invalide : " + value);
        }
    }

    private StaffProfile requireStaffProfile(UUID identifier) {
        StaffProfile profile = staffProfileRepository.findById(identifier);
        if (profile == null) {
            throw ApiException.notFound("Profil personnel introuvable");
        }
        return profile;
    }
}
