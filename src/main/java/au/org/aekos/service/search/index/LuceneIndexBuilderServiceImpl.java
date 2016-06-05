package au.org.aekos.service.search.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import au.org.aekos.service.retrieval.StubRetrievalService;

@Service
public class LuceneIndexBuilderServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(LuceneIndexBuilderServiceImpl.class);
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
	@Value("${lucene.index.wpath}")
	private String windowsIndexPath;
	
	@Value("${lucene.index.createMode}")
	private boolean createMode;
	
	
	
	
	
	
	
	
	
	private String getIndexPath(){
		if(SystemUtils.IS_OS_WINDOWS){
			return windowsIndexPath;
		}
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
	
	
	
	
	
	
	
}
