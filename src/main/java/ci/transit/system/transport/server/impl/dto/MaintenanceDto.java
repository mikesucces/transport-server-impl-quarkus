package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cette classe represente un entretien expose par l'API.
 *
 * @author Transit
 *
 */
public class MaintenanceDto {
  public UUID identifier;
  public UUID vehicleIdentifier;
  public String plateNumber;
  public String maintenanceType;
  public String status;
  public LocalDate scheduledOn;
  public LocalDate performedOn;
  public Integer mileageKm;
  public Integer nextDueKm;
  public LocalDate nextDueOn;
  public BigDecimal costAmount;
  public String garage;
  public String description;
}
