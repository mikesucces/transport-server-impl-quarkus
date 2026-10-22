package ci.transit.admin.dto;

import java.time.LocalDate;
import java.util.UUID;

public class SubscriptionDto {
  public UUID identifier;
  public UUID passengerIdentifier;
  public String passengerFullName;
  public String plan;
  public String status;
  public LocalDate startsOn;
  public LocalDate endsOn;
  public long daysRemaining;

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

  public String getPlan() {
    return plan;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDate getStartsOn() {
    return startsOn;
  }

  public void setStartsOn(LocalDate startsOn) {
    this.startsOn = startsOn;
  }

  public LocalDate getEndsOn() {
    return endsOn;
  }

  public void setEndsOn(LocalDate endsOn) {
    this.endsOn = endsOn;
  }

  public long getDaysRemaining() {
    return daysRemaining;
  }

  public void setDaysRemaining(long daysRemaining) {
    this.daysRemaining = daysRemaining;
  }
}
