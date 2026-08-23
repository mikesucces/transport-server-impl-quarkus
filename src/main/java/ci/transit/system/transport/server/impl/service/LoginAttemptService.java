package ci.transit.system.transport.server.impl.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.LoginAttemptDto;
import ci.transit.system.transport.server.impl.dto.LoginAttemptRequest;
import ci.transit.system.transport.server.impl.persistence.identity.LoginAttempt;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.repository.LoginAttemptRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.IdentityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoginAttemptService {

    @Inject
    LoginAttemptRepository loginAttemptRepository;

    @Inject
    PassengerService passengerService;

    public List<LoginAttemptDto> findAll(UUID passengerIdentifier) {
        List<LoginAttempt> attempts = passengerIdentifier == null
            ? loginAttemptRepository.findAll()
            : loginAttemptRepository.findByPassenger(passengerIdentifier);
        return attempts.stream().map(IdentityMapper::toDto).collect(Collectors.toList());
    }

    public LoginAttemptDto findById(Long identifier) {
        return IdentityMapper.toDto(requireLoginAttempt(identifier));
    }

    public LoginAttemptDto create(LoginAttemptRequest request) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setIdentifier(request.identifier.trim());
        attempt.setSuccess(request.success);
        attempt.setIpAddress(request.ipAddress);
        if (request.passengerIdentifier != null) {
            Passenger passenger = passengerService.requirePassenger(request.passengerIdentifier);
            attempt.setPassenger(passenger);
        }
        return IdentityMapper.toDto(loginAttemptRepository.save(attempt));
    }

    public void delete(Long identifier) {
        requireLoginAttempt(identifier);
        loginAttemptRepository.delete(identifier);
    }

    private LoginAttempt requireLoginAttempt(Long identifier) {
        LoginAttempt attempt = loginAttemptRepository.findById(identifier);
        if (attempt == null) {
            throw ApiException.notFound("Tentative de connexion introuvable");
        }
        return attempt;
    }
}
