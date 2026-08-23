package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

/**
 * Cette classe represente un usager expose par l'API.
 *
 * @author Transit
 *
 */
public class PassengerDto {
  public UUID identifier;
  public String fullName;
  public String phone;
  public String status;
}
