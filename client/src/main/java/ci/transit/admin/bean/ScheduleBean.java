package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.RouteDto;
import ci.transit.admin.dto.RouteScheduleDto;
import ci.transit.admin.dto.RouteScheduleRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ScheduleBean implements Serializable {

  public static final List<String> DAYS =
      List.of("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI", "DIMANCHE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<RouteDto> routes = new ArrayList<>();
  private List<RouteScheduleDto> schedules = new ArrayList<>();
  private String routeId;
  private RouteScheduleRequest form = new RouteScheduleRequest();

  @PostConstruct
  public void init() {
    try {
      routes = new ArrayList<>(api.routes(null));
    } catch (Exception e) {
      messages.error("Impossible de charger les lignes", e);
    }
  }

  public void load() {
    schedules = new ArrayList<>();
    if (!isRouteSelected()) {
      return;
    }
    try {
      schedules = new ArrayList<>(api.routeSchedules(UUID.fromString(routeId)));
    } catch (Exception e) {
      messages.error("Impossible de charger les horaires", e);
    }
  }

  public void prepareCreate() {
    form = new RouteScheduleRequest();
    form.dayOfWeek = "LUNDI";
  }

  public void save() {
    try {
      api.createSchedule(UUID.fromString(routeId), form);
      messages.info("Horaire ajoute");
      load();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void delete(RouteScheduleDto s) {
    try {
      api.deleteSchedule(s.identifier);
      messages.info("Horaire supprime");
      load();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public boolean isRouteSelected() {
    return routeId != null && !routeId.isBlank();
  }

  public List<RouteDto> getRoutes() {
    return routes;
  }

  public List<RouteScheduleDto> getSchedules() {
    return schedules;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public RouteScheduleRequest getForm() {
    return form;
  }

  public List<String> getDays() {
    return DAYS;
  }
}
