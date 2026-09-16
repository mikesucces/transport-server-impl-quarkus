package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente une presence validee exposee par l'API.
 *
 * @author Transit
 *
 */
public class AttendanceDto {
  public UUID identifier;
  public UUID rotationIdentifier;
  public UUID passengerIdentifier;
  public String passengerFullName;
  public UUID accessCodeIdentifier;
  public UUID controllerId;
  public Instant boardedAt;
}
