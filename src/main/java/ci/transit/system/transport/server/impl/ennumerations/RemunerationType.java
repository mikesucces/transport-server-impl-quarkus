package ci.transit.system.transport.server.impl.ennumerations;

public enum RemunerationType {

	FIXE("FIXE"),
	PAR_TRAJET("PAR TRAJET"),
	COMMISSION("COMMISSION");


	private String value;

	private RemunerationType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
