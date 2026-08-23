package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente la synthese alimentant le bloc
 * « A surveiller » du tableau de bord.
 *
 * @author Transit
 *
 */
public class FleetAlertsDto {
  public List<DocumentDto> expiringDocuments = new ArrayList<>();
  public List<DriverDto> expiringLicenses = new ArrayList<>();
  public List<MaintenanceDto> upcomingMaintenances = new ArrayList<>();
  public long vehiclesTotal;
  public long vehiclesInService;
  public long vehiclesInMaintenance;
  public BigDecimal monthlyPayroll;
}
