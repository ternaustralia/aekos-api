package au.org.aekos.service.search.index;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

public interface TermIndexManager {

	Directory getTermIndex() throws IOException;
	
	IndexWriter getIndexWriter() throws IOException;
	
	IndexReader getIndexReader();
	
}
