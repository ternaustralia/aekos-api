package au.org.aekos.service.search.load;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import au.org.aekos.service.retrieval.IndexLoaderRecord;
import au.org.aekos.service.search.index.TermIndexManager;

public class LuceneLoaderClientTest {

	/**
	 * Can we add species records to the index?
	 */
	@Test
	public void testAddSpecies01() throws IOException {
		LuceneLoaderClient objectUnderTest = new LuceneLoaderClient();
		TermIndexManager indexManager = mock(TermIndexManager.class);
		IndexWriter writer = new IndexWriter(new RAMDirectory(), new IndexWriterConfig(new StandardAnalyzer()));
		when(indexManager.getIndexWriter()).thenReturn(writer);
		objectUnderTest.setIndexManager(indexManager);
		objectUnderTest.beginLoad();
		objectUnderTest.addSpecies(new IndexLoaderRecord("Acacia abbatiana", Collections.emptySet(), Collections.emptySet()));
		objectUnderTest.addSpecies(new IndexLoaderRecord("Acacia abietina", Collections.emptySet(), Collections.emptySet()));
		objectUnderTest.addSpecies(new IndexLoaderRecord("Acacia abrupta", Collections.emptySet(), Collections.emptySet()));
		assertThat(writer.numDocs(), is(3));
		objectUnderTest.endLoad();
	}
}
