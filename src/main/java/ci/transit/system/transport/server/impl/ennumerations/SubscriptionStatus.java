package ci.transit.system.transport.server.impl.ennumerations;

public enum SubscriptionStatus {

	ACTIVE("ACTIVE"),
	EXPIRED("EXPIRED"),
	CANCELLED("CANCELLED");


	private String value;

	private SubscriptionStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
