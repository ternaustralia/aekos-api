package au.org.aekos.api.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneJoinTest {

	private static final Logger logger = LoggerFactory.getLogger(LuceneJoinTest.class);
	private static final String EMPLOYEE = "employee";
	private static final String DOCTYPE = "doctype";
	private static final String JOHN_SMITH = "johnsmith";
	private static final String FULLNAME_SORTED = "fullname_sorted";
	private static final String FULLNAME_BINARY = "fullname_binary";
	private IndexWriter writer;
	private IndexSearcher searcher;
	private SearcherManager searcherManager;
	private FSDirectory termIndex;

	/**
	 * Can we get the lucene join functionality to work?
	 */
	@Test
	@Ignore // FIXME can't get this to work yet
	public void testLuceneJoin01() throws IOException {
		createIndex();
		populateIndex();
		doJoin();
		cleanup();
	}

	private void doJoin() throws IOException {
		String fromField = FULLNAME_BINARY;
		boolean multipleValuesPerDocument = false;
		String toField = FULLNAME_BINARY;
		ScoreMode scoreMode = ScoreMode.Max; // Defines how the scores are translated into the other side of the join.
		Query fromQuery = new TermQuery(new Term(DOCTYPE, EMPLOYEE)); // Query executed to collect from values to join to the to values
		TopDocs fromQueryTopDocs = searcher.search(fromQuery, 1);
		assertThat("fromQuery should find the employee record", fromQueryTopDocs.totalHits, is(1));
		Query joinQuery = JoinUtil.createJoinQuery(fromField, multipleValuesPerDocument, toField, fromQuery, searcher, scoreMode);
		TopDocs td = searcher.search(joinQuery, 1); // Note: toSearcher can be the same as the fromSearcher
		assertThat(td.totalHits, is(1)); // apparent we can't... yet :(
		logger.info(td.totalHits + " results");
		for (int i = 0; i < td.scoreDocs.length; i++) {
			Document matchedDoc = searcher.doc(td.scoreDocs[i].doc);
			logger.info(matchedDoc.toString());
		}
	}

	private void createIndex() throws IOException {
		Path tempDir = Files.createTempDirectory("testLuceneJoin01");
		tempDir.toFile().deleteOnExit();
		termIndex = FSDirectory.open(tempDir);
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE;
		config.setOpenMode(openMode);
		writer = new IndexWriter(termIndex, config);
	}
	
	private void populateIndex() throws IOException {
		Document employeeDoc = new Document();
		employeeDoc.add(new StringField(DOCTYPE, EMPLOYEE, Field.Store.YES));
		employeeDoc.add(new TextField("fullname", JOHN_SMITH, Field.Store.YES));
		employeeDoc.add(new SortedDocValuesField(FULLNAME_SORTED, new BytesRef(JOHN_SMITH)));
		employeeDoc.add(new BinaryDocValuesField(FULLNAME_BINARY, new BytesRef(JOHN_SMITH)));
		employeeDoc.add(new TextField("department", "Accounting", Field.Store.YES));
		writer.addDocument(employeeDoc);
		Document likesDoc = new Document();
		likesDoc.add(new StringField(DOCTYPE, "likes", Field.Store.YES));
		likesDoc.add(new TextField("fullname", JOHN_SMITH, Field.Store.YES));
		likesDoc.add(new SortedDocValuesField(FULLNAME_SORTED, new BytesRef(JOHN_SMITH)));
		employeeDoc.add(new BinaryDocValuesField(FULLNAME_BINARY, new BytesRef(JOHN_SMITH)));
		employeeDoc.add(new TextField("fav_colour", "red", Field.Store.YES));
		writer.addDocument(likesDoc);
		writer.commit();
		writer.flush();
		writer.close();
		searcherManager = new SearcherManager(termIndex, new SearcherFactory());
		searcher = searcherManager.acquire();
	}
	
	private void cleanup() throws IOException {
		searcherManager.close();
		for (String curr: termIndex.listAll()) {
			termIndex.deleteFile(curr);
		}
		termIndex.getDirectory().toFile().delete();
	}
}
