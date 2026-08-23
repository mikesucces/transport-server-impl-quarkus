package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.VehicleDetailDto;
import ci.transit.system.transport.server.impl.dto.VehicleDto;
import ci.transit.system.transport.server.impl.dto.VehicleRequest;
import ci.transit.system.transport.server.impl.ennumerations.VehicleStatus;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.repository.MaintenanceRepository;
import ci.transit.system.transport.server.impl.repository.VehicleDocumentRepository;
import ci.transit.system.transport.server.impl.repository.VehicleRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.FleetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class VehicleService {

    @Inject
    VehicleRepository vehicleRepository;

    @Inject
    VehicleDocumentRepository documentRepository;

    @Inject
    MaintenanceRepository maintenanceRepository;

    public List<VehicleDto> findAll(String status) {
        List<Vehicle> vehicles = (status == null || status.isBlank())
            ? vehicleRepository.findAll()
            : vehicleRepository.findByStatus(parseStatus(status));
        return vehicles.stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public List<VehicleDto> findAvailable() {
        return vehicleRepository.findByStatus(VehicleStatus.DISPONIBLE)
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public VehicleDetailDto findById(UUID identifier) {
        Vehicle vehicle = requireVehicle(identifier);
        VehicleDetailDto detail = new VehicleDetailDto();
        detail.vehicle = FleetMapper.toDto(vehicle);
        detail.documents = documentRepository.findByVehicle(identifier)
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
        detail.maintenances = maintenanceRepository.findByVehicle(identifier)
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
        return detail;
    }

    public VehicleDto create(VehicleRequest request) {
        String plateNumber = request.plateNumber.trim().toUpperCase();
        if (vehicleRepository.findByPlateNumber(plateNumber).isPresent()) {
            throw ApiException.badRequest(
                "Un car avec l'immatriculation " + plateNumber + " existe deja");
        }
        Vehicle vehicle = new Vehicle();
        apply(vehicle, request, plateNumber);
        return FleetMapper.toDto(vehicleRepository.save(vehicle));
    }

    public VehicleDto update(UUID identifier, VehicleRequest request) {
        Vehicle vehicle = requireVehicle(identifier);
        String plateNumber = request.plateNumber.trim().toUpperCase();
        vehicleRepository.findByPlateNumber(plateNumber).ifPresent(other -> {
            if (!other.getUuid().equals(identifier)) {
                throw ApiException.badRequest("Immatriculation deja utilisee : " + plateNumber);
            }
        });
        apply(vehicle, request, plateNumber);
        vehicle.setUpdatedAt(Instant.now());
        return FleetMapper.toDto(vehicleRepository.save(vehicle));
    }

    public VehicleDto updateStatus(UUID identifier, String status) {
        Vehicle vehicle = requireVehicle(identifier);
        vehicle.setStatus(parseStatus(status));
        vehicle.setUpdatedAt(Instant.now());
        return FleetMapper.toDto(vehicleRepository.save(vehicle));
    }

    /**
     * Le kilometrage ne peut que croitre : une diminution traduit
     * presque toujours une erreur de saisie.
     */
    public VehicleDto updateMileage(UUID identifier, Integer mileageKm) {
        Vehicle vehicle = requireVehicle(identifier);
        if (mileageKm == null || mileageKm < 0) {
            throw ApiException.badRequest("Kilometrage invalide");
        }
        if (mileageKm < vehicle.getMileageKm()) {
            throw ApiException.badRequest(
                "Le kilometrage ne peut pas diminuer (actuel : " + vehicle.getMileageKm() + " km)");
        }
        vehicle.setMileageKm(mileageKm);
        vehicle.setUpdatedAt(Instant.now());
        return FleetMapper.toDto(vehicleRepository.save(vehicle));
    }

    public void delete(UUID identifier) {
        Vehicle vehicle = requireVehicle(identifier);
        if (vehicle.getStatus() == VehicleStatus.EN_SERVICE) {
            throw ApiException.badRequest("Impossible de supprimer un car en service");
        }
        vehicleRepository.delete(identifier);
    }

    public Vehicle requireVehicle(UUID identifier) {
        Vehicle vehicle = vehicleRepository.findById(identifier);
        if (vehicle == null) {
            throw ApiException.notFound("Car introuvable");
        }
        return vehicle;
    }

    private void apply(Vehicle vehicle, VehicleRequest request, String plateNumber) {
        vehicle.setPlateNumber(plateNumber);
        vehicle.setBrand(request.brand.trim());
        vehicle.setModel(request.model.trim());
        vehicle.setYear(request.year);
        vehicle.setCapacity(request.capacity);
        if (request.mileageKm != null) {
            vehicle.setMileageKm(request.mileageKm);
        }
        if (request.status != null && !request.status.isBlank()) {
            vehicle.setStatus(parseStatus(request.status));
        }
        vehicle.setCommissionedAt(request.commissionedAt);
        vehicle.setNotes(request.notes);
    }

    private VehicleStatus parseStatus(String value) {
        try {
            return VehicleStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut de car invalide : " + value);
        }
    }
}
