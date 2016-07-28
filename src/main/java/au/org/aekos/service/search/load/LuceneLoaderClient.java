package au.org.aekos.service.search.load;

import java.io.IOException;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.org.aekos.service.retrieval.IndexLoaderRecord;
import au.org.aekos.service.search.index.IndexConstants;
import au.org.aekos.service.search.index.TermIndexManager;

import static au.org.aekos.service.search.index.AekosTermDocumentBuilder.*;


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
		} catch (IOException e) {
			logger.error("Failed to end the load process", e);
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
	public void addSpecies(IndexLoaderRecord record) throws IOException {
		Document doc = buildSpeciesTermDocument(record.getScientificName());
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
