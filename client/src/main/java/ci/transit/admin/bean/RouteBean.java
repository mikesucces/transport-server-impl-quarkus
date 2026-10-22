package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.RouteDto;
import ci.transit.admin.dto.RouteRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RouteBean extends FilterableList<RouteDto> {

  public static final List<String> STATUSES = List.of("ACTIVE", "SUSPENDUE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<RouteDto> routes = new ArrayList<>();
  private RouteDto selected;
  private RouteRequest form = new RouteRequest();

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      routes = new ArrayList<>(api.routes(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les lignes", e);
    }
  }

  public void prepareCreate() {
    selected = null;
    form = new RouteRequest();
    form.status = "ACTIVE";
  }

  public void prepareEdit(RouteDto r) {
    selected = r;
    form = new RouteRequest();
    form.code = r.code;
    form.name = r.name;
    form.origin = r.origin;
    form.destination = r.destination;
    form.distanceKm = r.distanceKm;
    form.status = r.status;
  }

  public void save() {
    try {
      if (selected == null) {
        api.createRoute(form);
        messages.info("Ligne " + form.code + " creee");
      } else {
        api.updateRoute(selected.identifier, form);
        messages.info("Ligne " + form.code + " mise a jour");
      }
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void toggleStatus(RouteDto r) {
    String next = "ACTIVE".equals(r.status) ? "SUSPENDUE" : "ACTIVE";
    try {
      api.updateRouteStatus(r.identifier, Map.of("status", next));
      messages.info("Ligne " + r.code + " : " + next);
      refresh();
    } catch (Exception e) {
      messages.error("Changement de statut impossible", e);
    }
  }

  public void delete(RouteDto r) {
    try {
      api.deleteRoute(r.identifier);
      messages.info("Ligne " + r.code + " supprimee");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public List<RouteDto> getRoutes() {
    return routes;
  }

  public RouteRequest getForm() {
    return form;
  }

  public boolean isEditing() {
    return selected != null;
  }

  public List<String> getStatuses() {
    return STATUSES;
  }

  @Override
  protected List<RouteDto> items() {
    return routes;
  }

  @Override
  protected String searchText(RouteDto i) {
    return i.code + " " + i.name + " " + i.origin + " " + i.destination;
  }

  @Override
  protected String statusOf(RouteDto i) {
    return i.status;
  }
}
