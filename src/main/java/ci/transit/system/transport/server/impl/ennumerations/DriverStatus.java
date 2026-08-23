package ci.transit.system.transport.server.impl.ennumerations;

public enum DriverStatus {

	DISPONIBLE("DISPONIBLE"),
	EN_ROTATION("EN ROTATION"),
	REPOS("REPOS"),
	SUSPENDU("SUSPENDU");


	private String value;

	private DriverStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
