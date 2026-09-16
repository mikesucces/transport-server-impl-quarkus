package ci.transit.system.transport.server.impl.ennumerations;

public enum SubscriptionPlan {

	HEBDOMADAIRE("HEBDOMADAIRE"),
	MENSUEL("MENSUEL"),
	TRIMESTRIEL("TRIMESTRIEL");


	private String value;

	private SubscriptionPlan(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
