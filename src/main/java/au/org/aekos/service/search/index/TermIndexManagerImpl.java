package au.org.aekos.service.search.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.SystemUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TermIndexManagerImpl implements TermIndexManager, InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(TermIndexManagerImpl.class);
	
	private Directory termIndex;
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
	@Value("${lucene.index.wpath}")
	private String windowsIndexPath;
	
	@Value("${lucene.index.createMode}")
	private boolean createMode;
	
	@Value("${lucene.index.writer.commitLimit}")
	private int commitLimit = 1000;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public Directory getTermIndex() throws IOException {
		if(termIndex == null){
			initialiseIndexDirectory();
		}
		return termIndex;
	}
	
	private void initialiseIndexDirectory() throws IOException{
		ensureIndexPathExists();
		termIndex = FSDirectory.open(Paths.get(getIndexPath()));
	}

	public String getIndexPath(){
		if(SystemUtils.IS_OS_WINDOWS){
			return windowsIndexPath;
		}
		return indexPath;
	}
	
	//Ensure index path exists, if not attempt to create it. 
	public boolean ensureIndexPathExists(){
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
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriterConfig.OpenMode openMode = createMode ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
		config.setOpenMode(openMode);
		return new IndexWriter(getTermIndex(), config);
	}

	@Override
	public IndexReader getIndexReader() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//Not threadsafe today . . . 
	private int commitCounter = 0;
	
	public void writeDocument(Document doc, IndexWriter writer) throws IOException{
		IndexableField uidField = doc.getField(IndexConstants.FLD_UNIQUE_ID);
		if(uidField != null){
		    String uid = uidField.stringValue();
		    writer.updateDocument(new Term("id", uid), doc);
		}else{
			writer.addDocument(doc);
		}
		
	}
	

}
