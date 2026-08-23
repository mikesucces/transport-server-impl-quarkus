package ci.transit.system.transport.server.impl.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Cette classe represente un car expose par l'API.
 *
 * @author Transit
 *
 */
public class VehicleDto {
  public UUID identifier;
  public String plateNumber;
  public String brand;
  public String model;
  public Short year;
  public Short capacity;
  public Integer mileageKm;
  public String status;
  public LocalDate commissionedAt;
  public String notes;
}
