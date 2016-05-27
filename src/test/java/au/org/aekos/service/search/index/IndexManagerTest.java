package au.org.aekos.service.search.index;

import java.io.IOException;

import org.junit.Test;

public class IndexManagerTest {

	//@Test
	public void initialLuceneTest() throws IOException{
		IndexManager im = new IndexManager();
		im.createIndex();
	}
	
	
}
