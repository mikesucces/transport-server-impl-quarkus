package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.PassengerDto;
import ci.transit.admin.dto.SubscriptionDto;
import ci.transit.admin.dto.SubscriptionRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SubscriptionBean extends FilterableList<SubscriptionDto> {

  public static final List<String> PLANS = List.of("HEBDOMADAIRE", "MENSUEL", "TRIMESTRIEL");
  public static final List<String> METHODS = List.of("ESPECES", "MOBILE_MONEY", "VIREMENT", "AUTRE");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<SubscriptionDto> subscriptions = new ArrayList<>();
  private List<PassengerDto> passengers = new ArrayList<>();
  private SubscriptionRequest form = new SubscriptionRequest();
  /** Identifiant d'usager saisi dans le formulaire (String : pas de convertisseur UUID en JSF). */
  private String passengerId;

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      subscriptions = new ArrayList<>(api.subscriptions(null, null));
      passengers = new ArrayList<>(api.passengers("ACTIVE"));
    } catch (Exception e) {
      messages.error("Impossible de charger les abonnements", e);
    }
  }

  public void prepareCreate() {
    form = new SubscriptionRequest();
    form.plan = "MENSUEL";
    form.method = "ESPECES";
    passengerId = null;
  }

  public void save() {
    try {
      form.passengerId = passengerId == null || passengerId.isBlank() ? null : UUID.fromString(passengerId);
      api.createSubscription(form);
      messages.info("Abonnement cree");
      refresh();
    } catch (Exception e) {
      messages.error("Enregistrement impossible", e);
      FacesContext.getCurrentInstance().validationFailed();
    }
  }

  public void cancel(SubscriptionDto s) {
    try {
      api.updateSubscriptionStatus(s.identifier, Map.of("status", "CANCELLED"));
      messages.info("Abonnement de " + s.passengerFullName + " annule");
      refresh();
    } catch (Exception e) {
      messages.error("Annulation impossible", e);
    }
  }

  public void delete(SubscriptionDto s) {
    try {
      api.deleteSubscription(s.identifier);
      messages.info("Abonnement supprime");
      refresh();
    } catch (Exception e) {
      messages.error("Suppression impossible", e);
    }
  }

  public List<SubscriptionDto> getSubscriptions() {
    return subscriptions;
  }

  public List<PassengerDto> getPassengers() {
    return passengers;
  }

  public SubscriptionRequest getForm() {
    return form;
  }

  public String getPassengerId() {
    return passengerId;
  }

  public void setPassengerId(String passengerId) {
    this.passengerId = passengerId;
  }

  public List<String> getPlans() {
    return PLANS;
  }

  public List<String> getMethods() {
    return METHODS;
  }

  @Override
  protected List<SubscriptionDto> items() {
    return subscriptions;
  }

  @Override
  protected String searchText(SubscriptionDto i) {
    return i.passengerFullName + " " + i.plan;
  }

  @Override
  protected String statusOf(SubscriptionDto i) {
    return i.status;
  }
}
