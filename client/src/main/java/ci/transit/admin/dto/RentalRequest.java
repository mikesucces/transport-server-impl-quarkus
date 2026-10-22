package ci.transit.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class RentalRequest {
  public UUID passengerId;
  public UUID vehicleId;
  public String status;
  public LocalDate startDate;
  public LocalDate endDate;
  public BigDecimal totalAmount;
  public BigDecimal depositAmount;
  public String notes;

  public UUID getPassengerId() {
    return passengerId;
  }

  public void setPassengerId(UUID passengerId) {
    this.passengerId = passengerId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(UUID vehicleId) {
    this.vehicleId = vehicleId;
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
