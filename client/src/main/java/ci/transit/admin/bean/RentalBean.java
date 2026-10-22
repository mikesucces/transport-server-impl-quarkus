package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.PassengerDto;
import ci.transit.admin.dto.RentalDto;
import ci.transit.admin.dto.RentalRequest;
import ci.transit.admin.dto.VehicleDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RentalBean extends FilterableList<RentalDto> {

  public static final List<String> STATUSES = List.of("RESERVEE", "EN_COURS", "TERMINEE", "ANNULEE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<RentalDto> rentals = new ArrayList<>();
  private List<PassengerDto> passengers = new ArrayList<>();
  private List<VehicleDto> vehicles = new ArrayList<>();
  private RentalRequest form = new RentalRequest();
  // String : pas de convertisseur UUID en JSF
  private String passengerId;
  private String vehicleId;

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      rentals = new ArrayList<>(api.rentals(null, null, null));
      passengers = new ArrayList<>(api.passengers("ACTIVE"));
      vehicles = new ArrayList<>(api.vehicles(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les locations", e);
    }
  }

  public void prepareCreate() {
    form = new RentalRequest();
    form.status = "RESERVEE";
    passengerId = null;
    vehicleId = null;
  }

  public void save() {
    try {
      form.passengerId = toUuid(passengerId);
      form.vehicleId = toUuid(vehicleId);
      api.createRental(form);
      messages.info("Location creee");
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void changeStatus(RentalDto r, String status) {
    try {
      api.updateRentalStatus(r.identifier, Map.of("status", status));
      messages.info("Location " + r.plateNumber + " : " + status);
      refresh();
    } catch (Exception e) {
      messages.error("Changement de statut impossible", e);
    }
  }

  public void delete(RentalDto r) {
    try {
      api.deleteRental(r.identifier);
      messages.info("Location supprimee");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  private static UUID toUuid(String value) {
    return value == null || value.isBlank() ? null : UUID.fromString(value);
  }

  public List<RentalDto> getRentals() {
    return rentals;
  }

  public List<PassengerDto> getPassengers() {
    return passengers;
  }

  public List<VehicleDto> getVehicles() {
    return vehicles;
  }

  public RentalRequest getForm() {
    return form;
  }

  public String getPassengerId() {
    return passengerId;
  }

  public void setPassengerId(String passengerId) {
    this.passengerId = passengerId;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public List<String> getStatuses() {
    return STATUSES;
  }

  @Override
  protected List<RentalDto> items() {
    return rentals;
  }

  @Override
  protected String searchText(RentalDto i) {
    return i.passengerFullName + " " + i.plateNumber;
  }

  @Override
  protected String statusOf(RentalDto i) {
    return i.status;
  }
}
