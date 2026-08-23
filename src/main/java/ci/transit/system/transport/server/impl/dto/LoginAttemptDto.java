package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente une tentative de connexion usager exposee par l'API.
 *
 * @author Transit
 *
 */
public class LoginAttemptDto {
  public Long id;
  public String identifier;
  public UUID passengerIdentifier;
  public boolean success;
  public String ipAddress;
  public Instant attemptedAt;
}
