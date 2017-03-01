package au.org.aekos.api.loader.service.load;

public class IndexConstants {
	
	private IndexConstants() {}
	
    //Fields for the autocomplete species search
	public static final String FLD_SEARCH = "search";
	public static final String FLD_SEARCH_LEV = "search_lev";
	public static final String FLD_SEARCH_SUB = "search_sub";
	
	public static final String FLD_DOC_INDEX_TYPE = "doc_index_type";
	
	public static class DocTypes {
		private DocTypes() {}
		public static final String SPECIES_SUMMARY = DocumentType.SPECIES_SUMMARY.getCode();
		public static final String SPECIES_RECORD = DocumentType.SPECIES_RECORD.getCode();
		public static final String ENV_RECORD = DocumentType.ENV_RECORD.getCode();
	}
	
	//Exact match field name
	public static final String FLD_SPECIES = "species";
	public static final String FLD_TRAIT = "trait";
	public static final String FLD_ENVIRONMENT = "environment";
	public static final String FLD_SAMPLING_PROTOCOL = "sampling_protocol";
	public static final String FLD_BIBLIOGRAPHIC_CITATION = "bibliographic_citation";
	public static final String FLD_STORED_TRAITS = "trait_storage";
	public static final String FLD_LOCATION_ID = "location_id";
	public static final String FLD_EVENT_DATE = "event_date";
	public static final String FLD_JOIN_KEY = "jk";
	public static final String FLD_LOCATION_NAME = "loc_name";
	public static final String FLD_DATASET_NAME = "ds_name";
}
