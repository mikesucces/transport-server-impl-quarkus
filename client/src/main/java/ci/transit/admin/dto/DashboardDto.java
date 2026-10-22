package ci.transit.admin.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardDto {
  public FleetAlertsDto fleetAlerts = new FleetAlertsDto();
  public List<SubscriptionDto> expiringSubscriptions = new ArrayList<>();
  public List<RotationDto> todayRotations = new ArrayList<>();
  public long activeRotationsCount;
  public long activeSubscriptionsCount;
  public BigDecimal monthlyRevenue;

  public FleetAlertsDto getFleetAlerts() {
    return fleetAlerts;
  }

  public void setFleetAlerts(FleetAlertsDto fleetAlerts) {
    this.fleetAlerts = fleetAlerts;
  }

  public List<SubscriptionDto> getExpiringSubscriptions() {
    return expiringSubscriptions;
  }

  public void setExpiringSubscriptions(List<SubscriptionDto> expiringSubscriptions) {
    this.expiringSubscriptions = expiringSubscriptions;
  }

  public List<RotationDto> getTodayRotations() {
    return todayRotations;
  }

  public void setTodayRotations(List<RotationDto> todayRotations) {
    this.todayRotations = todayRotations;
  }

  public long getActiveRotationsCount() {
    return activeRotationsCount;
  }

  public void setActiveRotationsCount(long activeRotationsCount) {
    this.activeRotationsCount = activeRotationsCount;
  }

  public long getActiveSubscriptionsCount() {
    return activeSubscriptionsCount;
  }

  public void setActiveSubscriptionsCount(long activeSubscriptionsCount) {
    this.activeSubscriptionsCount = activeSubscriptionsCount;
  }

  public BigDecimal getMonthlyRevenue() {
    return monthlyRevenue;
  }

  public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
    this.monthlyRevenue = monthlyRevenue;
  }
}
