package ci.transit.system.transport.server.impl.ennumerations;

public enum RotationStatus {

	PLANIFIEE("PLANIFIEE"),
	EN_COURS("EN COURS"),
	TERMINEE("TERMINEE"),
	ANNULEE("ANNULEE");


	private String value;

	private RotationStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
