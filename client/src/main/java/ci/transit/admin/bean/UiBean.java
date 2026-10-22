package ci.transit.admin.bean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Aides d'affichage partagees par les pages : libelles francais des codes metier,
 * couleur des statuts, formats de dates, montants et echeances.
 */
@Named("ui")
@ApplicationScoped
public class UiBean {

  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter HOUR =
      DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

  private static final DecimalFormat NUMBER = new DecimalFormat("#,##0", french());

  private static DecimalFormatSymbols french() {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
    symbols.setGroupingSeparator(' ');
    return symbols;
  }

  private static final Map<String, String> LABELS = Map.ofEntries(
      // vehicules
      Map.entry("DISPONIBLE", "Disponible"), Map.entry("EN_SERVICE", "En service"),
      Map.entry("ENTRETIEN", "En entretien"), Map.entry("HORS_SERVICE", "Hors service"),
      // chauffeurs
      Map.entry("EN_ROTATION", "En rotation"), Map.entry("REPOS", "Au repos"),
      Map.entry("SUSPENDU", "Suspendu"),
      // remuneration
      Map.entry("FIXE", "Fixe"), Map.entry("PAR_TRAJET", "Par trajet"),
      Map.entry("COMMISSION", "Commission"),
      // rotations / locations
      Map.entry("PLANIFIEE", "Planifiée"), Map.entry("EN_COURS", "En cours"),
      Map.entry("TERMINEE", "Terminée"), Map.entry("ANNULEE", "Annulée"),
      Map.entry("RESERVEE", "Réservée"),
      // usagers / abonnements / lignes
      Map.entry("ACTIVE", "Actif"), Map.entry("SUSPENDED", "Suspendu"),
      Map.entry("DISABLED", "Désactivé"), Map.entry("EXPIRED", "Expiré"),
      Map.entry("CANCELLED", "Annulé"), Map.entry("SUSPENDUE", "Suspendue"),
      // entretiens
      Map.entry("PLANIFIE", "Planifié"), Map.entry("TERMINE", "Terminé"),
      Map.entry("ANNULE", "Annulé"),
      Map.entry("VIDANGE", "Vidange"), Map.entry("REPARATION", "Réparation"),
      Map.entry("CONTROLE", "Contrôle"), Map.entry("PNEUMATIQUES", "Pneumatiques"),
      // documents
      Map.entry("ASSURANCE", "Assurance"), Map.entry("VISITE_TECHNIQUE", "Visite technique"),
      Map.entry("CARTE_GRISE", "Carte grise"), Map.entry("LICENCE_TRANSPORT", "Licence de transport"),
      Map.entry("AUTRE", "Autre"),
      // formules et paiements
      Map.entry("HEBDOMADAIRE", "Hebdomadaire"), Map.entry("MENSUEL", "Mensuel"),
      Map.entry("TRIMESTRIEL", "Trimestriel"),
      Map.entry("ESPECES", "Espèces"), Map.entry("MOBILE_MONEY", "Mobile money"),
      Map.entry("VIREMENT", "Virement"),
      // jours
      Map.entry("LUNDI", "Lundi"), Map.entry("MARDI", "Mardi"), Map.entry("MERCREDI", "Mercredi"),
      Map.entry("JEUDI", "Jeudi"), Map.entry("VENDREDI", "Vendredi"), Map.entry("SAMEDI", "Samedi"),
      Map.entry("DIMANCHE", "Dimanche"),
      // roles
      Map.entry("OWNER", "Propriétaire"), Map.entry("MANAGER", "Gestionnaire"),
      Map.entry("CONTROLLER", "Contrôleur"), Map.entry("DRIVER", "Chauffeur"));

  /** Libelle francais d'un code metier ({@code EN_SERVICE} devient « En service »). */
  public String label(String code) {
    if (code == null || code.isBlank()) {
      return "";
    }
    String label = LABELS.get(code);
    if (label != null) {
      return label;
    }
    String text = code.toLowerCase(Locale.FRENCH).replace('_', ' ');
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  /** Variante feminine (une ligne « Active »). */
  public String labelF(String code) {
    return "ACTIVE".equals(code) ? "Active" : label(code);
  }

  /** Couleur semantique d'un statut : success, info, warn, danger ou secondary. */
  public String severity(String status) {
    return switch (status == null ? "" : status) {
      case "DISPONIBLE", "ACTIVE", "TERMINEE", "TERMINE" -> "success";
      case "EN_SERVICE", "EN_ROTATION", "EN_COURS", "PLANIFIEE", "PLANIFIE", "RESERVEE" -> "info";
      case "ENTRETIEN", "REPOS", "SUSPENDUE", "SUSPENDED", "EXPIRED" -> "warning";
      case "HORS_SERVICE", "SUSPENDU", "DISABLED", "CANCELLED", "ANNULEE", "ANNULE" -> "danger";
      default -> "secondary";
    };
  }

  public String date(LocalDate date) {
    return date == null ? "—" : DATE.format(date);
  }

  public String time(LocalTime time) {
    return time == null ? "—" : TIME.format(time);
  }

  public String dateTime(Instant instant) {
    return instant == null ? "—" : DATE_TIME.format(instant);
  }

  public String hour(Instant instant) {
    return instant == null ? "—" : HOUR.format(instant);
  }

  /** Nombre avec separateur de milliers (ex. 201 700). */
  public String number(Number value) {
    return value == null ? "—" : NUMBER.format(value);
  }

  /** Montant en francs CFA (ex. 73 123 FCFA). */
  public String money(BigDecimal amount) {
    return amount == null ? "—" : NUMBER.format(amount) + " FCFA";
  }

  /** Echeance en clair : « Dans 12 j », « Aujourd'hui », « Expiré depuis 5 j ». */
  public String daysText(long days) {
    if (days > 1) {
      return "Dans " + days + " j";
    }
    if (days == 1) {
      return "Demain";
    }
    if (days == 0) {
      return "Aujourd'hui";
    }
    return "Expiré depuis " + (-days) + " j";
  }

  /** Urgence d'une echeance : danger (depassee ou imminente), warn, puis success. */
  public String daysSeverity(long days) {
    if (days <= 7) {
      return "danger";
    }
    return days <= 30 ? "warning" : "success";
  }

  /** Initiales pour les pastilles d'avatar (« Aya Kouassi » donne « AK »). */
  public String initials(String name) {
    if (name == null || name.isBlank()) {
      return "?";
    }
    String[] parts = name.trim().split("\\s+");
    String first = parts[0].substring(0, 1);
    String second = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
    return (first + second).toUpperCase(Locale.FRENCH);
  }
}
