package ci.transit.system.transport.server.impl.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente la vue complete d'un abonnement : ses
 * caracteristiques et l'historique de ses paiements.
 *
 * @author Transit
 *
 */
public class SubscriptionDetailDto {
  public SubscriptionDto subscription;
  public List<PaymentDto> payments = new ArrayList<>();
}
