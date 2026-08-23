package ci.transit.system.transport.server.impl.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees de creation ou de modification d'un car.
 *
 * @author Transit
 *
 */
public class VehicleRequest {

  @NotBlank(message = "L'immatriculation est obligatoire")
  public String plateNumber;

  @NotBlank(message = "La marque est obligatoire")
  public String brand;

  @NotBlank(message = "Le modele est obligatoire")
  public String model;

  @Min(1950) @Max(2100)
  public Short year;

  @NotNull(message = "Le nombre de places est obligatoire")
  @Min(value = 1, message = "Le nombre de places doit etre superieur a 0")
  @Max(value = 120, message = "Le nombre de places est trop eleve")
  public Short capacity;

  @Min(value = 0, message = "Le kilometrage ne peut pas etre negatif")
  public Integer mileageKm;

  public String status;
  public LocalDate commissionedAt;
  public String notes;
}
