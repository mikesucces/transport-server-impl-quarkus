package ci.transit.admin.dto;

import java.time.LocalTime;
import java.util.UUID;

public class RouteScheduleDto {
  public UUID identifier;
  public UUID routeIdentifier;
  public String dayOfWeek;
  public LocalTime departureTime;

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public UUID getRouteIdentifier() {
    return routeIdentifier;
  }

  public void setRouteIdentifier(UUID routeIdentifier) {
    this.routeIdentifier = routeIdentifier;
  }

  public String getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(String dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  public LocalTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(LocalTime departureTime) {
    this.departureTime = departureTime;
  }
}
