package ci.transit.admin.dto;

import java.time.LocalDate;
import java.util.UUID;

public class DocumentDto {
  public UUID identifier;
  public UUID vehicleIdentifier;
  public String plateNumber;
  public String docType;
  public String reference;
  public String issuer;
  public LocalDate issuedOn;
  public LocalDate expiresOn;
  public long daysUntilExpiry;
  public boolean expired;

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public UUID getVehicleIdentifier() {
    return vehicleIdentifier;
  }

  public void setVehicleIdentifier(UUID vehicleIdentifier) {
    this.vehicleIdentifier = vehicleIdentifier;
  }

  public String getPlateNumber() {
    return plateNumber;
  }

  public void setPlateNumber(String plateNumber) {
    this.plateNumber = plateNumber;
  }

  public String getDocType() {
    return docType;
  }

  public void setDocType(String docType) {
    this.docType = docType;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public LocalDate getIssuedOn() {
    return issuedOn;
  }

  public void setIssuedOn(LocalDate issuedOn) {
    this.issuedOn = issuedOn;
  }

  public LocalDate getExpiresOn() {
    return expiresOn;
  }

  public void setExpiresOn(LocalDate expiresOn) {
    this.expiresOn = expiresOn;
  }

  public long getDaysUntilExpiry() {
    return daysUntilExpiry;
  }

  public void setDaysUntilExpiry(long daysUntilExpiry) {
    this.daysUntilExpiry = daysUntilExpiry;
  }

  public boolean isExpired() {
    return expired;
  }

  public void setExpired(boolean expired) {
    this.expired = expired;
  }
}
