package ci.transit.system.transport.server.impl.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente la vue complete d'une location : ses
 * caracteristiques et l'historique de ses paiements.
 *
 * @author Transit
 *
 */
public class RentalDetailDto {
  public RentalDto rental;
  public List<RentalPaymentDto> payments = new ArrayList<>();
}
