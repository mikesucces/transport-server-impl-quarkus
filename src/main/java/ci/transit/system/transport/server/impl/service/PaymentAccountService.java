package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.PaymentAccountDto;
import ci.transit.system.transport.server.impl.dto.PaymentAccountRequest;
import ci.transit.system.transport.server.impl.ennumerations.PaymentMethod;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.persistence.payment.PaymentAccount;
import ci.transit.system.transport.server.impl.repository.PassengerRepository;
import ci.transit.system.transport.server.impl.repository.PaymentAccountRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.PaymentAccountMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentAccountService {

    @Inject
    PaymentAccountRepository paymentAccountRepository;

    @Inject
    PassengerRepository passengerRepository;

    public List<PaymentAccountDto> findByPassenger(UUID passengerIdentifier) {
        requirePassenger(passengerIdentifier);
        return paymentAccountRepository.findByPassenger(passengerIdentifier)
            .stream().map(PaymentAccountMapper::toDto).collect(Collectors.toList());
    }

    public PaymentAccountDto findById(UUID identifier) {
        return PaymentAccountMapper.toDto(requireAccount(identifier));
    }

    public PaymentAccountDto create(PaymentAccountRequest request) {
        Passenger passenger = requirePassenger(request.passengerId);
        PaymentAccount account = new PaymentAccount();
        account.setPassenger(passenger);
        apply(account, request);
        PaymentAccount saved = paymentAccountRepository.save(account);
        if (saved.isDefaultAccount()) {
            clearOtherDefaults(passenger.getUuid(), saved.getUuid());
        }
        return PaymentAccountMapper.toDto(saved);
    }

    public PaymentAccountDto update(UUID identifier, PaymentAccountRequest request) {
        PaymentAccount account = requireAccount(identifier);
        if (!account.getPassenger().getUuid().equals(request.passengerId)) {
            account.setPassenger(requirePassenger(request.passengerId));
        }
        apply(account, request);
        account.setUpdatedAt(Instant.now());
        PaymentAccount saved = paymentAccountRepository.save(account);
        if (saved.isDefaultAccount()) {
            clearOtherDefaults(saved.getPassenger().getUuid(), saved.getUuid());
        }
        return PaymentAccountMapper.toDto(saved);
    }

    public PaymentAccountDto updateActive(UUID identifier, boolean active) {
        PaymentAccount account = requireAccount(identifier);
        account.setActive(active);
        account.setUpdatedAt(Instant.now());
        return PaymentAccountMapper.toDto(paymentAccountRepository.save(account));
    }

    public void delete(UUID identifier) {
        requireAccount(identifier);
        paymentAccountRepository.delete(identifier);
    }

    /** Un seul moyen par defaut a la fois par usager. */
    private void clearOtherDefaults(UUID passengerIdentifier, UUID keepIdentifier) {
        paymentAccountRepository.findByPassenger(passengerIdentifier).stream()
            .filter(a -> !a.getUuid().equals(keepIdentifier) && a.isDefaultAccount())
            .forEach(a -> {
                a.setDefaultAccount(false);
                a.setUpdatedAt(Instant.now());
                paymentAccountRepository.save(a);
            });
    }

    private void apply(PaymentAccount account, PaymentAccountRequest request) {
        try {
            account.setMethod(PaymentMethod.valueOf(request.method.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Mode de paiement invalide : " + request.method);
        }
        account.setLabel(request.label);
        account.setReference(request.reference.trim());
        if (request.defaultAccount != null) {
            account.setDefaultAccount(request.defaultAccount);
        }
        if (request.active != null) {
            account.setActive(request.active);
        }
    }

    private PaymentAccount requireAccount(UUID identifier) {
        PaymentAccount account = paymentAccountRepository.findById(identifier);
        if (account == null) {
            throw ApiException.notFound("Moyen de paiement introuvable");
        }
        return account;
    }

    private Passenger requirePassenger(UUID identifier) {
        Passenger passenger = passengerRepository.findById(identifier);
        if (passenger == null) {
            throw ApiException.notFound("Usager introuvable");
        }
        return passenger;
    }
}
