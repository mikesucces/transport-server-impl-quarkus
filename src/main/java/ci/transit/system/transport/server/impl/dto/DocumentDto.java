package ci.transit.system.transport.server.impl.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Cette classe represente un document de car expose par l'API.
 *
 * @author Transit
 *
 */
public class DocumentDto {
  public UUID identifier;
  public UUID vehicleIdentifier;
  public String plateNumber;
  public String docType;
  public String reference;
  public String issuer;
  public LocalDate issuedOn;
  public LocalDate expiresOn;
  public long daysUntilExpiry;
  public boolean expired;
  public String fileUrl;
}
