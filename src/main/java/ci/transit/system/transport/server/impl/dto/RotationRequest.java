package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees d'une rotation.
 *
 * @author Transit
 *
 */
public class RotationRequest {

  @NotNull(message = "Le chauffeur est obligatoire")
  public UUID driverId;

  @NotNull(message = "Le vehicule est obligatoire")
  public UUID vehicleId;

  /** Ligne optionnelle a laquelle rattacher la rotation. */
  public UUID routeId;

  public String status;

  @NotNull(message = "Le debut planifie est obligatoire")
  public Instant scheduledStart;

  public Instant scheduledEnd;
  public Instant startedAt;
  public Instant endedAt;

  @PositiveOrZero
  public Integer startMileageKm;

  @PositiveOrZero
  public Integer endMileageKm;

  public String accessCodeReference;
  public String notes;
}
