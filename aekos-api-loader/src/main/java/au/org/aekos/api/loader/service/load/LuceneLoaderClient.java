package au.org.aekos.api.loader.service.load;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
	public void addSpeciesRecord(SpeciesLoaderRecord record) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.SPECIES_RECORD, Field.Store.YES));
		String speciesName = record.getSpeciesName();
		doc.add(new TextField(IndexConstants.FLD_SPECIES, speciesName, Field.Store.YES)); // FIXME split into scientificName and taxonRemarks
		doc.add(new TextField(IndexConstants.FLD_SAMPLING_PROTOCOL, record.getSamplingProtocol(), Field.Store.YES));
		doc.add(new StoredField(IndexConstants.FLD_BIBLIOGRAPHIC_CITATION, record.getBibliographicCitation()));
		doc.add(new StringField(IndexConstants.FLD_LOCATION_ID, record.getLocationId(), Field.Store.YES));
		doc.add(new StringField(IndexConstants.FLD_EVENT_DATE, record.getEventDate(), Field.Store.YES));
		doc.add(new SortedDocValuesField(IndexConstants.FLD_JOIN_KEY, new BytesRef(record.getJoinKey())));
doc.add(new StoredField("join_dump", record.getJoinKey()));
		// FIXME need to index all the fields
		FacetsConfig config = new FacetsConfig();
		addAllAndFacet(doc, IndexConstants.FLD_TRAIT, record.getTraitNames(), config);
		writeTraitsTo(doc, record.getTraits());
		caseInsensitiveTokeniseAndBoost(speciesName, doc);
		writeDocument(config.build(doc), indexWriter);
	}
	
	@Override
	public void addEnvRecord(EnvironmentLoaderRecord record) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.ENV_RECORD, Field.Store.YES));
		doc.add(new TextField(IndexConstants.FLD_LOCATION_ID, record.getLocationId(), Field.Store.YES));
		doc.add(new StringField(IndexConstants.FLD_EVENT_DATE, record.getEventDate(), Field.Store.YES));
doc.add(new SortedDocValuesField(IndexConstants.FLD_JOIN_KEY + "a" /*FIXME remove 'a'*/, new BytesRef(record.getJoinKey())));
doc.add(new StoredField("join_dump", record.getJoinKey()));
		// FIXME need to index all the fields
		FacetsConfig config = new FacetsConfig();
		addAllAndFacet(doc, IndexConstants.FLD_ENVIRONMENT, record.getEnvironmentalVariableNames(), config);
		writeDocument(config.build(doc), indexWriter);
	}
	
	private void addAllAndFacet(Document doc, String fieldName, Set<String> values, FacetsConfig config) {
		config.setMultiValued(fieldName, true); 
		for (String curr : values) {
			doc.add(new StringField(fieldName, curr, Field.Store.YES));
			doc.add(new SortedSetDocValuesFacetField(fieldName, curr));
		}
	}
	
	private static void caseInsensitiveTokeniseAndBoost(String speciesName, Document doc){
		String cleanedLcSpeciesName = speciesName.toLowerCase().replace(" ", "").replace(".", "");
		Field textField = new StringField(IndexConstants.FLD_SEARCH, cleanedLcSpeciesName, Field.Store.NO );
		//textField.setBoost(10.0f);
		doc.add(textField);
		doc.add(new SortedDocValuesField(IndexConstants.FLD_SEARCH, new BytesRef(cleanedLcSpeciesName)));
		tokenise(speciesName, doc);
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

	private void writeDocument(Document doc, IndexWriter writer) throws IOException{
		if (writer == null) {
			throw new NullPointerException("writer is null, have you called .beginLoad() yet?");
		}
		writer.addDocument(doc);
		if (++commitCount == commitLimit) {
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
