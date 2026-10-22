package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.DriverDto;
import ci.transit.admin.dto.DriverRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DriverBean extends FilterableList<DriverDto> {

  public static final List<String> STATUSES = List.of("DISPONIBLE", "EN_ROTATION", "REPOS", "SUSPENDU");
  public static final List<String> REMUNERATIONS = List.of("FIXE", "PAR_TRAJET", "COMMISSION");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<DriverDto> drivers = new ArrayList<>();
  private DriverDto selected;
  private DriverRequest form = new DriverRequest();

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      drivers = new ArrayList<>(api.drivers(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les chauffeurs", e);
    }
  }

  public void prepareCreate() {
    selected = null;
    form = new DriverRequest();
    form.status = "DISPONIBLE";
    form.remunerationType = "FIXE";
  }

  public void prepareEdit(DriverDto d) {
    selected = d;
    form = new DriverRequest();
    form.fullName = d.fullName;
    form.phone = d.phone;
    form.matricule = d.matricule;
    form.licenseNumber = d.licenseNumber;
    form.licenseCategory = d.licenseCategory;
    form.licenseExpiresOn = d.licenseExpiresOn;
    form.remunerationType = d.remunerationType;
    form.monthlySalary = d.monthlySalary;
    form.tripRate = d.tripRate;
    form.commissionRate = d.commissionRate;
    form.status = d.status;
    form.hiredOn = d.hiredOn;
  }

  public void save() {
    try {
      if (selected == null) {
        api.createDriver(form);
        messages.info("Chauffeur " + form.fullName + " cree");
      } else {
        api.updateDriver(selected.identifier, form);
        messages.info("Chauffeur " + form.fullName + " mis a jour");
      }
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void changeStatus(DriverDto d, String status) {
    try {
      api.updateDriverStatus(d.identifier, Map.of("status", status));
      messages.info(d.fullName + " : " + status);
      refresh();
    } catch (Exception e) {
      messages.error("Changement de statut impossible", e);
    }
  }

  public void delete(DriverDto d) {
    try {
      api.deleteDriver(d.identifier);
      messages.info("Chauffeur " + d.fullName + " supprime");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public List<DriverDto> getDrivers() {
    return drivers;
  }

  public DriverRequest getForm() {
    return form;
  }

  public boolean isEditing() {
    return selected != null;
  }

  public List<String> getStatuses() {
    return STATUSES;
  }

  public List<String> getRemunerations() {
    return REMUNERATIONS;
  }

  @Override
  protected List<DriverDto> items() {
    return drivers;
  }

  @Override
  protected String searchText(DriverDto i) {
    return i.fullName + " " + i.phone + " " + i.licenseNumber + " " + i.matricule;
  }

  @Override
  protected String statusOf(DriverDto i) {
    return i.status;
  }
}
