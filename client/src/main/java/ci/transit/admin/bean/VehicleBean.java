package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.VehicleDto;
import ci.transit.admin.dto.VehicleRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class VehicleBean extends FilterableList<VehicleDto> {

  public static final List<String> STATUSES =
      List.of("DISPONIBLE", "EN_SERVICE", "ENTRETIEN", "HORS_SERVICE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<VehicleDto> vehicles = new ArrayList<>();
  private VehicleDto selected;
  private VehicleRequest form = new VehicleRequest();

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      vehicles = new ArrayList<>(api.vehicles(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les cars", e);
    }
  }

  public void prepareCreate() {
    selected = null;
    form = new VehicleRequest();
    form.status = "DISPONIBLE";
  }

  public void prepareEdit(VehicleDto v) {
    selected = v;
    form = new VehicleRequest();
    form.plateNumber = v.plateNumber;
    form.brand = v.brand;
    form.model = v.model;
    form.year = v.year;
    form.capacity = v.capacity;
    form.mileageKm = v.mileageKm;
    form.status = v.status;
    form.commissionedAt = v.commissionedAt;
    form.notes = v.notes;
  }

  public void save() {
    try {
      if (selected == null) {
        api.createVehicle(form);
        messages.info("Car " + form.plateNumber + " cree");
      } else {
        api.updateVehicle(selected.identifier, form);
        messages.info("Car " + form.plateNumber + " mis a jour");
      }
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void changeStatus(VehicleDto v, String status) {
    try {
      api.updateVehicleStatus(v.identifier, Map.of("status", status));
      messages.info(v.plateNumber + " : " + status);
      refresh();
    } catch (Exception e) {
      messages.error("Changement de statut impossible", e);
    }
  }

  public void delete(VehicleDto v) {
    try {
      api.deleteVehicle(v.identifier);
      messages.info("Car " + v.plateNumber + " supprime");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public String severity(String status) {
    return switch (status == null ? "" : status) {
      case "DISPONIBLE" -> "success";
      case "EN_SERVICE" -> "info";
      case "ENTRETIEN" -> "warn";
      case "HORS_SERVICE" -> "danger";
      default -> "secondary";
    };
  }

  public List<VehicleDto> getVehicles() {
    return vehicles;
  }

  public VehicleRequest getForm() {
    return form;
  }

  public boolean isEditing() {
    return selected != null;
  }

  public List<String> getStatuses() {
    return STATUSES;
  }

  @Override
  protected List<VehicleDto> items() {
    return vehicles;
  }

  @Override
  protected String searchText(VehicleDto i) {
    return i.plateNumber + " " + i.brand + " " + i.model;
  }

  @Override
  protected String statusOf(VehicleDto i) {
    return i.status;
  }
}
