package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cette classe represente un chauffeur expose par l'API.
 *
 * @author Transit
 *
 */
public class DriverDto {
  public UUID identifier;
  public String fullName;
  public String phone;
  public String matricule;
  public String licenseNumber;
  public String licenseCategory;
  public LocalDate licenseExpiresOn;
  public long daysUntilLicenseExpiry;
  public String remunerationType;
  public BigDecimal monthlySalary;
  public BigDecimal tripRate;
  public BigDecimal commissionRate;
  public String status;
  public LocalDate hiredOn;
  public boolean hasAccount;
}
