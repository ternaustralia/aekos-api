package au.org.aekos.service.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;
import org.springframework.util.StringUtils;

public class AekosTermDocumentBuilder {
	
	/**
	 * Builds a document for a single trait to species occurrence ( or vice versa )
	 * 
	 * @param species	name of species
	 * @param trait		name of trait
	 * @return			populated document
	 */
	public static Document buildTraitSpeciesTermDocument(String species, String trait){
		Document doc = new Document();
		addStringFieldAndSort(IndexConstants.FLD_SPECIES, species, doc);
		addStringFieldAndSort(IndexConstants.FLD_TRAIT, trait, doc);
		addDocumentType(DocumentType.TRAIT_SPECIES, doc);
		addUidField(species, trait, DocumentType.TRAIT_SPECIES.name(), doc);
		return doc;
	}
	
	/**
	 * Builds a document for a single environment variable to species occurrence ( or vice versa )
	 * 
	 * @param species		name of species
	 * @param environment	name of variable
	 * @return				populated document
	 */
	public static Document buildSpeciesEnvironmentTermDocument(String species, String environment){
		Document doc = new Document();
		addStringFieldAndSort(IndexConstants.FLD_SPECIES, species, doc);
		addStringFieldAndSort(IndexConstants.FLD_ENVIRONMENT, environment, doc);
		addDocumentType(DocumentType.SPECIES_ENV, doc);
		addUidField(species, environment, DocumentType.SPECIES_ENV.name(), doc);
		return doc;
	}
	
	/**
	 * Builds a document for a single species
	 * 
	 * @param speciesName	name of species
	 * @return				populated document
	 */
	public static Document buildSpeciesTermDocument(String speciesName){
		speciesName = speciesName.trim();
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.FLD_TRAIT_VALUE /*FIXME should we be using 'trait' here? */, speciesName, Field.Store.YES));
		doc.add(new StringField(IndexConstants.FLD_DISPLAY_VALUE, speciesName, Field.Store.YES));
		caseInsensitiveTokeniseAndBoost(speciesName, doc);
		return doc;
	}
	
	private static void caseInsensitiveTokeniseAndBoost(String speciesName, Document doc){
		String cleanedLcSpeciesName = speciesName.toLowerCase().replace(" ", "").replace(".", "");
		Field textField = new StringField(IndexConstants.FLD_SEARCH, cleanedLcSpeciesName, Field.Store.NO );
		//textField.setBoost(10.0f);
		doc.add(textField);
		doc.add(new SortedDocValuesField(IndexConstants.FLD_SEARCH, new BytesRef(cleanedLcSpeciesName)));
		addLevenshteinDistanceField(doc, cleanedLcSpeciesName);
		tokenise(speciesName, doc);
	}

	private static void addLevenshteinDistanceField(Document doc, String cleanedLcSpeciesName) {
		String cleanedLcSpeciesNameNoPunctuation = cleanedLcSpeciesName.replaceAll(",", "").replaceAll("'", "").replaceAll("-","");
	    Field textFieldLevenshtein = new TextField(IndexConstants.FLD_SEARCH_LEV, cleanedLcSpeciesNameNoPunctuation, Field.Store.NO );
		doc.add(textFieldLevenshtein);
	}

	private static void tokenise(String traitDisplayValue, Document doc) {
		if(!traitDisplayValue.contains(" ")){
			return;
		}
		String lc = traitDisplayValue.toLowerCase();
		String[] tokens = lc.split(" ");
		if(tokens.length == 0){
			return;
		}
		for(int x = 1; x < tokens.length; x++){
			String token = tokens[x];
			if(StringUtils.hasLength(token) && !token.contains(".")) {
				Field field = new TextField(IndexConstants.FLD_SEARCH_SUB, token, Field.Store.NO);
				doc.add(field);
			}
		}
	}
	
	private static void addUidField(String term1, String term2, String documentType, Document doc){
		String uidHashCode = getUidField(term1, term2, documentType);
		doc.add(new StringField(IndexConstants.FLD_UNIQUE_ID, uidHashCode, Field.Store.YES ));
	}
	
	private static void addStringFieldAndSort( String fieldName, String value, Document doc){
		doc.add(new StringField(fieldName, value, Field.Store.YES));
		doc.add(new SortedDocValuesField(fieldName, new BytesRef(value)));
	}
	
	private static void addDocumentType(DocumentType docType, Document doc){
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE , docType.name(), Field.Store.NO));
	}
	
	//Was going to just use hashcode but no guarantee of uniqueness
	private static String getUidField(String term1, String term2, String docType) {
		return term1 + "-" + term2 + "-" + docType;
	}
}
