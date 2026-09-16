package ci.transit.system.transport.server.impl.ennumerations;

public enum RentalStatus {

	RESERVEE("RESERVEE"),
	EN_COURS("EN COURS"),
	TERMINEE("TERMINEE"),
	ANNULEE("ANNULEE");


	private String value;

	private RentalStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
