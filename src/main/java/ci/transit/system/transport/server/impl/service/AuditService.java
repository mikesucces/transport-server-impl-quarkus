package ci.transit.system.transport.server.impl.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import ci.transit.system.transport.server.impl.dto.AuditEntryDto;
import ci.transit.system.transport.server.impl.dto.AuditPageDto;
import ci.transit.system.transport.server.impl.dto.AuditTableDto;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

/**
 * Consultation de l'historique d'audit (tables xxx_aud alimentees par Hibernate Envers).
 * Le nom de table venant de l'appelant n'est jamais concatene tel quel : il doit figurer dans la liste blanche.
 *
 * @author Transit
 *
 */
@ApplicationScoped
public class AuditService {

  private static final int MAX_PAGE_SIZE = 200;
  private static final String[] OPERATIONS = {"CREATION", "MODIFICATION", "SUPPRESSION"};
  /** Colonnes techniques de la table d'audit, retirees des donnees renvoyees. */
  private static final Set<String> TECHNICAL = Set.of("rev", "revtype", "audit_ts", "audit_user");

  private static final List<String> TABLES = List.of(
      "access_codes", "attendances", "drivers", "login_attempts", "maintenances", "passengers",
      "payment_accounts", "payments", "rental_payments", "rentals", "rotations", "route_schedules",
      "routes", "staff_profiles", "subscriptions", "vehicle_documents", "vehicles");

  @Inject
  EntityManager entityManager;

  public List<AuditTableDto> tables() {
    List<AuditTableDto> result = new ArrayList<>();
    for (String table : TABLES) {
      AuditTableDto dto = new AuditTableDto();
      dto.table = table;
      dto.count = ((Number) entityManager.createNativeQuery("select count(*) from " + table + "_aud").getSingleResult()).longValue();
      result.add(dto);
    }
    return result;
  }

  public AuditPageDto find(String table, String username, String entityId, int page, int size) {
    if (table == null || !TABLES.contains(table)) {
      throw ApiException.badRequest("Table inconnue : " + table);
    }
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    int safePage = Math.max(page, 0);
    boolean byUser = username != null && !username.isBlank();
    boolean byId = entityId != null && !entityId.isBlank();

    StringBuilder where = new StringBuilder(" where 1 = 1");
    if (byUser) {
      where.append(" and r.username = :username");
    }
    if (byId) {
      where.append(" and cast(a.id as text) = :entityId");
    }
    String from = " from " + table + "_aud a join revinfo r on r.id = a.rev" + where;

    Query count = entityManager.createNativeQuery("select count(*)" + from);
    Query rows = entityManager.createNativeQuery(
        "select a.*, r.timestamp as audit_ts, r.username as audit_user" + from
            + " order by a.rev desc, cast(a.id as text)", Tuple.class);
    for (Query query : new Query[] {count, rows}) {
      if (byUser) {
        query.setParameter("username", username);
      }
      if (byId) {
        query.setParameter("entityId", entityId);
      }
    }
    rows.setFirstResult(safePage * safeSize);
    rows.setMaxResults(safeSize);

    AuditPageDto result = new AuditPageDto();
    result.table = table;
    result.page = safePage;
    result.size = safeSize;
    result.total = ((Number) count.getSingleResult()).longValue();
    @SuppressWarnings("unchecked")
    List<Tuple> tuples = rows.getResultList();
    for (Tuple tuple : tuples) {
      result.items.add(toEntry(tuple));
    }
    return result;
  }

  private AuditEntryDto toEntry(Tuple tuple) {
    AuditEntryDto entry = new AuditEntryDto();
    entry.revision = ((Number) tuple.get("rev")).intValue();
    Number timestamp = (Number) tuple.get("audit_ts");
    entry.timestamp = timestamp == null ? null : Instant.ofEpochMilli(timestamp.longValue());
    entry.username = (String) tuple.get("audit_user");
    Number type = (Number) tuple.get("revtype");
    entry.operation = type == null || type.intValue() < 0 || type.intValue() > 2 ? "INCONNUE" : OPERATIONS[type.intValue()];
    entry.entityId = String.valueOf(tuple.get("id"));
    entry.data = new LinkedHashMap<>();
    for (TupleElement<?> element : tuple.getElements()) {
      String name = element.getAlias();
      if (!TECHNICAL.contains(name)) {
        entry.data.put(name, normalize(tuple.get(name)));
      }
    }
    return entry;
  }

  private Object normalize(Object value) {
    if (value instanceof Timestamp ts) {
      return ts.toInstant().toString();
    }
    if (value instanceof java.sql.Date || value instanceof java.util.UUID) {
      return value.toString();
    }
    return value;
  }
}
