package ci.transit.system.transport.server.impl.ennumerations;

public enum MaintenanceType {

	VIDANGE("VIDANGE"),
	REPARATION("REPARATION"),
	CONTROLE("CONTROLE"),
	PNEUMATIQUES("PNEUMATIQUES"),
	AUTRE("AUTRE");


	private String value;

	private MaintenanceType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
