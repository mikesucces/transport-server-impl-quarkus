package ci.transit.admin.dto;

import java.time.Instant;
import java.util.UUID;

public class RotationDto {
  public UUID identifier;
  public UUID driverIdentifier;
  public String driverFullName;
  public UUID vehicleIdentifier;
  public String plateNumber;
  public UUID routeIdentifier;
  public String routeCode;
  public String routeName;
  public String status;
  public Instant scheduledStart;
  public Instant scheduledEnd;
  public String notes;

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public UUID getDriverIdentifier() {
    return driverIdentifier;
  }

  public void setDriverIdentifier(UUID driverIdentifier) {
    this.driverIdentifier = driverIdentifier;
  }

  public String getDriverFullName() {
    return driverFullName;
  }

  public void setDriverFullName(String driverFullName) {
    this.driverFullName = driverFullName;
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

  public UUID getRouteIdentifier() {
    return routeIdentifier;
  }

  public void setRouteIdentifier(UUID routeIdentifier) {
    this.routeIdentifier = routeIdentifier;
  }

  public String getRouteCode() {
    return routeCode;
  }

  public void setRouteCode(String routeCode) {
    this.routeCode = routeCode;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getScheduledStart() {
    return scheduledStart;
  }

  public void setScheduledStart(Instant scheduledStart) {
    this.scheduledStart = scheduledStart;
  }

  public Instant getScheduledEnd() {
    return scheduledEnd;
  }

  public void setScheduledEnd(Instant scheduledEnd) {
    this.scheduledEnd = scheduledEnd;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
