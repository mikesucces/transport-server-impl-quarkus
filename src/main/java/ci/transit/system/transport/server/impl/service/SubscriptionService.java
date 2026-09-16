package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.SubscriptionDetailDto;
import ci.transit.system.transport.server.impl.dto.SubscriptionDto;
import ci.transit.system.transport.server.impl.dto.SubscriptionRenewRequest;
import ci.transit.system.transport.server.impl.dto.SubscriptionRequest;
import ci.transit.system.transport.server.impl.ennumerations.PaymentMethod;
import ci.transit.system.transport.server.impl.ennumerations.SubscriptionPlan;
import ci.transit.system.transport.server.impl.ennumerations.SubscriptionStatus;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.persistence.payment.PaymentAccount;
import ci.transit.system.transport.server.impl.persistence.subscription.Payment;
import ci.transit.system.transport.server.impl.persistence.subscription.Subscription;
import ci.transit.system.transport.server.impl.repository.PassengerRepository;
import ci.transit.system.transport.server.impl.repository.PaymentAccountRepository;
import ci.transit.system.transport.server.impl.repository.PaymentRepository;
import ci.transit.system.transport.server.impl.repository.SubscriptionRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.SubscriptionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SubscriptionService {

    @Inject
    SubscriptionRepository subscriptionRepository;

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    PassengerRepository passengerRepository;

    @Inject
    PaymentAccountRepository paymentAccountRepository;

    public List<SubscriptionDto> findAll(UUID passengerId, String status) {
        List<Subscription> subscriptions;
        if (status != null && !status.isBlank()) {
            subscriptions = subscriptionRepository.findByStatus(parseStatus(status));
        } else if (passengerId != null) {
            subscriptions = subscriptionRepository.findByPassenger(passengerId);
        } else {
            subscriptions = subscriptionRepository.findAll();
        }
        return subscriptions.stream().map(SubscriptionMapper::toDto).collect(Collectors.toList());
    }

    public List<SubscriptionDto> findByPassenger(UUID passengerId) {
        requirePassenger(passengerId);
        return subscriptionRepository.findByPassenger(passengerId)
            .stream().map(SubscriptionMapper::toDto).collect(Collectors.toList());
    }

    public SubscriptionDetailDto findById(UUID identifier) {
        return toDetail(requireSubscription(identifier));
    }

    public SubscriptionDetailDto create(SubscriptionRequest request) {
        Passenger passenger = requirePassenger(request.passengerId);
        SubscriptionPlan plan = parsePlan(request.plan);

        Subscription subscription = new Subscription();
        subscription.setPassenger(passenger);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartsOn(LocalDate.now());
        subscription.setEndsOn(LocalDate.now().plusDays(planDurationDays(plan)));
        Subscription saved = subscriptionRepository.save(subscription);

        recordPayment(saved, request.amount, request.method, request.collectedBy, request.paymentAccountId);
        return toDetail(saved);
    }

    public SubscriptionDetailDto renew(UUID identifier, SubscriptionRenewRequest request) {
        Subscription subscription = requireSubscription(identifier);
        SubscriptionPlan plan = (request.plan != null && !request.plan.isBlank())
            ? parsePlan(request.plan) : subscription.getPlan();

        LocalDate base = LocalDate.now().isAfter(subscription.getEndsOn())
            ? LocalDate.now() : subscription.getEndsOn();
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndsOn(base.plusDays(planDurationDays(plan)));
        subscription.setUpdatedAt(Instant.now());
        Subscription saved = subscriptionRepository.save(subscription);

        recordPayment(saved, request.amount, request.method, request.collectedBy, request.paymentAccountId);
        return toDetail(saved);
    }

    public SubscriptionDto updateStatus(UUID identifier, String status) {
        Subscription subscription = requireSubscription(identifier);
        subscription.setStatus(parseStatus(status));
        subscription.setUpdatedAt(Instant.now());
        return SubscriptionMapper.toDto(subscriptionRepository.save(subscription));
    }

    public void delete(UUID identifier) {
        Subscription subscription = requireSubscription(identifier);
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw ApiException.badRequest("Impossible de supprimer un abonnement actif");
        }
        subscriptionRepository.delete(identifier);
    }

    private void recordPayment(Subscription subscription, java.math.BigDecimal amount,
                                String method, UUID collectedBy, UUID paymentAccountId) {
        Payment payment = new Payment();
        payment.setSubscription(subscription);
        payment.setAmount(amount);
        payment.setMethod(parseMethod(method));
        payment.setCollectedBy(collectedBy);
        if (paymentAccountId != null) {
            payment.setPaymentAccount(requirePaymentAccount(paymentAccountId, subscription.getPassenger().getUuid()));
        }
        paymentRepository.save(payment);
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

    private SubscriptionDetailDto toDetail(Subscription subscription) {
        SubscriptionDetailDto dto = new SubscriptionDetailDto();
        dto.subscription = SubscriptionMapper.toDto(subscription);
        dto.payments = paymentRepository.findBySubscription(subscription.getUuid())
            .stream().map(SubscriptionMapper::toDto).collect(Collectors.toList());
        return dto;
    }

    /** Duree en jours de chaque formule. */
    private int planDurationDays(SubscriptionPlan plan) {
        return switch (plan) {
            case HEBDOMADAIRE -> 7;
            case MENSUEL -> 30;
            case TRIMESTRIEL -> 90;
        };
    }

    private SubscriptionPlan parsePlan(String value) {
        try {
            return SubscriptionPlan.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Formule d'abonnement invalide : " + value);
        }
    }

    private SubscriptionStatus parseStatus(String value) {
        try {
            return SubscriptionStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut d'abonnement invalide : " + value);
        }
    }

    private PaymentMethod parseMethod(String value) {
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Mode de paiement invalide : " + value);
        }
    }

    private Subscription requireSubscription(UUID identifier) {
        Subscription subscription = subscriptionRepository.findById(identifier);
        if (subscription == null) {
            throw ApiException.notFound("Abonnement introuvable");
        }
        return subscription;
    }

    private Passenger requirePassenger(UUID identifier) {
        Passenger passenger = passengerRepository.findById(identifier);
        if (passenger == null) {
            throw ApiException.notFound("Usager introuvable");
        }
        return passenger;
    }
}
