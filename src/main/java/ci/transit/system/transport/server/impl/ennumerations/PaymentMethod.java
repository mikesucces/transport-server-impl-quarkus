package ci.transit.system.transport.server.impl.ennumerations;

public enum PaymentMethod {

	ESPECES("ESPECES"),
	MOBILE_MONEY("MOBILE MONEY"),
	VIREMENT("VIREMENT"),
	AUTRE("AUTRE");


	private String value;

	private PaymentMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
