package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees d'un chauffeur.
 *
 * @author Transit
 *
 */
public class DriverRequest {

  @NotBlank(message = "Le nom est obligatoire")
  public String fullName;

  @NotBlank(message = "Le telephone est obligatoire")
  public String phone;

  public String matricule;

  @NotBlank(message = "Le numero de permis est obligatoire")
  public String licenseNumber;

  public String licenseCategory;

  @NotNull(message = "La date d'expiration du permis est obligatoire")
  public LocalDate licenseExpiresOn;

  public String remunerationType;

  @PositiveOrZero
  public BigDecimal monthlySalary;

  @PositiveOrZero
  public BigDecimal tripRate;

  @PositiveOrZero @DecimalMax("100.0")
  public BigDecimal commissionRate;

  public String status;
  public LocalDate hiredOn;
}
