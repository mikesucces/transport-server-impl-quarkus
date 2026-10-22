package ci.transit.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MaintenanceRequest {
  public String maintenanceType;
  public String status;
  public LocalDate scheduledOn;
  public LocalDate performedOn;
  public Integer mileageKm;
  public BigDecimal costAmount;
  public String garage;
  public String description;

  public String getMaintenanceType() {
    return maintenanceType;
  }

  public void setMaintenanceType(String maintenanceType) {
    this.maintenanceType = maintenanceType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDate getScheduledOn() {
    return scheduledOn;
  }

  public void setScheduledOn(LocalDate scheduledOn) {
    this.scheduledOn = scheduledOn;
  }

  public LocalDate getPerformedOn() {
    return performedOn;
  }

  public void setPerformedOn(LocalDate performedOn) {
    this.performedOn = performedOn;
  }

  public Integer getMileageKm() {
    return mileageKm;
  }

  public void setMileageKm(Integer mileageKm) {
    this.mileageKm = mileageKm;
  }

  public BigDecimal getCostAmount() {
    return costAmount;
  }

  public void setCostAmount(BigDecimal costAmount) {
    this.costAmount = costAmount;
  }

  public String getGarage() {
    return garage;
  }

  public void setGarage(String garage) {
    this.garage = garage;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
