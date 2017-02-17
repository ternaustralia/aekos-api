package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.index.Trait;
import au.org.aekos.api.loader.service.load.LuceneLoaderClient;

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
		objectUnderTest.addSpecies("Acacia abbatiana", 11);
		objectUnderTest.addSpecies("Acacia abietina", 22);
		objectUnderTest.addSpecies("Acacia abrupta", 33);
		assertThat(writer.numDocs(), is(3));
		objectUnderTest.endLoad();
	}
	
	/**
	 * Can we deserialse a trait array?
	 */
	@Test
	public void testGetTraitsFrom01() throws IOException {
		Document doc = new Document();
		String jsonTraitArray = "[{\"name\":\"height\",\"value\":\"3\",\"units\":\"metres\"},{\"name\":\"lifeForm\",\"value\":\"Shrub\",\"units\":\"\"}]";
		doc.add(new StoredField(IndexConstants.FLD_STORED_TRAITS, jsonTraitArray));
		Collection<Trait> result = LuceneLoaderClient.getTraitsFrom(doc);
		assertThat(result.size(), is(2));
		assertThat(result, hasItems(
				new Trait("height", "3", "metres"),
				new Trait("lifeForm", "Shrub", "")));
	}
	
	/**
	 * Can we serialse a trait array?
	 */
	@Test
	public void testWriteTraitsTo01() throws IOException {
		Document doc = new Document();
		Collection<Trait> traits = Arrays.asList(
				new Trait("height", "3", "metres"),
				new Trait("lifeForm", "Shrub", ""));
		LuceneLoaderClient.writeTraitsTo(doc, traits);
		String result = doc.get(IndexConstants.FLD_STORED_TRAITS);
		assertThat(result, is("[{\"name\":\"height\",\"value\":\"3\",\"units\":\"metres\"},{\"name\":\"lifeForm\",\"value\":\"Shrub\",\"units\":\"\"}]"));
	}
}
