package ci.transit.admin.dto;

import java.util.ArrayList;
import java.util.List;

public class FleetAlertsDto {
  public List<DocumentDto> expiringDocuments = new ArrayList<>();
  public List<DriverDto> expiringLicenses = new ArrayList<>();
  public List<MaintenanceDto> upcomingMaintenances = new ArrayList<>();
  public long vehiclesTotal;
  public long vehiclesInService;
  public long vehiclesInMaintenance;

  public List<DocumentDto> getExpiringDocuments() {
    return expiringDocuments;
  }

  public void setExpiringDocuments(List<DocumentDto> expiringDocuments) {
    this.expiringDocuments = expiringDocuments;
  }

  public List<DriverDto> getExpiringLicenses() {
    return expiringLicenses;
  }

  public void setExpiringLicenses(List<DriverDto> expiringLicenses) {
    this.expiringLicenses = expiringLicenses;
  }

  public List<MaintenanceDto> getUpcomingMaintenances() {
    return upcomingMaintenances;
  }

  public void setUpcomingMaintenances(List<MaintenanceDto> upcomingMaintenances) {
    this.upcomingMaintenances = upcomingMaintenances;
  }

  public long getVehiclesTotal() {
    return vehiclesTotal;
  }

  public void setVehiclesTotal(long vehiclesTotal) {
    this.vehiclesTotal = vehiclesTotal;
  }

  public long getVehiclesInService() {
    return vehiclesInService;
  }

  public void setVehiclesInService(long vehiclesInService) {
    this.vehiclesInService = vehiclesInService;
  }

  public long getVehiclesInMaintenance() {
    return vehiclesInMaintenance;
  }

  public void setVehiclesInMaintenance(long vehiclesInMaintenance) {
    this.vehiclesInMaintenance = vehiclesInMaintenance;
  }
}
