package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.DashboardDto;
import ci.transit.system.transport.server.impl.ennumerations.SubscriptionStatus;
import ci.transit.system.transport.server.impl.repository.PaymentRepository;
import ci.transit.system.transport.server.impl.repository.RotationRepository;
import ci.transit.system.transport.server.impl.repository.SubscriptionRepository;
import ci.transit.system.transport.server.impl.utilities.RotationMapper;
import ci.transit.system.transport.server.impl.utilities.SubscriptionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Cette classe fournit la synthese transversale affichee sur le
 * tableau de bord global (au-dela de la flotte seule).
 *
 * @author Transit
 *
 */
@ApplicationScoped
public class DashboardService {

    @Inject
    FleetService fleetService;

    @Inject
    SubscriptionRepository subscriptionRepository;

    @Inject
    RotationRepository rotationRepository;

    @Inject
    PaymentRepository paymentRepository;

    public DashboardDto computeDashboard(int days) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Instant startOfToday = today.atStartOfDay(zone).toInstant();
        Instant startOfTomorrow = today.plusDays(1).atStartOfDay(zone).toInstant();
        Instant startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();

        DashboardDto dashboard = new DashboardDto();
        dashboard.fleetAlerts = fleetService.computeAlerts(days);
        dashboard.expiringSubscriptions = subscriptionRepository.findExpiringBefore(today.plusDays(days))
            .stream().map(SubscriptionMapper::toDto).collect(Collectors.toList());
        dashboard.todayRotations = rotationRepository.findScheduledBetween(startOfToday, startOfTomorrow)
            .stream().map(RotationMapper::toDto).collect(Collectors.toList());
        dashboard.activeRotationsCount = rotationRepository.findActive().size();
        dashboard.activeSubscriptionsCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        dashboard.monthlyRevenue = paymentRepository.sumAmountSince(startOfMonth);
        return dashboard;
    }
}
