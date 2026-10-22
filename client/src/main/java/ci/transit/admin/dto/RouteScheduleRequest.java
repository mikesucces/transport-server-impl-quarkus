package ci.transit.admin.dto;

import java.time.LocalTime;

public class RouteScheduleRequest {
  public String dayOfWeek;
  public LocalTime departureTime;

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
