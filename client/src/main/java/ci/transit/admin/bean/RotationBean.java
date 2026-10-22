package ci.transit.admin.bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.DriverDto;
import ci.transit.admin.dto.RotationDto;
import ci.transit.admin.dto.RotationRequest;
import ci.transit.admin.dto.RouteDto;
import ci.transit.admin.dto.VehicleDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RotationBean extends FilterableList<RotationDto> {

  public static final List<String> STATUSES = List.of("PLANIFIEE", "EN_COURS", "TERMINEE", "ANNULEE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<RotationDto> rotations = new ArrayList<>();
  private List<DriverDto> drivers = new ArrayList<>();
  private List<VehicleDto> vehicles = new ArrayList<>();
  private List<RouteDto> routes = new ArrayList<>();

  private RotationRequest form = new RotationRequest();
  // Champs du formulaire : String pour les identifiants (pas de convertisseur UUID en JSF),
  // LocalDateTime pour les dates (converties en Instant a l'enregistrement).
  private String driverId;
  private String vehicleId;
  private String routeId;
  private LocalDateTime start;
  private LocalDateTime end;

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      rotations = new ArrayList<>(api.rotations(null, null, null));
      drivers = new ArrayList<>(api.drivers(null));
      vehicles = new ArrayList<>(api.vehicles(null));
      routes = new ArrayList<>(api.routes("ACTIVE"));
    } catch (Exception e) {
      messages.error("Impossible de charger les rotations", e);
    }
  }

  public void prepareCreate() {
    form = new RotationRequest();
    form.status = "PLANIFIEE";
    driverId = null;
    vehicleId = null;
    routeId = null;
    start = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
    end = null;
  }

  public void save() {
    try {
      form.driverId = toUuid(driverId);
      form.vehicleId = toUuid(vehicleId);
      form.routeId = toUuid(routeId);
      form.scheduledStart = start == null ? null : start.atZone(ZoneId.systemDefault()).toInstant();
      form.scheduledEnd = end == null ? null : end.atZone(ZoneId.systemDefault()).toInstant();
      api.createRotation(form);
      messages.info("Rotation creee");
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void changeStatus(RotationDto r, String status) {
    try {
      api.updateRotationStatus(r.identifier, Map.of("status", status));
      messages.info("Rotation " + r.plateNumber + " : " + status);
      refresh();
    } catch (Exception e) {
      messages.error("Changement de statut impossible", e);
    }
  }

  public void delete(RotationDto r) {
    try {
      api.deleteRotation(r.identifier);
      messages.info("Rotation supprimee");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  private static UUID toUuid(String value) {
    return value == null || value.isBlank() ? null : UUID.fromString(value);
  }


  public List<RotationDto> getRotations() {
    return rotations;
  }

  public List<DriverDto> getDrivers() {
    return drivers;
  }

  public List<VehicleDto> getVehicles() {
    return vehicles;
  }

  public List<RouteDto> getRoutes() {
    return routes;
  }

  public RotationRequest getForm() {
    return form;
  }

  public String getDriverId() {
    return driverId;
  }

  public void setDriverId(String driverId) {
    this.driverId = driverId;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public void setEnd(LocalDateTime end) {
    this.end = end;
  }


  public List<String> getStatuses() {
    return STATUSES;
  }

  @Override
  protected List<RotationDto> items() {
    return rotations;
  }

  @Override
  protected String searchText(RotationDto i) {
    return i.routeCode + " " + i.routeName + " " + i.driverFullName + " " + i.plateNumber;
  }

  @Override
  protected String statusOf(RotationDto i) {
    return i.status;
  }
}
