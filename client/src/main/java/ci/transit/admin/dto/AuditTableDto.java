package ci.transit.admin.dto;

public class AuditTableDto {
  public String table;
  public long count;

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }
}
