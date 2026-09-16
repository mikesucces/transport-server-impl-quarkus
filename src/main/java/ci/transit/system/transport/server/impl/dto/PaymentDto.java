package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente un paiement expose par l'API.
 *
 * @author Transit
 *
 */
public class PaymentDto {
  public UUID identifier;
  public UUID subscriptionIdentifier;
  public UUID passengerIdentifier;
  public String passengerFullName;
  public BigDecimal amount;
  public String method;
  public UUID collectedBy;
  public Instant paidAt;
}
