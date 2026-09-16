package ci.transit.system.transport.server.impl.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.AccessCodeDto;
import ci.transit.system.transport.server.impl.dto.AccessCodeGenerateRequest;
import ci.transit.system.transport.server.impl.dto.AccessCodeGeneratedDto;
import ci.transit.system.transport.server.impl.dto.AccessCodeRequest;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.identity.AccessCode;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;
import ci.transit.system.transport.server.impl.repository.AccessCodeRepository;
import ci.transit.system.transport.server.impl.repository.RotationRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.CodeHasher;
import ci.transit.system.transport.server.impl.utilities.IdentityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AccessCodeService {

    private static final int DEFAULT_VALIDITY_HOURS = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    AccessCodeRepository accessCodeRepository;

    @Inject
    VehicleService vehicleService;

    @Inject
    RotationRepository rotationRepository;

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

    /** Genere un code aleatoire pour une rotation ; utilise par le controller a l'embarquement. */
    public AccessCodeGeneratedDto generate(AccessCodeGenerateRequest request) {
        Rotation rotation = rotationRepository.findById(request.rotationId);
        if (rotation == null) {
            throw ApiException.notFound("Rotation introuvable");
        }
        Vehicle vehicle = rotation.getVehicle();
        int validityHours = request.validityHours != null ? request.validityHours : DEFAULT_VALIDITY_HOURS;
        String rawCode = String.format("%04d", RANDOM.nextInt(10000));
        Instant now = Instant.now();

        AccessCode accessCode = new AccessCode();
        accessCode.setVehicle(vehicle);
        accessCode.setVehicleNumber(vehicle.getPlateNumber());
        accessCode.setRotation(rotation);
        accessCode.setCodeHash(CodeHasher.hash(rawCode));
        accessCode.setValidFrom(now);
        accessCode.setValidUntil(now.plus(validityHours, ChronoUnit.HOURS));
        AccessCode saved = accessCodeRepository.save(accessCode);

        rotation.setAccessCodeReference(saved.getUuid().toString());
        rotationRepository.save(rotation);

        AccessCodeGeneratedDto dto = new AccessCodeGeneratedDto();
        dto.identifier = saved.getUuid();
        dto.rotationIdentifier = rotation.getUuid();
        dto.vehicleIdentifier = vehicle.getUuid();
        dto.vehicleNumber = vehicle.getPlateNumber();
        dto.code = rawCode;
        dto.validFrom = saved.getValidFrom();
        dto.validUntil = saved.getValidUntil();
        return dto;
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
