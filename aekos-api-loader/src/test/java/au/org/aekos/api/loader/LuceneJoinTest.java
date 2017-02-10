package au.org.aekos.api.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

public class LuceneJoinTest {

	private static final String FAV_COLOUR = "fav_colour";
	private static final String FULLNAME_A = "fullnamea";
	private static final String FULLNAME_B = "fullnameb";
	private static final String EMPLOYEE = "employee";
	private static final String DOCTYPE = "doctype";
	private static final String JOHN_SMITH = "johnsmith";
	private IndexWriter writer;
	private IndexSearcher searcher;
	private SearcherManager searcherManager;
	private RAMDirectory termIndex;

	/**
	 * Can we get the lucene query-time join functionality to work?
	 */
	@Test
	public void testLuceneJoin01() throws IOException {
		createIndex();
		populateIndex();
		doJoin();
	}

	private void doJoin() throws IOException {
		String fromField = FULLNAME_A;
		boolean multipleValuesPerDocument = false;
		String toField = FULLNAME_B;
		ScoreMode scoreMode = ScoreMode.Max; // Defines how the scores are translated into the other side of the join.
		Query fromQuery = new TermQuery(new Term(DOCTYPE, EMPLOYEE)); // Query executed to collect from values to join to the to values
		TopDocs fromQueryTopDocs = searcher.search(fromQuery, 1);
		assertThat("fromQuery should find the employee record", fromQueryTopDocs.totalHits, is(1));
		Query joinQuery = JoinUtil.createJoinQuery(fromField, multipleValuesPerDocument, toField, fromQuery, searcher, scoreMode);
		TopDocs td = searcher.search(joinQuery, 2); // Note: toSearcher can be the same as the fromSearcher
		assertThat("Should find the 'likes' record", td.totalHits, is(1));
		Document matchedDoc = searcher.doc(td.scoreDocs[0].doc);
		assertThat(matchedDoc.getField(FAV_COLOUR).stringValue(), is("red"));
	}

	private void createIndex() throws IOException {
		termIndex = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE;
		config.setOpenMode(openMode);
		writer = new IndexWriter(termIndex, config);
	}
	
	private void populateIndex() throws IOException {
		Document employeeDoc = new Document();
		employeeDoc.add(new StringField(DOCTYPE, EMPLOYEE, Field.Store.YES));
		employeeDoc.add(new TextField(FULLNAME_A, JOHN_SMITH, Field.Store.YES));
		employeeDoc.add(new SortedDocValuesField(FULLNAME_A, new BytesRef(JOHN_SMITH)));
		employeeDoc.add(new TextField("department", "Accounting", Field.Store.YES));
		writer.addDocument(employeeDoc);
		Document likesDoc = new Document();
		likesDoc.add(new StringField(DOCTYPE, "likes", Field.Store.YES));
		likesDoc.add(new TextField(FULLNAME_B, JOHN_SMITH, Field.Store.YES));
		likesDoc.add(new SortedDocValuesField(FULLNAME_B, new BytesRef(JOHN_SMITH)));
		likesDoc.add(new TextField(FAV_COLOUR, "red", Field.Store.YES));
		writer.addDocument(likesDoc);
		writer.commit();
		writer.flush();
		writer.close();
		searcherManager = new SearcherManager(termIndex, new SearcherFactory());
		searcher = searcherManager.acquire();
	}
}
