package ci.transit.system.transport.server.impl.ennumerations;

public enum RouteStatus {

	ACTIVE("ACTIVE"),
	SUSPENDUE("SUSPENDUE");


	private String value;

	private RouteStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
