package au.org.aekos.service.search.load;

import static au.org.aekos.service.search.index.AekosTermDocumentBuilder.buildSpeciesEnvironmentTermDocument;
import static au.org.aekos.service.search.index.AekosTermDocumentBuilder.buildSpeciesTermDocument;
import static au.org.aekos.service.search.index.AekosTermDocumentBuilder.buildTraitSpeciesTermDocument;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.org.aekos.service.index.IndexLoaderRecord;
import au.org.aekos.service.search.index.IndexConstants;
import au.org.aekos.service.search.index.TermIndexManager;


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
//			indexManager.resetSearcher(); // FIXME check if this works, should mean we don't have to restart the app
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
	public void addSpeciesRecord(IndexLoaderRecord record) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, "tom", Field.Store.YES));
		doc.add(new TextField("speciesName", record.getSpeciesName(), Field.Store.YES));
		for (String curr : record.getTraitNames()) {
			doc.add(new StringField("trait", curr, Field.Store.YES));
		}
		// TODO add env
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
}
