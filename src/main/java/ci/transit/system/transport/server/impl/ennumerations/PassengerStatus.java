package ci.transit.system.transport.server.impl.ennumerations;

public enum PassengerStatus {

	ACTIVE("ACTIVE"),
	SUSPENDED("SUSPENDED"),
	DISABLED("DISABLED");


	private String value;

	private PassengerStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
