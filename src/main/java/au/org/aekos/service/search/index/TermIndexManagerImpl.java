package au.org.aekos.service.search.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TermIndexManagerImpl implements TermIndexManager, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(TermIndexManagerImpl.class);
	
	private FSDirectory termIndex;
	
	private SearcherManager searcherManager;
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
	@Value("${lucene.index.createMode}")
	private boolean createMode;
	
	@Value("${lucene.index.writer.commitLimit}")
	private int commitLimit = 1000;

	@Override
	public Directory getTermIndex() throws IOException {
		if(termIndex == null){
			initialiseIndexDirectory();
		}
		return termIndex;
	}
	
	private void initialiseIndexDirectory() throws IOException{
		ensureIndexPathExists();
		//termIndex = new NRTCachingDirectory(FSDirectory.open(Paths.get(getIndexPath())), 5.0, 60.0);
		termIndex = FSDirectory.open(Paths.get(getIndexPath()));
		boolean isEmpty = termIndex.listAll().length == 0;
		if (isEmpty) {
			logger.warn("Index directory is empty, initialising the empty directory");
			IndexWriter w = getIndexWriter();
			w.commit();
			w.close();
		}
	}

	private String getIndexPath(){
		return indexPath;
	}
	
	//Ensure index path exists, if not attempt to create it.
	private boolean ensureIndexPathExists(){
		if(Files.isDirectory(Paths.get(getIndexPath()))){
			return true;
		}
		//else try and create the path
		try {
			Files.createDirectories(Paths.get(getIndexPath()));
		} catch (IOException e) {
			logger.error("Can't create index path :" + getIndexPath(), e);
			return false;
		}
		return true;
	}

	@Override
	public IndexWriter getIndexWriter() throws IOException {
		flushDeletions();
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriterConfig.OpenMode openMode = createMode ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
		config.setOpenMode(openMode);
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
	public void releaseIndexSearcher(IndexSearcher searcher) throws IOException{
		searcherManager.release(searcher);
	}

	@Override
	public void closeTermIndex() throws IOException{
		if(termIndex != null){
			try {
				flushDeletions();
				if(searcherManager != null ){
					searcherManager.close();
				}
				termIndex.close();
				
			} catch (IOException e) {
				logger.error("Issue with Term Index",e);
				throw e;
			}
			searcherManager = null;
			termIndex = null;
		}
	}
	
	@Override
	public void destroy() throws Exception {
		closeTermIndex();
	}

	@Override
	public void flushDeletions() throws IOException {
		if(termIndex != null && termIndex.checkPendingDeletions()){
			termIndex.deletePendingFiles();
		}
	}
}
