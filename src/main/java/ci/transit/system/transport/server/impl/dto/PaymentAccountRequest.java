package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees d'un moyen de paiement enregistre.
 *
 * @author Transit
 *
 */
public class PaymentAccountRequest {

  @NotNull(message = "L'usager est obligatoire")
  public UUID passengerId;

  @NotBlank(message = "Le mode de paiement est obligatoire")
  public String method;

  public String label;

  @NotBlank(message = "La reference est obligatoire")
  public String reference;

  public Boolean defaultAccount;
  public Boolean active;
}
