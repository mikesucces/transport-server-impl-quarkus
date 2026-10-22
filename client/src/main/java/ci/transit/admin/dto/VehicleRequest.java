package ci.transit.admin.dto;

import java.time.LocalDate;

public class VehicleRequest {
  public String plateNumber;
  public String brand;
  public String model;
  public Short year;
  public Short capacity;
  public Integer mileageKm;
  public String status;
  public LocalDate commissionedAt;
  public String notes;

  public String getPlateNumber() {
    return plateNumber;
  }

  public void setPlateNumber(String plateNumber) {
    this.plateNumber = plateNumber;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Short getYear() {
    return year;
  }

  public void setYear(Short year) {
    this.year = year;
  }

  public Short getCapacity() {
    return capacity;
  }

  public void setCapacity(Short capacity) {
    this.capacity = capacity;
  }

  public Integer getMileageKm() {
    return mileageKm;
  }

  public void setMileageKm(Integer mileageKm) {
    this.mileageKm = mileageKm;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDate getCommissionedAt() {
    return commissionedAt;
  }

  public void setCommissionedAt(LocalDate commissionedAt) {
    this.commissionedAt = commissionedAt;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
