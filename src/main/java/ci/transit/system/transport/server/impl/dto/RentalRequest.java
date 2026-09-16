package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees d'une location.
 *
 * @author Transit
 *
 */
public class RentalRequest {

  @NotNull(message = "L'usager est obligatoire")
  public UUID passengerId;

  @NotNull(message = "Le vehicule est obligatoire")
  public UUID vehicleId;

  public String status;

  @NotNull(message = "La date de debut est obligatoire")
  public LocalDate startDate;

  @NotNull(message = "La date de fin est obligatoire")
  public LocalDate endDate;

  public LocalDate actualReturnDate;

  @PositiveOrZero
  public Integer startMileageKm;

  @PositiveOrZero
  public Integer endMileageKm;

  @NotNull(message = "Le montant total est obligatoire")
  @PositiveOrZero(message = "Le montant ne peut pas etre negatif")
  public BigDecimal totalAmount;

  @PositiveOrZero
  public BigDecimal depositAmount;

  public String notes;
}
