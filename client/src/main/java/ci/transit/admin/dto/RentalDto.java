package ci.transit.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
  public BigDecimal totalAmount;
  public BigDecimal depositAmount;
  public String notes;

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public UUID getPassengerIdentifier() {
    return passengerIdentifier;
  }

  public void setPassengerIdentifier(UUID passengerIdentifier) {
    this.passengerIdentifier = passengerIdentifier;
  }

  public String getPassengerFullName() {
    return passengerFullName;
  }

  public void setPassengerFullName(String passengerFullName) {
    this.passengerFullName = passengerFullName;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public LocalDate getActualReturnDate() {
    return actualReturnDate;
  }

  public void setActualReturnDate(LocalDate actualReturnDate) {
    this.actualReturnDate = actualReturnDate;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public BigDecimal getDepositAmount() {
    return depositAmount;
  }

  public void setDepositAmount(BigDecimal depositAmount) {
    this.depositAmount = depositAmount;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
