package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.PassengerDto;
import ci.transit.system.transport.server.impl.dto.PassengerRequest;
import ci.transit.system.transport.server.impl.ennumerations.PassengerStatus;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.repository.PassengerRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.IdentityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PassengerService {

    @Inject
    PassengerRepository passengerRepository;

    public List<PassengerDto> findAll(String status) {
        List<Passenger> passengers = (status == null || status.isBlank())
            ? passengerRepository.findAll()
            : passengerRepository.findByStatus(parseStatus(status));
        return passengers.stream().map(IdentityMapper::toDto).collect(Collectors.toList());
    }

    public PassengerDto findById(UUID identifier) {
        return IdentityMapper.toDto(requirePassenger(identifier));
    }

    public PassengerDto create(PassengerRequest request) {
        if (passengerRepository.findByPhone(request.phone.trim()).isPresent()) {
            throw ApiException.badRequest("Ce numero de telephone est deja utilise");
        }
        Passenger passenger = new Passenger();
        apply(passenger, request);
        return IdentityMapper.toDto(passengerRepository.save(passenger));
    }

    public PassengerDto update(UUID identifier, PassengerRequest request) {
        Passenger passenger = requirePassenger(identifier);
        passengerRepository.findByPhone(request.phone.trim()).ifPresent(other -> {
            if (!other.getUuid().equals(identifier)) {
                throw ApiException.badRequest("Ce numero de telephone est deja utilise");
            }
        });
        apply(passenger, request);
        passenger.setUpdatedAt(Instant.now());
        return IdentityMapper.toDto(passengerRepository.save(passenger));
    }

    public PassengerDto updateStatus(UUID identifier, String status) {
        Passenger passenger = requirePassenger(identifier);
        passenger.setStatus(parseStatus(status));
        passenger.setUpdatedAt(Instant.now());
        return IdentityMapper.toDto(passengerRepository.save(passenger));
    }

    public void delete(UUID identifier) {
        requirePassenger(identifier);
        passengerRepository.delete(identifier);
    }

    public Passenger requirePassenger(UUID identifier) {
        Passenger passenger = passengerRepository.findById(identifier);
        if (passenger == null) {
            throw ApiException.notFound("Usager introuvable");
        }
        return passenger;
    }

    private void apply(Passenger passenger, PassengerRequest request) {
        passenger.setFullName(request.fullName.trim());
        passenger.setPhone(request.phone.trim());
        if (request.status != null && !request.status.isBlank()) {
            passenger.setStatus(parseStatus(request.status));
        }
    }

    private PassengerStatus parseStatus(String value) {
        try {
            return PassengerStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut d'usager invalide : " + value);
        }
    }
}
