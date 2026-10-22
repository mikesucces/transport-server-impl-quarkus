package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.DocumentDto;
import ci.transit.admin.dto.DocumentRequest;
import ci.transit.admin.dto.VehicleDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DocumentBean implements Serializable {

  public static final List<String> TYPES =
      List.of("ASSURANCE", "VISITE_TECHNIQUE", "CARTE_GRISE", "LICENCE_TRANSPORT", "AUTRE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<VehicleDto> vehicles = new ArrayList<>();
  private List<DocumentDto> documents = new ArrayList<>();
  // String : pas de convertisseur UUID en JSF
  private String vehicleId;
  private DocumentRequest form = new DocumentRequest();

  @PostConstruct
  public void init() {
    try {
      vehicles = new ArrayList<>(api.vehicles(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les cars", e);
    }
  }

  public void load() {
    documents = new ArrayList<>();
    if (vehicleId == null || vehicleId.isBlank()) {
      return;
    }
    try {
      documents = new ArrayList<>(api.vehicleDocuments(UUID.fromString(vehicleId)));
    } catch (Exception e) {
      messages.error("Impossible de charger les documents", e);
    }
  }

  public void prepareCreate() {
    form = new DocumentRequest();
    form.docType = "ASSURANCE";
  }

  public void save() {
    try {
      api.createDocument(UUID.fromString(vehicleId), form);
      messages.info("Document ajoute");
      load();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void delete(DocumentDto d) {
    try {
      api.deleteDocument(d.identifier);
      messages.info("Document supprime");
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

  public List<DocumentDto> getDocuments() {
    return documents;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public DocumentRequest getForm() {
    return form;
  }

  public List<String> getTypes() {
    return TYPES;
  }
}
