package au.org.aekos.service.search.index;

import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpeciesLookupIndexServiceImpl implements InitializingBean{
	
	private Logger logger = LoggerFactory.getLogger(SpeciesLookupIndexServiceImpl.class);
	
	private RAMDirectory speciesIndex;
	
	public RAMDirectory getSpeciesIndex() {
		return speciesIndex;
	}

	@Autowired
	private LuceneIndexBuilderService indexBuilderService;

	@Override
	public void afterPropertiesSet() throws Exception {
		speciesIndex = indexBuilderService.buildSpeciesRAMDirectory();
		logger.info("Species index created - ram bytes " + speciesIndex.ramBytesUsed());
	}
	
	
	

	
	
}
