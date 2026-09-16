package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees de renouvellement d'un abonnement
 * existant, finance par un nouveau paiement.
 *
 * @author Transit
 *
 */
public class SubscriptionRenewRequest {

  /** Formule optionnelle : si absente, la formule actuelle de l'abonnement est reconduite. */
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
