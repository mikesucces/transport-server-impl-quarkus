package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente le tableau de bord global, agregeant les
 * alertes flotte (M2) et les indicateurs des modules ajoutes depuis
 * (rotations, abonnements/paiements).
 *
 * @author Transit
 *
 */
public class DashboardDto {
  public FleetAlertsDto fleetAlerts;
  public List<SubscriptionDto> expiringSubscriptions = new ArrayList<>();
  public List<RotationDto> todayRotations = new ArrayList<>();
  public long activeRotationsCount;
  public long activeSubscriptionsCount;
  public BigDecimal monthlyRevenue;
}
