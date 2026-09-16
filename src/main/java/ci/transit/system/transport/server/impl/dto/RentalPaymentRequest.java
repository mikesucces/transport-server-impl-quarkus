package ci.transit.system.transport.server.impl.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees d'un paiement de location.
 *
 * @author Transit
 *
 */
public class RentalPaymentRequest {

  @NotNull(message = "Le montant est obligatoire")
  @PositiveOrZero(message = "Le montant ne peut pas etre negatif")
  public BigDecimal amount;

  @NotBlank(message = "Le mode de paiement est obligatoire")
  public String method;

  public UUID collectedBy;
  public UUID paymentAccountId;
}
