package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees de souscription d'un abonnement,
 * finance par un premier paiement.
 *
 * @author Transit
 *
 */
public class SubscriptionRequest {

  @NotNull(message = "L'usager est obligatoire")
  public UUID passengerId;

  @NotBlank(message = "La formule est obligatoire")
  public String plan;

  @NotNull(message = "Le montant est obligatoire")
  @PositiveOrZero(message = "Le montant ne peut pas etre negatif")
  public BigDecimal amount;

  @NotBlank(message = "Le mode de paiement est obligatoire")
  public String method;

  public UUID collectedBy;

  /** Moyen de paiement enregistre a utiliser, optionnel. */
  public UUID paymentAccountId;
}
