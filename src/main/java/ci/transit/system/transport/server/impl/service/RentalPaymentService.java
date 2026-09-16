package ci.transit.system.transport.server.impl.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.RentalPaymentDto;
import ci.transit.system.transport.server.impl.dto.RentalPaymentRequest;
import ci.transit.system.transport.server.impl.ennumerations.PaymentMethod;
import ci.transit.system.transport.server.impl.persistence.payment.PaymentAccount;
import ci.transit.system.transport.server.impl.persistence.rental.Rental;
import ci.transit.system.transport.server.impl.persistence.rental.RentalPayment;
import ci.transit.system.transport.server.impl.repository.PaymentAccountRepository;
import ci.transit.system.transport.server.impl.repository.RentalPaymentRepository;
import ci.transit.system.transport.server.impl.repository.RentalRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.RentalMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RentalPaymentService {

    @Inject
    RentalPaymentRepository rentalPaymentRepository;

    @Inject
    RentalRepository rentalRepository;

    @Inject
    PaymentAccountRepository paymentAccountRepository;

    public List<RentalPaymentDto> findByRental(UUID rentalIdentifier) {
        requireRental(rentalIdentifier);
        return rentalPaymentRepository.findByRental(rentalIdentifier)
            .stream().map(RentalMapper::toDto).collect(Collectors.toList());
    }

    public RentalPaymentDto create(UUID rentalIdentifier, RentalPaymentRequest request) {
        Rental rental = requireRental(rentalIdentifier);

        RentalPayment payment = new RentalPayment();
        payment.setRental(rental);
        payment.setAmount(request.amount);
        try {
            payment.setMethod(PaymentMethod.valueOf(request.method.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Mode de paiement invalide : " + request.method);
        }
        payment.setCollectedBy(request.collectedBy);
        if (request.paymentAccountId != null) {
            payment.setPaymentAccount(requirePaymentAccount(request.paymentAccountId, rental.getPassenger().getUuid()));
        }
        return RentalMapper.toDto(rentalPaymentRepository.save(payment));
    }

    public void delete(UUID identifier) {
        if (rentalPaymentRepository.findById(identifier) == null) {
            throw ApiException.notFound("Paiement introuvable");
        }
        rentalPaymentRepository.delete(identifier);
    }

    private PaymentAccount requirePaymentAccount(UUID identifier, UUID passengerIdentifier) {
        PaymentAccount account = paymentAccountRepository.findById(identifier);
        if (account == null) {
            throw ApiException.notFound("Moyen de paiement introuvable");
        }
        if (!account.getPassenger().getUuid().equals(passengerIdentifier)) {
            throw ApiException.badRequest("Ce moyen de paiement n'appartient pas a cet usager");
        }
        return account;
    }

    private Rental requireRental(UUID identifier) {
        Rental rental = rentalRepository.findById(identifier);
        if (rental == null) {
            throw ApiException.notFound("Location introuvable");
        }
        return rental;
    }
}
