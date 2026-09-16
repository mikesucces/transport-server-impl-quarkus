package ci.transit.system.transport.server.impl.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.PaymentDto;
import ci.transit.system.transport.server.impl.persistence.subscription.Payment;
import ci.transit.system.transport.server.impl.repository.PaymentRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.SubscriptionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    public List<PaymentDto> findAll(UUID subscriptionIdentifier) {
        List<Payment> payments = subscriptionIdentifier == null
            ? paymentRepository.findAll()
            : paymentRepository.findBySubscription(subscriptionIdentifier);
        return payments.stream().map(SubscriptionMapper::toDto).collect(Collectors.toList());
    }

    public PaymentDto findById(UUID identifier) {
        return SubscriptionMapper.toDto(requirePayment(identifier));
    }

    public void delete(UUID identifier) {
        requirePayment(identifier);
        paymentRepository.delete(identifier);
    }

    private Payment requirePayment(UUID identifier) {
        Payment payment = paymentRepository.findById(identifier);
        if (payment == null) {
            throw ApiException.notFound("Paiement introuvable");
        }
        return payment;
    }
}
