package au.org.aekos.api.loader.service.index;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public interface TermIndexManager {

	Directory getTermIndex() throws IOException;
	
	void flushDeletions() throws IOException;
	
	IndexWriter getIndexWriter() throws IOException;
	
	IndexSearcher getIndexSearcher() throws IOException;
	
	void releaseIndexSearcher(IndexSearcher searcher) throws IOException;
	
	public void closeTermIndex() throws IOException;
}
