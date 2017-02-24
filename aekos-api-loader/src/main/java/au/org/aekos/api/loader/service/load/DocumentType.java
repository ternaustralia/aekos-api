package au.org.aekos.api.loader.service.load;

public enum DocumentType {
	
	SPECIES_SUMMARY,
	SPECIES_RECORD,
	ENV_RECORD;
	
	public String getCode() {
		return name().toLowerCase();
	}
}
