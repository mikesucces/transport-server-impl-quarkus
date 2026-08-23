package ci.transit.system.transport.server.impl.ennumerations;

public enum DocumentType {

	ASSURANCE("ASSURANCE"),
	VISITE_TECHNIQUE("VISITE TECHNIQUE"),
	CARTE_GRISE("CARTE GRISE"),
	LICENCE_TRANSPORT("LICENCE TRANSPORT"),
	AUTRE("AUTRE");


	private String value;

	private DocumentType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
