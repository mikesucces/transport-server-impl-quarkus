package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente un code d'acces expose par l'API.
 * Le code en clair et son hash ne sont jamais renvoyes.
 *
 * @author Transit
 *
 */
public class AccessCodeDto {
  public UUID identifier;
  public UUID vehicleIdentifier;
  public String vehicleNumber;
  public UUID rotationIdentifier;
  public Instant validFrom;
  public Instant validUntil;
  public boolean active;
}
