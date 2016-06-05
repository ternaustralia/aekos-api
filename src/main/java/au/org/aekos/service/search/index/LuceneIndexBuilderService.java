package au.org.aekos.service.search.index;

import org.apache.lucene.store.RAMDirectory;

public interface LuceneIndexBuilderService {

	RAMDirectory buildSpeciesRAMDirectory();
	
}
