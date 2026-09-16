package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente un code d'acces nouvellement genere. Contrairement
 * a AccessCodeDto, elle expose le code en clair : c'est la seule fois ou il
 * est communique, a charge du controller de le transmettre aux usagers.
 *
 * @author Transit
 *
 */
public class AccessCodeGeneratedDto {
  public UUID identifier;
  public UUID rotationIdentifier;
  public UUID vehicleIdentifier;
  public String vehicleNumber;
  public String code;
  public Instant validFrom;
  public Instant validUntil;
}
