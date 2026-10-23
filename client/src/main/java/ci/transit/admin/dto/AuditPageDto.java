package ci.transit.admin.dto;

import java.util.ArrayList;
import java.util.List;

public class AuditPageDto {
  public String table;
  public long total;
  public int page;
  public int size;
  public List<AuditEntryDto> items = new ArrayList<>();

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<AuditEntryDto> getItems() {
    return items;
  }

  public void setItems(List<AuditEntryDto> items) {
    this.items = items;
  }
}
