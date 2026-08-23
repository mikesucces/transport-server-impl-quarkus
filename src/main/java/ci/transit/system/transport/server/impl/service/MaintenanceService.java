package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.MaintenanceDto;
import ci.transit.system.transport.server.impl.dto.MaintenanceRequest;
import ci.transit.system.transport.server.impl.ennumerations.MaintenanceStatus;
import ci.transit.system.transport.server.impl.ennumerations.MaintenanceType;
import ci.transit.system.transport.server.impl.ennumerations.VehicleStatus;
import ci.transit.system.transport.server.impl.persistence.fleet.Maintenance;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.repository.MaintenanceRepository;
import ci.transit.system.transport.server.impl.repository.VehicleRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.FleetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MaintenanceService {

    @Inject
    MaintenanceRepository maintenanceRepository;

    @Inject
    VehicleRepository vehicleRepository;

    @Inject
    VehicleService vehicleService;

    public List<MaintenanceDto> findByVehicle(UUID vehicleIdentifier) {
        vehicleService.requireVehicle(vehicleIdentifier);
        return maintenanceRepository.findByVehicle(vehicleIdentifier)
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public List<MaintenanceDto> findUpcoming(int days) {
        return maintenanceRepository.findUpcomingBefore(LocalDate.now().plusDays(days))
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public MaintenanceDto create(UUID vehicleIdentifier, MaintenanceRequest request) {
        Vehicle vehicle = vehicleService.requireVehicle(vehicleIdentifier);
        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);
        apply(maintenance, request);
        Maintenance saved = maintenanceRepository.save(maintenance);

        // Un entretien en cours immobilise le car.
        if (saved.getStatus() == MaintenanceStatus.EN_COURS) {
            vehicle.setStatus(VehicleStatus.ENTRETIEN);
            vehicle.setUpdatedAt(Instant.now());
            vehicleRepository.save(vehicle);
        }
        return FleetMapper.toDto(saved);
    }

    public MaintenanceDto update(UUID identifier, MaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(identifier);
        if (maintenance == null) {
            throw ApiException.notFound("Entretien introuvable");
        }
        apply(maintenance, request);
        maintenance.setUpdatedAt(Instant.now());
        Maintenance saved = maintenanceRepository.save(maintenance);

        // Entretien termine : le car redevient disponible et son kilometrage est repris.
        if (saved.getStatus() == MaintenanceStatus.TERMINE) {
            Vehicle vehicle = saved.getVehicle();
            if (vehicle.getStatus() == VehicleStatus.ENTRETIEN) {
                vehicle.setStatus(VehicleStatus.DISPONIBLE);
            }
            if (saved.getMileageKm() != null && saved.getMileageKm() > vehicle.getMileageKm()) {
                vehicle.setMileageKm(saved.getMileageKm());
            }
            vehicle.setUpdatedAt(Instant.now());
            vehicleRepository.save(vehicle);
        }
        return FleetMapper.toDto(saved);
    }

    public void delete(UUID identifier) {
        if (maintenanceRepository.findById(identifier) == null) {
            throw ApiException.notFound("Entretien introuvable");
        }
        maintenanceRepository.delete(identifier);
    }

    private void apply(Maintenance maintenance, MaintenanceRequest request) {
        try {
            maintenance.setMaintenanceType(
                MaintenanceType.valueOf(request.maintenanceType.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Type d'entretien invalide : " + request.maintenanceType);
        }
        if (request.status != null && !request.status.isBlank()) {
            try {
                maintenance.setStatus(MaintenanceStatus.valueOf(request.status.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("Statut d'entretien invalide : " + request.status);
            }
        }
        maintenance.setScheduledOn(request.scheduledOn);
        maintenance.setPerformedOn(request.performedOn);
        maintenance.setMileageKm(request.mileageKm);
        maintenance.setNextDueKm(request.nextDueKm);
        maintenance.setNextDueOn(request.nextDueOn);
        maintenance.setCostAmount(request.costAmount);
        maintenance.setGarage(request.garage);
        maintenance.setDescription(request.description);
    }
}
