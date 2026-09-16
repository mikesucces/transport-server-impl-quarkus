package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cette classe represente une location expose par l'API.
 *
 * @author Transit
 *
 */
public class RentalDto {
  public UUID identifier;
  public UUID passengerIdentifier;
  public String passengerFullName;
  public UUID vehicleIdentifier;
  public String plateNumber;
  public String status;
  public LocalDate startDate;
  public LocalDate endDate;
  public LocalDate actualReturnDate;
  public Integer startMileageKm;
  public Integer endMileageKm;
  public BigDecimal totalAmount;
  public BigDecimal depositAmount;
  public String notes;
}
