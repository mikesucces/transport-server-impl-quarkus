package ci.transit.system.transport.server.impl.ennumerations;

public enum VehicleStatus {

	DISPONIBLE("DISPONIBLE"),
	EN_SERVICE("EN SERVICE"),
	ENTRETIEN("ENTRETIEN"),
	HORS_SERVICE("HORS SERVICE");


	private String value;

	private VehicleStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
