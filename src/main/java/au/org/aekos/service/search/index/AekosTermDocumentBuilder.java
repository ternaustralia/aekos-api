package au.org.aekos.service.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.util.BytesRef;

//Try 2 types of document - preprocessed/ edit document
//  or  occurence document
public class AekosTermDocumentBuilder {
	
	//Species to trait metamodel  -  

	//Trait to species

	//Species to environment   ( soil ph, aspect, slope etc. )  meta  query parameter constraints 

	/**
	 * Builds a document for a single trait to species occurence ( or vice versa )
	 * @param trait
	 * @param species
	 * @return
	 */
	public static Document buildTraitSpeciesTermDocument(String trait, String species){
		Document doc = new Document();
		addStringFieldAndSort(IndexConstants.FLD_TRAIT, trait, doc);
		addStringFieldAndSort(IndexConstants.FLD_SPECIES, species, doc);
		addDocumentType(DocumentType.TRAIT_SPECIES, doc);
		addUidField(trait, species, DocumentType.TRAIT_SPECIES.name(), doc);
		return doc;
	}
	
	public static Document buildSpeciesEnvironmentTermDocument(String species, String environment){
		Document doc = new Document();
		addStringFieldAndSort(IndexConstants.FLD_SPECIES, species, doc);
		addStringFieldAndSort(IndexConstants.FLD_ENVIRONMENT, environment, doc);
		addDocumentType(DocumentType.SPECIES_ENV, doc);
		addUidField(species, environment, DocumentType.SPECIES_ENV.name(), doc);
		return doc;
	}
	
	public static void addUidField(String trait1, String trait2, String documentType, Document doc){
		String uidHashCode = getUidField(trait1, trait2, documentType);
		doc.add(new StringField(IndexConstants.FLD_UNIQUE_ID, uidHashCode, Field.Store.YES ));
	}
	
	public static void addStringFieldAndSort( String fieldName, String value, Document doc){
		doc.add(new StringField(fieldName, value, Field.Store.YES));
		doc.add(new SortedDocValuesField(fieldName, new BytesRef(value)));
	}
	
	public static void addDocumentType(DocumentType docType, Document doc){
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE , docType.name(), Field.Store.NO));
	}
	
	//Was going to just use hashcode but no guarantee of uniqueness
	public static String getUidField(String term1, String term2, String docType) {
		return term1 + "-" + term2 + "-" + docType;
	}
	
}
