package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.MaintenanceDto;
import ci.transit.admin.dto.MaintenanceRequest;
import ci.transit.admin.dto.VehicleDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class MaintenanceBean implements Serializable {

  public static final List<String> TYPES = List.of("VIDANGE", "REPARATION", "CONTROLE", "PNEUMATIQUES", "AUTRE");
  public static final List<String> STATUSES = List.of("PLANIFIE", "EN_COURS", "TERMINE", "ANNULE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<VehicleDto> vehicles = new ArrayList<>();
  private List<MaintenanceDto> maintenances = new ArrayList<>();
  private String vehicleId;
  private MaintenanceRequest form = new MaintenanceRequest();

  @PostConstruct
  public void init() {
    try {
      vehicles = new ArrayList<>(api.vehicles(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les cars", e);
    }
  }

  public void load() {
    maintenances = new ArrayList<>();
    if (!isVehicleSelected()) {
      return;
    }
    try {
      maintenances = new ArrayList<>(api.vehicleMaintenances(UUID.fromString(vehicleId)));
    } catch (Exception e) {
      messages.error("Impossible de charger les entretiens", e);
    }
  }

  public void prepareCreate() {
    form = new MaintenanceRequest();
    form.maintenanceType = "VIDANGE";
    form.status = "PLANIFIE";
  }

  public void save() {
    try {
      api.createMaintenance(UUID.fromString(vehicleId), form);
      messages.info("Entretien ajoute");
      load();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void delete(MaintenanceDto m) {
    try {
      api.deleteMaintenance(m.identifier);
      messages.info("Entretien supprime");
      load();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public boolean isVehicleSelected() {
    return vehicleId != null && !vehicleId.isBlank();
  }

  public List<VehicleDto> getVehicles() {
    return vehicles;
  }

  public List<MaintenanceDto> getMaintenances() {
    return maintenances;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public MaintenanceRequest getForm() {
    return form;
  }

  public List<String> getTypes() {
    return TYPES;
  }

  public List<String> getStatuses() {
    return STATUSES;
  }
}
