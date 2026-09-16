package ci.transit.system.transport.server.impl.ennumerations;

public enum ScheduleDay {

	LUNDI("LUNDI"),
	MARDI("MARDI"),
	MERCREDI("MERCREDI"),
	JEUDI("JEUDI"),
	VENDREDI("VENDREDI"),
	SAMEDI("SAMEDI"),
	DIMANCHE("DIMANCHE");


	private String value;

	private ScheduleDay(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
