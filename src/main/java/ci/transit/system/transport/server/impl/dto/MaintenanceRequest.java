package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees d'un entretien.
 *
 * @author Transit
 *
 */
public class MaintenanceRequest {

  @NotBlank(message = "Le type d'entretien est obligatoire")
  public String maintenanceType;

  public String status;
  public LocalDate scheduledOn;
  public LocalDate performedOn;

  @PositiveOrZero
  public Integer mileageKm;

  @PositiveOrZero
  public Integer nextDueKm;

  public LocalDate nextDueOn;

  @PositiveOrZero(message = "Le cout ne peut pas etre negatif")
  public BigDecimal costAmount;

  public String garage;
  public String description;
}
