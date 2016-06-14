package au.org.aekos.service.search.index;

import java.io.IOException;

import org.apache.lucene.store.RAMDirectory;

public interface LuceneIndexBuilderService {

	RAMDirectory buildSpeciesRAMDirectory();
	
	void initialiseIndexWriter() throws IOException;
	
}
