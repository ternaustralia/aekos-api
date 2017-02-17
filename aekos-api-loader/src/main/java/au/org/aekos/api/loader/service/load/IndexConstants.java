package au.org.aekos.api.loader.service.load;

public class IndexConstants {
	
	private IndexConstants() {}
	
    //Fields for the autocomplete species search
	public static final String FLD_DISPLAY_VALUE = "displayValue";
	public static final String FLD_SEARCH = "search";
	public static final String FLD_SEARCH_LEV = "search_lev";
	public static final String FLD_SEARCH_SUB = "search_sub";
	public static final String FLD_INSTANCE_COUNT = "instanceCount";
	
	public static final String FLD_DOC_INDEX_TYPE = "doc_index_type";
	
	public static class DocTypes {
		private DocTypes() {}
		public static final String TRAIT_SPECIES = DocumentType.TRAIT_SPECIES.getCode();
		public static final String SPECIES_ENV = DocumentType.SPECIES_ENV.getCode();
		public static final String SPECIES_SUMMARY = DocumentType.SPECIES_SUMMARY.getCode();
		public static final String SPECIES_RECORD = DocumentType.SPECIES_RECORD.getCode();
		public static final String ENV_RECORD = DocumentType.ENV_RECORD.getCode();
	}
	
	//Unique term for loading unique checking
	public static final String FLD_UNIQUE_ID = "uid";
	
	//Exact match field name
	public static final String FLD_SPECIES = "species";
	public static final String FLD_TRAIT = "trait";
	public static final String FLD_ENVIRONMENT = "environment";
	public static final String FLD_SAMPLING_PROTOCOL = "sampling_protocol";
	public static final String FLD_BIBLIOGRAPHIC_CITATION = "bibliographic_citation";
	public static final String FLD_STORED_TRAITS = "traitStorage";
}
