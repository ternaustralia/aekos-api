package au.org.aekos.api.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

public class LuceneMultiDocValuesTest {

	private static final String COLOURS_FIELD = "doctype";
	private static final String RED = "red";
	private static final String GREEN = "green";
	private static final String BLUE = "blue";
	private IndexWriter writer;
	private IndexSearcher searcher;
	private SearcherManager searcherManager;
	private RAMDirectory termIndex;

	/**
	 * Can we create a repeated/multi field that has DocValues?
	 */
	@Test
	public void testLuceneMultiDocValues01() throws IOException {
		createIndex();
		populateIndex();
		queryForRed();
		queryForGreen();
	}

	/**
	 * Can we find the single document that contains 'red'?
	 */
	private void queryForRed() throws IOException {
		Query query = new TermQuery(new Term(COLOURS_FIELD, RED));
		TopDocs td = searcher.search(query, 1);
		assertThat("Should find a single matching document", td.totalHits, is(1));
		Document doc = searcher.doc(td.scoreDocs[0].doc);
		IndexableField[] fields = doc.getFields(COLOURS_FIELD);
		assertThat(fields[0].stringValue(), is(RED));
		assertThat(fields[1].stringValue(), is(GREEN));
	}
	
	/**
	 * Can we find both documents that contain 'green'?
	 */
	private void queryForGreen() throws IOException {
		Query query = new TermQuery(new Term(COLOURS_FIELD, GREEN));
		TopDocs td = searcher.search(query, 2);
		assertThat("Should find a single matching document", td.totalHits, is(2));
		Document doc1 = searcher.doc(td.scoreDocs[0].doc);
		IndexableField[] fields = doc1.getFields(COLOURS_FIELD);
		assertThat(fields[0].stringValue(), is(RED));
		assertThat(fields[1].stringValue(), is(GREEN));
		Document doc2 = searcher.doc(td.scoreDocs[1].doc);
		IndexableField[] field2 = doc2.getFields(COLOURS_FIELD);
		assertThat(field2[0].stringValue(), is(GREEN));
		assertThat(field2[1].stringValue(), is(BLUE));
	}

	private void createIndex() throws IOException {
		termIndex = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE;
		config.setOpenMode(openMode);
		writer = new IndexWriter(termIndex, config);
	}
	
	private void populateIndex() throws IOException {
		addDoc(COLOURS_FIELD, RED, GREEN);
		addDoc(COLOURS_FIELD, GREEN, BLUE);
		writer.commit();
		writer.flush();
		writer.close();
		searcherManager = new SearcherManager(termIndex, new SearcherFactory());
		searcher = searcherManager.acquire();
	}

	private void addDoc(String field, String...values) throws IOException {
		Document doc1 = new Document();
		StringBuilder allValues = new StringBuilder();
		for (String curr : values) {
			doc1.add(new StringField(field, curr, Field.Store.YES));
			allValues.append(curr);
		}
		doc1.add(new SortedDocValuesField(field, new BytesRef(allValues.toString())));
		writer.addDocument(doc1);
	}
}
