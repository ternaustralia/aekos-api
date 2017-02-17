package au.org.aekos.api.loader.service.load;

import static au.org.aekos.api.loader.service.load.AekosTermDocumentBuilder.buildSpeciesEnvironmentTermDocument;
import static au.org.aekos.api.loader.service.load.AekosTermDocumentBuilder.buildSpeciesTermDocument;
import static au.org.aekos.api.loader.service.load.AekosTermDocumentBuilder.buildTraitSpeciesTermDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.index.Trait;


/**
 * To use:
 * <pre>
 *   // client is a LuceneLoaderClient
 *   client.beingLoad();
 *   // do you stuff
 *   cliet.endLoad();
 * 
 * @author Ben
 *
 */
@Component
public class LuceneLoaderClient implements LoaderClient {

	private static final Logger logger = LoggerFactory.getLogger(LuceneLoaderClient.class);
	private static final Gson gson = new Gson();
	
	@Value("${lucene.index.writer.commitLimit}")
	private int commitLimit = 1000;
	
	private int commitCount = 0;
	
	@Autowired
	private TermIndexManager indexManager;
	
	//TODO not threadsafe for parallel load processes . . .
	private IndexWriter indexWriter;
	
	@Override
	public void beginLoad() {
		try {
			indexWriter = indexManager.getIndexWriter();
			commitCount = 0;
		} catch (IOException e) {
			logger.error("Failed to begin the load process", e);
		}
	}
	
	@Override
	public void endLoad() {
		try {
			indexWriter.commit();
			indexWriter.flush();
			indexWriter.close();
			indexManager.flushDeletions();
		} catch (IOException e) {
			logger.error("Failed to end the load process", e);
		}
	}
	
	@Override
	public void deleteAll() throws IOException {
		try {
			indexWriter.deleteAll();
		} catch (NullPointerException e) {
			throw new RuntimeException("Have you called beginLoad() yet?", e);
		}
	}
	
	@Override
	public void addSpeciesTraitTermsToIndex(String species, List<String> traits) throws IOException {
		for(String trait: traits){
			addSpeciesTraitTermToIndex(species, trait);
		}
	}

	@Override
	public void addTraitSpeciesTermsToIndex(String trait, List<String> species) throws IOException {
		for(String speciesStr : species){
			addSpeciesTraitTermToIndex(speciesStr, trait);
		}
	}

	@Override
	public void addSpeciesTraitTermToIndex(String species, String trait) throws IOException {
		Document doc = buildTraitSpeciesTermDocument(species, trait);
		writeDocument(doc, indexWriter);
	}

	@Override
	public void addSpeciesEnvironmentTermsToIndex(String species, List<String> environmentTraits) throws IOException {
		for(String environmentTrait : environmentTraits ){
			addSpeciesEnvironmentTermToIndex(species, environmentTrait);
		}
	}

	@Override
	public void addSpeciesEnvironmentTermToIndex(String species, String environmentTrait) throws IOException {
		Document doc = buildSpeciesEnvironmentTermDocument(species, environmentTrait);
		writeDocument(doc, indexWriter);
	}
	
	@Override
	public void addSpecies(String speciesName, int speciesCount) throws IOException {
		Document doc = buildSpeciesTermDocument(speciesName, speciesCount);
		writeDocument(doc, indexWriter);
	}
	
	@Override
	public void addSpeciesRecord(SpeciesLoaderRecord record) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.SPECIES_RECORD, Field.Store.YES));
		doc.add(new TextField(IndexConstants.FLD_SPECIES, record.getSpeciesName(), Field.Store.YES)); // FIXME split into scientificName and taxonRemarks
		doc.add(new TextField(IndexConstants.FLD_SAMPLING_PROTOCOL, record.getSamplingProtocol(), Field.Store.YES));
		doc.add(new StoredField(IndexConstants.FLD_BIBLIOGRAPHIC_CITATION, record.getBibliographicCitation()));
		// FIXME need to index all the fields
//		StringBuilder allTraitValues = new StringBuilder();
		FacetsConfig config = new FacetsConfig(); // FIXME move to field
		for (String curr : record.getTraitNames()) {
			doc.add(new StringField(IndexConstants.FLD_TRAIT, curr, Field.Store.YES));
			doc.add(new SortedSetDocValuesField(IndexConstants.FLD_TRAIT, new BytesRef(curr)));
//			allTraitValues.append(curr);
		}
		writeTraitsTo(doc, record.getTraits());
		writeDocument(config.build(doc), indexWriter);
	}
	
	@Override
	public void addEnvRecord(EnvironmentLoaderRecord record) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.ENV_RECORD, Field.Store.YES));
		doc.add(new TextField(IndexConstants.FLD_LOCATION_ID, record.getLocationID(), Field.Store.YES));
		// FIXME need to index all the fields
		StringBuilder allVariableValues = new StringBuilder();
		for (String curr : record.getEnvironmentalVariableNames()) {
			doc.add(new StringField(IndexConstants.FLD_ENVIRONMENT, curr, Field.Store.YES));
			allVariableValues.append(curr);
		}
		doc.add(new SortedDocValuesField(IndexConstants.FLD_ENVIRONMENT, new BytesRef(allVariableValues.toString())));
		writeDocument(doc, indexWriter);
	}

	private void writeDocument(Document doc, IndexWriter writer) throws IOException{
		if (writer == null) {
			throw new NullPointerException("writer is null, have you called .beginLoad() yet?");
		}
		IndexableField uidField = doc.getField(IndexConstants.FLD_UNIQUE_ID);
		boolean hasDocumentAlreadyBeenWritten = uidField != null;
		if(hasDocumentAlreadyBeenWritten){
		    String uid = uidField.stringValue();
		    writer.updateDocument(new Term(IndexConstants.FLD_UNIQUE_ID, uid), doc);
		}else{
			writer.addDocument(doc);
		}
		if(++commitCount == commitLimit ){
			writer.commit();
			commitCount = 0;
		}
	}

	public void setIndexManager(TermIndexManager indexManager) {
		this.indexManager = indexManager;
	}

	static Collection<Trait> getTraitsFrom(Document doc) {
		String field = IndexConstants.FLD_STORED_TRAITS;
		String traitJson = doc.get(field);
		if (traitJson == null) {
			throw new IllegalStateException(String.format("Data problem: no data stored for '%s' field", field));
		}
		return Arrays.asList(gson.fromJson(traitJson, Trait[].class));
	}
	
	static void writeTraitsTo(Document doc, Collection<Trait> traits) {
		String traitsJson = gson.toJson(traits);
		doc.add(new StoredField(IndexConstants.FLD_STORED_TRAITS, traitsJson));
	}
}
