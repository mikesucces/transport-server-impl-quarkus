package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente une rotation exposee par l'API.
 *
 * @author Transit
 *
 */
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
  public Instant startedAt;
  public Instant endedAt;
  public Integer startMileageKm;
  public Integer endMileageKm;
  public String accessCodeReference;
  public String notes;
}
