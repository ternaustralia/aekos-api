package au.org.aekos.api.loader.service.index;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class RAMDirectoryTermIndexManager implements TermIndexManager {

	private RAMDirectory index;
	private SearcherManager searcherManager;

	@Override
	public Directory getTermIndex() throws IOException {
		if (index == null) {
			index = new RAMDirectory();
		}
		return index;
	}

	@Override
	public void flushDeletions() throws IOException {
		// Nothing to do
	}

	@Override
	public IndexWriter getIndexWriter() throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		return new IndexWriter(getTermIndex(), config);
	}

	@Override
	public IndexSearcher getIndexSearcher() throws IOException {
		if(searcherManager == null){
			searcherManager = new SearcherManager(getTermIndex(), new SearcherFactory());
		}
		return searcherManager.acquire();
	}

	@Override
	public void releaseIndexSearcher(IndexSearcher searcher) throws IOException {
		searcherManager.release(searcher);
	}

	@Override
	public void closeTermIndex() throws IOException {
		if (index == null) {
			return;
		}
		index.close();
	}

}
