package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.RotationDto;
import ci.transit.system.transport.server.impl.dto.RotationRequest;
import ci.transit.system.transport.server.impl.ennumerations.DriverStatus;
import ci.transit.system.transport.server.impl.ennumerations.RotationStatus;
import ci.transit.system.transport.server.impl.ennumerations.VehicleStatus;
import ci.transit.system.transport.server.impl.persistence.fleet.Driver;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;
import ci.transit.system.transport.server.impl.repository.DriverRepository;
import ci.transit.system.transport.server.impl.repository.RotationRepository;
import ci.transit.system.transport.server.impl.repository.VehicleRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.RotationMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RotationService {

    @Inject
    RotationRepository rotationRepository;

    @Inject
    DriverRepository driverRepository;

    @Inject
    VehicleRepository vehicleRepository;

    public List<RotationDto> findAll(String status, UUID driverId, UUID vehicleId) {
        List<Rotation> rotations;
        if (status != null && !status.isBlank()) {
            rotations = rotationRepository.findByStatus(parseStatus(status));
        } else if (driverId != null) {
            rotations = rotationRepository.findByDriver(driverId);
        } else if (vehicleId != null) {
            rotations = rotationRepository.findByVehicle(vehicleId);
        } else {
            rotations = rotationRepository.findAll();
        }
        return rotations.stream().map(RotationMapper::toDto).collect(Collectors.toList());
    }

    public List<RotationDto> findActive() {
        return rotationRepository.findActive()
            .stream().map(RotationMapper::toDto).collect(Collectors.toList());
    }

    public List<RotationDto> findByDriver(UUID driverIdentifier) {
        requireDriver(driverIdentifier);
        return rotationRepository.findByDriver(driverIdentifier)
            .stream().map(RotationMapper::toDto).collect(Collectors.toList());
    }

    public List<RotationDto> findByVehicle(UUID vehicleIdentifier) {
        requireVehicle(vehicleIdentifier);
        return rotationRepository.findByVehicle(vehicleIdentifier)
            .stream().map(RotationMapper::toDto).collect(Collectors.toList());
    }

    public RotationDto findById(UUID identifier) {
        return RotationMapper.toDto(requireRotation(identifier));
    }

    public RotationDto create(RotationRequest request) {
        Driver driver = requireDriver(request.driverId);
        Vehicle vehicle = requireVehicle(request.vehicleId);

        Rotation rotation = new Rotation();
        rotation.setDriver(driver);
        rotation.setVehicle(vehicle);
        apply(rotation, request);

        if (rotation.getStatus() == RotationStatus.EN_COURS) {
            requireAvailable(driver, vehicle);
        }
        Rotation saved = rotationRepository.save(rotation);
        applySideEffects(saved, null, driver, vehicle);
        return RotationMapper.toDto(saved);
    }

    public RotationDto update(UUID identifier, RotationRequest request) {
        Rotation rotation = requireRotation(identifier);
        RotationStatus previousStatus = rotation.getStatus();
        Driver driver = requireDriver(request.driverId);
        Vehicle vehicle = requireVehicle(request.vehicleId);
        rotation.setDriver(driver);
        rotation.setVehicle(vehicle);
        apply(rotation, request);

        if (rotation.getStatus() == RotationStatus.EN_COURS && previousStatus != RotationStatus.EN_COURS) {
            requireAvailable(driver, vehicle);
        }
        rotation.setUpdatedAt(Instant.now());
        Rotation saved = rotationRepository.save(rotation);
        applySideEffects(saved, previousStatus, driver, vehicle);
        return RotationMapper.toDto(saved);
    }

    public RotationDto updateStatus(UUID identifier, String status) {
        Rotation rotation = requireRotation(identifier);
        RotationStatus previousStatus = rotation.getStatus();
        RotationStatus newStatus = parseStatus(status);
        Driver driver = rotation.getDriver();
        Vehicle vehicle = rotation.getVehicle();

        if (newStatus == RotationStatus.EN_COURS && previousStatus != RotationStatus.EN_COURS) {
            requireAvailable(driver, vehicle);
        }
        rotation.setStatus(newStatus);
        rotation.setUpdatedAt(Instant.now());
        Rotation saved = rotationRepository.save(rotation);
        applySideEffects(saved, previousStatus, driver, vehicle);
        return RotationMapper.toDto(saved);
    }

    public void delete(UUID identifier) {
        Rotation rotation = requireRotation(identifier);
        if (rotation.getStatus() == RotationStatus.EN_COURS) {
            throw ApiException.badRequest("Impossible de supprimer une rotation en cours");
        }
        rotationRepository.delete(identifier);
    }

    /** Bascule le statut du chauffeur et du vehicule selon la transition de la rotation. */
    private void applySideEffects(Rotation rotation, RotationStatus previousStatus, Driver driver, Vehicle vehicle) {
        RotationStatus status = rotation.getStatus();
        boolean wasActive = previousStatus == RotationStatus.EN_COURS;

        if (status == RotationStatus.EN_COURS && !wasActive) {
            driver.setStatus(DriverStatus.EN_ROTATION);
            vehicle.setStatus(VehicleStatus.EN_SERVICE);
            if (rotation.getStartedAt() == null) {
                rotation.setStartedAt(Instant.now());
                rotationRepository.save(rotation);
            }
            saveDriverAndVehicle(driver, vehicle);
        } else if ((status == RotationStatus.TERMINEE || status == RotationStatus.ANNULEE) && wasActive) {
            if (driver.getStatus() == DriverStatus.EN_ROTATION) {
                driver.setStatus(DriverStatus.DISPONIBLE);
            }
            if (vehicle.getStatus() == VehicleStatus.EN_SERVICE) {
                vehicle.setStatus(VehicleStatus.DISPONIBLE);
            }
            if (status == RotationStatus.TERMINEE) {
                if (rotation.getEndedAt() == null) {
                    rotation.setEndedAt(Instant.now());
                    rotationRepository.save(rotation);
                }
                if (rotation.getEndMileageKm() != null && rotation.getEndMileageKm() > vehicle.getMileageKm()) {
                    vehicle.setMileageKm(rotation.getEndMileageKm());
                }
            }
            saveDriverAndVehicle(driver, vehicle);
        }
    }

    private void saveDriverAndVehicle(Driver driver, Vehicle vehicle) {
        driver.setUpdatedAt(Instant.now());
        vehicle.setUpdatedAt(Instant.now());
        driverRepository.save(driver);
        vehicleRepository.save(vehicle);
    }

    private void requireAvailable(Driver driver, Vehicle vehicle) {
        if (driver.getStatus() != DriverStatus.DISPONIBLE) {
            throw ApiException.badRequest("Chauffeur non disponible");
        }
        if (vehicle.getStatus() != VehicleStatus.DISPONIBLE) {
            throw ApiException.badRequest("Vehicule non disponible");
        }
    }

    private void apply(Rotation rotation, RotationRequest request) {
        if (request.status != null && !request.status.isBlank()) {
            rotation.setStatus(parseStatus(request.status));
        }
        rotation.setScheduledStart(request.scheduledStart);
        rotation.setScheduledEnd(request.scheduledEnd);
        rotation.setStartedAt(request.startedAt);
        rotation.setEndedAt(request.endedAt);
        rotation.setStartMileageKm(request.startMileageKm);
        rotation.setEndMileageKm(request.endMileageKm);
        rotation.setAccessCodeReference(request.accessCodeReference);
        rotation.setNotes(request.notes);
    }

    private RotationStatus parseStatus(String value) {
        try {
            return RotationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut de rotation invalide : " + value);
        }
    }

    private Rotation requireRotation(UUID identifier) {
        Rotation rotation = rotationRepository.findById(identifier);
        if (rotation == null) {
            throw ApiException.notFound("Rotation introuvable");
        }
        return rotation;
    }

    private Driver requireDriver(UUID identifier) {
        Driver driver = driverRepository.findById(identifier);
        if (driver == null) {
            throw ApiException.notFound("Chauffeur introuvable");
        }
        return driver;
    }

    private Vehicle requireVehicle(UUID identifier) {
        Vehicle vehicle = vehicleRepository.findById(identifier);
        if (vehicle == null) {
            throw ApiException.notFound("Vehicule introuvable");
        }
        return vehicle;
    }
}
