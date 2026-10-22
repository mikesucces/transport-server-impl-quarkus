package ci.transit.admin.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Base des ecrans de liste : recherche texte et filtre de statut appliques cote serveur.
 */
public abstract class FilterableList<T> implements Serializable {

  private String search = "";
  private String statusFilter = "";

  /** Elements charges depuis l'API. */
  protected abstract List<T> items();

  /** Texte sur lequel porte la recherche (plusieurs champs concatenes). */
  protected abstract String searchText(T item);

  /** Statut de l'element, ou {@code null} si l'ecran n'a pas de filtre de statut. */
  protected abstract String statusOf(T item);

  public List<T> getFiltered() {
    String query = search == null ? "" : search.trim().toLowerCase(Locale.FRENCH);
    return items().stream()
        .filter(i -> statusFilter == null || statusFilter.isBlank() || statusFilter.equals(statusOf(i)))
        .filter(i -> query.isEmpty() || searchText(i).toLowerCase(Locale.FRENCH).contains(query))
        .toList();
  }

  public int getTotal() {
    return items().size();
  }

  public int getFilteredCount() {
    return getFiltered().size();
  }

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  public String getStatusFilter() {
    return statusFilter;
  }

  public void setStatusFilter(String statusFilter) {
    this.statusFilter = statusFilter;
  }
}
