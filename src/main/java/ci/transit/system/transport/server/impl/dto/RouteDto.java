package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

/**
 * Cette classe represente une ligne exposee par l'API.
 *
 * @author Transit
 *
 */
public class RouteDto {
  public UUID identifier;
  public String code;
  public String name;
  public String origin;
  public String destination;
  public Integer distanceKm;
  public String status;
}
