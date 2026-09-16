package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

/**
 * Cette classe represente un moyen de paiement enregistre expose par l'API.
 *
 * @author Transit
 *
 */
public class PaymentAccountDto {
  public UUID identifier;
  public UUID passengerIdentifier;
  public String method;
  public String label;
  public String reference;
  public boolean defaultAccount;
  public boolean active;
}
