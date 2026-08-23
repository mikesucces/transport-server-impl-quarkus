package ci.transit.system.transport.server.impl.ennumerations;

public enum MaintenanceStatus {

	PLANIFIE("PLANIFIE"),
	EN_COURS("EN COURS"),
	TERMINE("TERMINE"),
	ANNULE("ANNULE");


	private String value;

	private MaintenanceStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
