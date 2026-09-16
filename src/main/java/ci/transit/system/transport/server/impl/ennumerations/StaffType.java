package ci.transit.system.transport.server.impl.ennumerations;

public enum StaffType {

	OWNER("OWNER"),
	MANAGER("MANAGER"),
	CONTROLLER("CONTROLLER"),
	DRIVER("DRIVER");


	private String value;

	private StaffType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
