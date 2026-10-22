package ci.transit.admin.dto;

import java.time.Instant;
import java.util.UUID;

public class RotationRequest {
  public UUID driverId;
  public UUID vehicleId;
  public UUID routeId;
  public String status;
  public Instant scheduledStart;
  public Instant scheduledEnd;
  public String notes;

  public UUID getDriverId() {
    return driverId;
  }

  public void setDriverId(UUID driverId) {
    this.driverId = driverId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(UUID vehicleId) {
    this.vehicleId = vehicleId;
  }

  public UUID getRouteId() {
    return routeId;
  }

  public void setRouteId(UUID routeId) {
    this.routeId = routeId;
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
