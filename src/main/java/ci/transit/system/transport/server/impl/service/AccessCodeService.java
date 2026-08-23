package ci.transit.system.transport.server.impl.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.AccessCodeDto;
import ci.transit.system.transport.server.impl.dto.AccessCodeRequest;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.identity.AccessCode;
import ci.transit.system.transport.server.impl.repository.AccessCodeRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.CodeHasher;
import ci.transit.system.transport.server.impl.utilities.IdentityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AccessCodeService {

    @Inject
    AccessCodeRepository accessCodeRepository;

    @Inject
    VehicleService vehicleService;

    public List<AccessCodeDto> findAll(UUID vehicleIdentifier) {
        List<AccessCode> accessCodes = vehicleIdentifier == null
            ? accessCodeRepository.findAll()
            : accessCodeRepository.findByVehicle(vehicleIdentifier);
        return accessCodes.stream().map(IdentityMapper::toDto).collect(Collectors.toList());
    }

    public AccessCodeDto findById(UUID identifier) {
        return IdentityMapper.toDto(requireAccessCode(identifier));
    }

    public AccessCodeDto create(AccessCodeRequest request) {
        Vehicle vehicle = vehicleService.requireVehicle(request.vehicleIdentifier);
        AccessCode accessCode = new AccessCode();
        accessCode.setVehicle(vehicle);
        accessCode.setVehicleNumber(vehicle.getPlateNumber());
        apply(accessCode, request);
        return IdentityMapper.toDto(accessCodeRepository.save(accessCode));
    }

    public AccessCodeDto update(UUID identifier, AccessCodeRequest request) {
        AccessCode accessCode = requireAccessCode(identifier);
        if (!accessCode.getVehicle().getUuid().equals(request.vehicleIdentifier)) {
            Vehicle vehicle = vehicleService.requireVehicle(request.vehicleIdentifier);
            accessCode.setVehicle(vehicle);
            accessCode.setVehicleNumber(vehicle.getPlateNumber());
        }
        apply(accessCode, request);
        return IdentityMapper.toDto(accessCodeRepository.save(accessCode));
    }

    public void delete(UUID identifier) {
        requireAccessCode(identifier);
        accessCodeRepository.delete(identifier);
    }

    private AccessCode requireAccessCode(UUID identifier) {
        AccessCode accessCode = accessCodeRepository.findById(identifier);
        if (accessCode == null) {
            throw ApiException.notFound("Code d'acces introuvable");
        }
        return accessCode;
    }

    private void apply(AccessCode accessCode, AccessCodeRequest request) {
        if (!request.validUntil.isAfter(request.validFrom)) {
            throw ApiException.badRequest("La date de fin de validite doit etre posterieure a la date de debut");
        }
        accessCode.setCodeHash(CodeHasher.hash(request.code));
        accessCode.setValidFrom(request.validFrom);
        accessCode.setValidUntil(request.validUntil);
    }
}
