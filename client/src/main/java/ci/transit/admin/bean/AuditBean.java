package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ci.transit.admin.client.TransportApi;
import ci.transit.admin.dto.AuditEntryDto;
import ci.transit.admin.dto.AuditPageDto;
import ci.transit.admin.dto.AuditTableDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** Journal d'audit : historique des creations, modifications et suppressions, table par table. */
@Named
@ViewScoped
public class AuditBean implements Serializable {

  private static final int PAGE_SIZE = 200;

  /** Libelle des tables auditees (le code est le nom de la table en base). */
  private static final Map<String, String> TABLE_LABELS = Map.ofEntries(
      Map.entry("vehicles", "Cars"),
      Map.entry("vehicle_documents", "Documents des cars"),
      Map.entry("maintenances", "Entretiens"),
      Map.entry("drivers", "Chauffeurs"),
      Map.entry("passengers", "Usagers"),
      Map.entry("subscriptions", "Abonnements"),
      Map.entry("payments", "Paiements d'abonnement"),
      Map.entry("payment_accounts", "Moyens de paiement"),
      Map.entry("rentals", "Locations"),
      Map.entry("rental_payments", "Paiements de location"),
      Map.entry("routes", "Lignes"),
      Map.entry("route_schedules", "Horaires"),
      Map.entry("rotations", "Rotations"),
      Map.entry("attendances", "Embarquements"),
      Map.entry("access_codes", "Codes d'accès"),
      Map.entry("staff_profiles", "Personnel"),
      Map.entry("login_attempts", "Tentatives de connexion"));

  /** Champs les plus parlants pour resumer une ligne, par ordre de preference. */
  private static final List<String> SUMMARY_KEYS = List.of(
      "plate_number", "full_name", "name", "label", "reference", "phone", "vehicle_number", "status");

  @Inject
  @RestClient
  transient TransportApi api;

  @Inject
  transient Messages messages;

  private List<AuditTableDto> tables = new ArrayList<>();
  private String table = "vehicles";
  private String username = "";
  private String entityId = "";
  private AuditPageDto page = new AuditPageDto();
  private AuditEntryDto selected;

  @PostConstruct
  public void init() {
    try {
      tables = new ArrayList<>(api.auditTables());
    } catch (Exception e) {
      messages.error("Impossible de charger le journal d'audit", e);
      return;
    }
    search();
  }

  public void search() {
    try {
      page = api.audit(table, blankToNull(username), blankToNull(entityId), 0, PAGE_SIZE);
    } catch (Exception e) {
      page = new AuditPageDto();
      messages.error("Impossible de charger l'historique", e);
    }
  }

  public void reset() {
    username = "";
    entityId = "";
    search();
  }

  public void select(AuditEntryDto entry) {
    selected = entry;
  }

  /** Voir tout l'historique d'un enregistrement a partir d'une ligne. */
  public void followEntity(AuditEntryDto entry) {
    entityId = entry.getEntityId();
    search();
  }

  public String tableLabel(String code) {
    return TABLE_LABELS.getOrDefault(code, code);
  }

  public String operationLabel(String operation) {
    return switch (operation == null ? "" : operation) {
      case "CREATION" -> "Création";
      case "MODIFICATION" -> "Modification";
      case "SUPPRESSION" -> "Suppression";
      default -> "—";
    };
  }

  public String operationSeverity(String operation) {
    return switch (operation == null ? "" : operation) {
      case "CREATION" -> "success";
      case "MODIFICATION" -> "info";
      case "SUPPRESSION" -> "danger";
      default -> "secondary";
    };
  }

  /** Quelques valeurs de la ligne pour la reconnaitre dans la liste. */
  public String summary(AuditEntryDto entry) {
    if (entry.getData() == null) {
      return "";
    }
    List<String> parts = new ArrayList<>();
    for (String key : SUMMARY_KEYS) {
      Object value = entry.getData().get(key);
      if (value != null && !String.valueOf(value).isBlank()) {
        parts.add(String.valueOf(value));
      }
      if (parts.size() == 2) {
        break;
      }
    }
    return String.join(" · ", parts);
  }

  public String shortId(String id) {
    return id != null && id.length() > 8 ? id.substring(0, 8) + "…" : id;
  }

  /** Champs de la ligne selectionnee, pour la fenetre de detail. */
  public List<Map.Entry<String, String>> getSelectedFields() {
    List<Map.Entry<String, String>> fields = new ArrayList<>();
    if (selected != null && selected.getData() != null) {
      selected.getData().forEach((k, v) -> fields.add(Map.entry(k, v == null ? "—" : String.valueOf(v))));
    }
    return fields;
  }

  public List<String> getTableCodes() {
    return tables.stream().map(AuditTableDto::getTable).toList();
  }

  public long countOf(String code) {
    return tables.stream().filter(t -> t.getTable().equals(code)).mapToLong(AuditTableDto::getCount).findFirst().orElse(0);
  }

  public long getTotalRows() {
    return tables.stream().mapToLong(AuditTableDto::getCount).sum();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  public List<AuditEntryDto> getEntries() {
    return page.getItems();
  }

  public long getTotal() {
    return page.getTotal();
  }

  public int getShown() {
    return page.getItems().size();
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public AuditEntryDto getSelected() {
    return selected;
  }
}
