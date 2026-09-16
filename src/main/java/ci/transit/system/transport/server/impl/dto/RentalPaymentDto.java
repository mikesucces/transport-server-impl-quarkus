package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente un paiement de location expose par l'API.
 *
 * @author Transit
 *
 */
public class RentalPaymentDto {
  public UUID identifier;
  public UUID rentalIdentifier;
  public UUID paymentAccountId;
  public BigDecimal amount;
  public String method;
  public UUID collectedBy;
  public Instant paidAt;
}
