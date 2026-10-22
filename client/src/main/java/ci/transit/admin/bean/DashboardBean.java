package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.DashboardDto;
import ci.transit.admin.dto.VehicleDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

  /** Statut -> libelle et couleur, dans l'ordre d'affichage du graphique. */
  private static final Map<String, String[]> FLEET_STATUSES = new LinkedHashMap<>();

  static {
    FLEET_STATUSES.put("EN_SERVICE", new String[] {"En service", "#4f46e5"});
    FLEET_STATUSES.put("DISPONIBLE", new String[] {"Disponible", "#10b981"});
    FLEET_STATUSES.put("ENTRETIEN", new String[] {"En entretien", "#f59e0b"});
    FLEET_STATUSES.put("HORS_SERVICE", new String[] {"Hors service", "#e11d48"});
  }

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private DashboardDto dashboard = new DashboardDto();
  private List<VehicleDto> vehicles = new ArrayList<>();

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    try {
      dashboard = api.dashboard(30);
      vehicles = new ArrayList<>(api.vehicles(null));
    } catch (Exception e) {
      messages.error("Impossible de charger le tableau de bord", e);
    }
  }

  public DashboardDto getDashboard() {
    return dashboard;
  }

  /** Part du parc actuellement en service, en pourcentage (0 a 100). */
  public int getServiceRate() {
    long total = dashboard.fleetAlerts.vehiclesTotal;
    return total == 0 ? 0 : (int) Math.round(100.0 * dashboard.fleetAlerts.vehiclesInService / total);
  }

  /** Nombre total d'elements demandant une action (documents, permis, entretiens, abonnements). */
  public int getAlertCount() {
    return dashboard.fleetAlerts.expiringDocuments.size() + dashboard.fleetAlerts.expiringLicenses.size()
        + dashboard.fleetAlerts.upcomingMaintenances.size() + dashboard.expiringSubscriptions.size();
  }

  /** Graphique en anneau (Chart.js) de la repartition du parc par statut. */
  public String getFleetChart() {
    Map<String, Long> counts = vehicles.stream()
        .collect(Collectors.groupingBy(v -> v.status == null ? "" : v.status, Collectors.counting()));
    List<String> labels = new ArrayList<>();
    List<String> data = new ArrayList<>();
    List<String> colors = new ArrayList<>();
    FLEET_STATUSES.forEach((status, meta) -> {
      long n = counts.getOrDefault(status, 0L);
      if (n > 0) {
        labels.add("\"" + meta[0] + "\"");
        data.add(String.valueOf(n));
        colors.add("\"" + meta[1] + "\"");
      }
    });
    if (labels.isEmpty()) {
      labels.add("\"Aucun car\"");
      data.add("1");
      colors.add("\"#e4e8f0\"");
    }
    return "{\"type\":\"doughnut\",\"data\":{\"labels\":[" + String.join(",", labels)
        + "],\"datasets\":[{\"data\":[" + String.join(",", data) + "],\"backgroundColor\":["
        + String.join(",", colors) + "],\"borderWidth\":3,\"borderColor\":\"#ffffff\",\"hoverOffset\":6}]},"
        + "\"options\":{\"maintainAspectRatio\":false,\"cutout\":\"72%\","
        + "\"plugins\":{\"legend\":{\"position\":\"bottom\",\"labels\":{\"usePointStyle\":true,\"padding\":16}}}}}";
  }
}
