package ci.transit.system.transport.server.impl.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees d'un document de car.
 *
 * @author Transit
 *
 */
public class DocumentRequest {

  @NotBlank(message = "Le type de document est obligatoire")
  public String docType;

  public String reference;
  public String issuer;
  public LocalDate issuedOn;

  @NotNull(message = "La date d'expiration est obligatoire")
  public LocalDate expiresOn;

  public String fileUrl;
}
