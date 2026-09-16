package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.RentalDetailDto;
import ci.transit.system.transport.server.impl.dto.RentalDto;
import ci.transit.system.transport.server.impl.dto.RentalRequest;
import ci.transit.system.transport.server.impl.ennumerations.RentalStatus;
import ci.transit.system.transport.server.impl.ennumerations.VehicleStatus;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.rental.Rental;
import ci.transit.system.transport.server.impl.repository.PassengerRepository;
import ci.transit.system.transport.server.impl.repository.RentalPaymentRepository;
import ci.transit.system.transport.server.impl.repository.RentalRepository;
import ci.transit.system.transport.server.impl.repository.VehicleRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.RentalMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RentalService {

    @Inject
    RentalRepository rentalRepository;

    @Inject
    PassengerRepository passengerRepository;

    @Inject
    VehicleRepository vehicleRepository;

    @Inject
    RentalPaymentRepository rentalPaymentRepository;

    public List<RentalDto> findAll(String status, UUID passengerId, UUID vehicleId) {
        List<Rental> rentals;
        if (status != null && !status.isBlank()) {
            rentals = rentalRepository.findByStatus(parseStatus(status));
        } else if (passengerId != null) {
            rentals = rentalRepository.findByPassenger(passengerId);
        } else if (vehicleId != null) {
            rentals = rentalRepository.findByVehicle(vehicleId);
        } else {
            rentals = rentalRepository.findAll();
        }
        return rentals.stream().map(RentalMapper::toDto).collect(Collectors.toList());
    }

    public List<RentalDto> findByPassenger(UUID passengerIdentifier) {
        requirePassenger(passengerIdentifier);
        return rentalRepository.findByPassenger(passengerIdentifier)
            .stream().map(RentalMapper::toDto).collect(Collectors.toList());
    }

    public List<RentalDto> findByVehicle(UUID vehicleIdentifier) {
        requireVehicle(vehicleIdentifier);
        return rentalRepository.findByVehicle(vehicleIdentifier)
            .stream().map(RentalMapper::toDto).collect(Collectors.toList());
    }

    public RentalDetailDto findById(UUID identifier) {
        return toDetail(requireRental(identifier));
    }

    public RentalDetailDto create(RentalRequest request) {
        Passenger passenger = requirePassenger(request.passengerId);
        Vehicle vehicle = requireVehicle(request.vehicleId);

        Rental rental = new Rental();
        rental.setPassenger(passenger);
        rental.setVehicle(vehicle);
        apply(rental, request);

        if (rental.getStatus() == RentalStatus.EN_COURS) {
            requireAvailable(vehicle);
        }
        Rental saved = rentalRepository.save(rental);
        applySideEffects(saved, null, vehicle);
        return toDetail(saved);
    }

    public RentalDetailDto update(UUID identifier, RentalRequest request) {
        Rental rental = requireRental(identifier);
        RentalStatus previousStatus = rental.getStatus();
        Passenger passenger = requirePassenger(request.passengerId);
        Vehicle vehicle = requireVehicle(request.vehicleId);
        rental.setPassenger(passenger);
        rental.setVehicle(vehicle);
        apply(rental, request);

        if (rental.getStatus() == RentalStatus.EN_COURS && previousStatus != RentalStatus.EN_COURS) {
            requireAvailable(vehicle);
        }
        rental.setUpdatedAt(Instant.now());
        Rental saved = rentalRepository.save(rental);
        applySideEffects(saved, previousStatus, vehicle);
        return toDetail(saved);
    }

    public RentalDto updateStatus(UUID identifier, String status) {
        Rental rental = requireRental(identifier);
        RentalStatus previousStatus = rental.getStatus();
        RentalStatus newStatus = parseStatus(status);
        Vehicle vehicle = rental.getVehicle();

        if (newStatus == RentalStatus.EN_COURS && previousStatus != RentalStatus.EN_COURS) {
            requireAvailable(vehicle);
        }
        rental.setStatus(newStatus);
        rental.setUpdatedAt(Instant.now());
        Rental saved = rentalRepository.save(rental);
        applySideEffects(saved, previousStatus, vehicle);
        return RentalMapper.toDto(saved);
    }

    public void delete(UUID identifier) {
        Rental rental = requireRental(identifier);
        if (rental.getStatus() == RentalStatus.EN_COURS) {
            throw ApiException.badRequest("Impossible de supprimer une location en cours");
        }
        rentalRepository.delete(identifier);
    }

    /** Bascule le statut du vehicule selon la transition de la location. */
    private void applySideEffects(Rental rental, RentalStatus previousStatus, Vehicle vehicle) {
        RentalStatus status = rental.getStatus();
        boolean wasActive = previousStatus == RentalStatus.EN_COURS;

        if (status == RentalStatus.EN_COURS && !wasActive) {
            vehicle.setStatus(VehicleStatus.EN_SERVICE);
            vehicle.setUpdatedAt(Instant.now());
            vehicleRepository.save(vehicle);
        } else if ((status == RentalStatus.TERMINEE || status == RentalStatus.ANNULEE) && wasActive) {
            if (vehicle.getStatus() == VehicleStatus.EN_SERVICE) {
                vehicle.setStatus(VehicleStatus.DISPONIBLE);
            }
            if (status == RentalStatus.TERMINEE) {
                if (rental.getActualReturnDate() == null) {
                    rental.setActualReturnDate(java.time.LocalDate.now());
                    rentalRepository.save(rental);
                }
                if (rental.getEndMileageKm() != null && rental.getEndMileageKm() > vehicle.getMileageKm()) {
                    vehicle.setMileageKm(rental.getEndMileageKm());
                }
            }
            vehicle.setUpdatedAt(Instant.now());
            vehicleRepository.save(vehicle);
        }
    }

    private void requireAvailable(Vehicle vehicle) {
        if (vehicle.getStatus() != VehicleStatus.DISPONIBLE) {
            throw ApiException.badRequest("Vehicule non disponible");
        }
    }

    private void apply(Rental rental, RentalRequest request) {
        if (request.status != null && !request.status.isBlank()) {
            rental.setStatus(parseStatus(request.status));
        }
        rental.setStartDate(request.startDate);
        rental.setEndDate(request.endDate);
        rental.setActualReturnDate(request.actualReturnDate);
        rental.setStartMileageKm(request.startMileageKm);
        rental.setEndMileageKm(request.endMileageKm);
        rental.setTotalAmount(request.totalAmount);
        rental.setDepositAmount(request.depositAmount);
        rental.setNotes(request.notes);
    }

    private RentalDetailDto toDetail(Rental rental) {
        RentalDetailDto dto = new RentalDetailDto();
        dto.rental = RentalMapper.toDto(rental);
        dto.payments = rentalPaymentRepository.findByRental(rental.getUuid())
            .stream().map(RentalMapper::toDto).collect(Collectors.toList());
        return dto;
    }

    private RentalStatus parseStatus(String value) {
        try {
            return RentalStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut de location invalide : " + value);
        }
    }

    private Rental requireRental(UUID identifier) {
        Rental rental = rentalRepository.findById(identifier);
        if (rental == null) {
            throw ApiException.notFound("Location introuvable");
        }
        return rental;
    }

    private Passenger requirePassenger(UUID identifier) {
        Passenger passenger = passengerRepository.findById(identifier);
        if (passenger == null) {
            throw ApiException.notFound("Usager introuvable");
        }
        return passenger;
    }

    private Vehicle requireVehicle(UUID identifier) {
        Vehicle vehicle = vehicleRepository.findById(identifier);
        if (vehicle == null) {
            throw ApiException.notFound("Vehicule introuvable");
        }
        return vehicle;
    }
}
