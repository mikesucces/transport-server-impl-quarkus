package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.PassengerDto;
import ci.transit.admin.dto.PassengerRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class PassengerBean extends FilterableList<PassengerDto> {

  public static final List<String> STATUSES = List.of("ACTIVE", "SUSPENDED", "DISABLED");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<PassengerDto> passengers = new ArrayList<>();
  private PassengerDto selected;
  private PassengerRequest form = new PassengerRequest();

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      passengers = new ArrayList<>(api.passengers(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les usagers", e);
    }
  }

  public void prepareCreate() {
    selected = null;
    form = new PassengerRequest();
    form.status = "ACTIVE";
  }

  public void prepareEdit(PassengerDto p) {
    selected = p;
    form = new PassengerRequest();
    form.fullName = p.fullName;
    form.phone = p.phone;
    form.status = p.status;
  }

  public void save() {
    try {
      if (selected == null) {
        api.createPassenger(form);
        messages.info("Usager " + form.fullName + " cree");
      } else {
        api.updatePassenger(selected.identifier, form);
        messages.info("Usager " + form.fullName + " mis a jour");
      }
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void changeStatus(PassengerDto p, String status) {
    try {
      api.updatePassengerStatus(p.identifier, Map.of("status", status));
      messages.info(p.fullName + " : " + status);
      refresh();
    } catch (Exception e) {
      messages.error("Changement de statut impossible", e);
    }
  }

  public void delete(PassengerDto p) {
    try {
      api.deletePassenger(p.identifier);
      messages.info("Usager " + p.fullName + " supprime");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public List<PassengerDto> getPassengers() {
    return passengers;
  }

  public PassengerRequest getForm() {
    return form;
  }

  public boolean isEditing() {
    return selected != null;
  }

  public List<String> getStatuses() {
    return STATUSES;
  }

  @Override
  protected List<PassengerDto> items() {
    return passengers;
  }

  @Override
  protected String searchText(PassengerDto i) {
    return i.fullName + " " + i.phone;
  }

  @Override
  protected String statusOf(PassengerDto i) {
    return i.status;
  }
}
