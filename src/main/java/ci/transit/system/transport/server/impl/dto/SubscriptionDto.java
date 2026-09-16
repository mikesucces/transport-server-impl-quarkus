package ci.transit.system.transport.server.impl.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Cette classe represente un abonnement expose par l'API.
 *
 * @author Transit
 *
 */
public class SubscriptionDto {
  public UUID identifier;
  public UUID passengerIdentifier;
  public String passengerFullName;
  public String plan;
  public String status;
  public LocalDate startsOn;
  public LocalDate endsOn;
  public long daysRemaining;
}
