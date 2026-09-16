package ci.transit.system.transport.server.impl.dto;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Cette classe represente un horaire de ligne expose par l'API.
 *
 * @author Transit
 *
 */
public class RouteScheduleDto {
  public UUID identifier;
  public UUID routeIdentifier;
  public String dayOfWeek;
  public LocalTime departureTime;
}
