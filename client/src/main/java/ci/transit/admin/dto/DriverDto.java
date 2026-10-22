package ci.transit.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMatricule() {
    return matricule;
  }

  public void setMatricule(String matricule) {
    this.matricule = matricule;
  }

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  public String getLicenseCategory() {
    return licenseCategory;
  }

  public void setLicenseCategory(String licenseCategory) {
    this.licenseCategory = licenseCategory;
  }

  public LocalDate getLicenseExpiresOn() {
    return licenseExpiresOn;
  }

  public void setLicenseExpiresOn(LocalDate licenseExpiresOn) {
    this.licenseExpiresOn = licenseExpiresOn;
  }

  public long getDaysUntilLicenseExpiry() {
    return daysUntilLicenseExpiry;
  }

  public void setDaysUntilLicenseExpiry(long daysUntilLicenseExpiry) {
    this.daysUntilLicenseExpiry = daysUntilLicenseExpiry;
  }

  public String getRemunerationType() {
    return remunerationType;
  }

  public void setRemunerationType(String remunerationType) {
    this.remunerationType = remunerationType;
  }

  public BigDecimal getMonthlySalary() {
    return monthlySalary;
  }

  public void setMonthlySalary(BigDecimal monthlySalary) {
    this.monthlySalary = monthlySalary;
  }

  public BigDecimal getTripRate() {
    return tripRate;
  }

  public void setTripRate(BigDecimal tripRate) {
    this.tripRate = tripRate;
  }

  public BigDecimal getCommissionRate() {
    return commissionRate;
  }

  public void setCommissionRate(BigDecimal commissionRate) {
    this.commissionRate = commissionRate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDate getHiredOn() {
    return hiredOn;
  }

  public void setHiredOn(LocalDate hiredOn) {
    this.hiredOn = hiredOn;
  }
}
