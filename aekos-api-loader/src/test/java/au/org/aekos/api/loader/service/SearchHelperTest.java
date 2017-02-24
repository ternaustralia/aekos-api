package au.org.aekos.api.loader.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import au.org.aekos.api.loader.service.load.IndexConstants;

public class SearchHelperTest {

	/**
	 * Is the query as we expect (including maintaining case)?
	 */
	@Test
	public void testQueryForRecordTypeAndSomeTerm01() {
		Query q = SearchHelper.queryForRecordTypeAndSomeTerm("someType", "fieldFoo", "averageHeight");
		assertThat(q.toString(), is("+doc_index_type:someType +fieldFoo:averageHeight"));
	}
	
	/**
	 * Can we find species records when they contain a certain trait?
	 */
	@Test
	public void testQueryForSpeciesRecordTraitValue01() throws Throwable {
		DirectoryReader indexReader = createIndex(
			speciesRecordDoc(
				stringField(IndexConstants.FLD_TRAIT, "averageHeight"),
				stringField(IndexConstants.FLD_TRAIT, "lifeForm"),
				stringField("otherField", "aaa")
			),
			speciesRecordDoc(
				stringField(IndexConstants.FLD_TRAIT, "averageHeight"),
				stringField(IndexConstants.FLD_TRAIT, "basalArea"),
				stringField("otherField", "bbb"))
		);
		Query q = SearchHelper.queryForSpeciesRecordTraitValue("averageHeight");
		IndexSearcher searcher = new IndexSearcher(indexReader);
		int onlyAfterTotalHits = 1;
		TopDocs td = searcher.search(q, onlyAfterTotalHits);
		assertThat("Should find both species", td.totalHits, is(2));
		indexReader.close();
	}
	
	/**
	 * Can we find env records when they contain a certain variable?
	 */
	@Test
	public void testQueryForEnvRecordVariableValue01() throws Throwable {
		DirectoryReader indexReader = createIndex(
			envRecordDoc(
				stringField(IndexConstants.FLD_ENVIRONMENT, "ph"),
				stringField(IndexConstants.FLD_ENVIRONMENT, "aspect"),
				stringField("otherField", "aaa")
			),
			envRecordDoc(
				stringField(IndexConstants.FLD_ENVIRONMENT, "erosionState"),
				stringField(IndexConstants.FLD_ENVIRONMENT, "aspect"),
				stringField("otherField", "bbb"))
		);
		Query q = SearchHelper.queryForEnvRecordVariableValue("aspect");
		IndexSearcher searcher = new IndexSearcher(indexReader);
		int onlyAfterTotalHits = 1;
		TopDocs r = searcher.search(new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.ENV_RECORD)), 2);
		for (ScoreDoc curr : r.scoreDocs) {
			System.out.println(searcher.doc(curr.doc));
		}
		TopDocs td = searcher.search(q, onlyAfterTotalHits);
		assertThat("Should find both records", td.totalHits, is(2));
		indexReader.close();
	}
	
	/**
	 * Can we find all available trait names and how many times they occur?
	 */
	@Test
	public void testFacetSpeciesRecordTraits01() throws Throwable {
		DirectoryReader indexReader = createIndex(
			speciesRecordDoc(
				facetedField(IndexConstants.FLD_TRAIT, "averageHeight"),
				facetedField(IndexConstants.FLD_TRAIT, "lifeForm"),
				facetedField("otherField", "aaa")
			),
			speciesRecordDoc(
				facetedField(IndexConstants.FLD_TRAIT, "averageHeight"),
				facetedField(IndexConstants.FLD_TRAIT, "basalArea"),
				facetedField("otherField", "bbb"))
		);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		FacetResult result = SearchHelper.facetSpeciesRecordTraits(searcher);
		assertThat("Should only have the 'trait' facet", result.dim, is(IndexConstants.FLD_TRAIT));
		assertThat("Expected these facets in this order", toEasyAssertStringList(result.labelValues),
				is(Arrays.asList("averageHeight|2", "basalArea|1", "lifeForm|1")));
		indexReader.close();
	}
	
	/**
	 * Can we find all available environmental variables and how many times they occur?
	 */
	@Test
	public void testFacetEnvironmentRecordVariables01() throws Throwable {
		DirectoryReader indexReader = createIndex(
			envRecordDoc(
				facetedField(IndexConstants.FLD_ENVIRONMENT, "ph"),
				facetedField(IndexConstants.FLD_ENVIRONMENT, "aspect"),
				facetedField("otherField", "aaa")
			),
			envRecordDoc(
				facetedField(IndexConstants.FLD_ENVIRONMENT, "erosionState"),
				facetedField(IndexConstants.FLD_ENVIRONMENT, "aspect"),
				facetedField("otherField", "bbb"))
		);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		FacetResult result = SearchHelper.facetEnvironmentRecordVariables(searcher);
		assertThat("Should only have the 'environment' facet", result.dim, is(IndexConstants.FLD_ENVIRONMENT));
		assertThat("Expected these facets in this order", toEasyAssertStringList(result.labelValues),
				is(Arrays.asList("aspect|2", "erosionState|1", "ph|1")));
		indexReader.close();
	}
	
	/**
	 * Can we facet a single field?
	 */
	@Test
	public void testDoFacetQueryForField01() throws Throwable {
		DirectoryReader indexReader = createIndex(
			speciesRecordDoc(
				facetedField(IndexConstants.FLD_TRAIT, "averageHeight"),
				facetedField(IndexConstants.FLD_TRAIT, "lifeForm"),
				facetedField("otherField", "aaa")
			),
			speciesRecordDoc(
				facetedField(IndexConstants.FLD_TRAIT, "averageHeight"),
				facetedField(IndexConstants.FLD_TRAIT, "basalArea"),
				facetedField("otherField", "bbb"))
		);
	    IndexSearcher searcher = new IndexSearcher(indexReader);
		Query q = new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, new StandardAnalyzer()).parse(IndexConstants.DocTypes.SPECIES_RECORD);
		FacetResult result = SearchHelper.doFacetQueryForField(searcher, q, IndexConstants.FLD_TRAIT);
		assertThat("Should only have the 'trait' facet", result.dim, is(IndexConstants.FLD_TRAIT));
		assertThat("Expected all these facets in this order", toEasyAssertStringList(result.labelValues),
				is(Arrays.asList("averageHeight|2", "basalArea|1", "lifeForm|1")));
		indexReader.close();
	}
	
	/**
	 * Can we facet all fields?
	 */
	@Test
	public void testDoFacetQuery01() throws Throwable {
		DirectoryReader indexReader = createIndex(
			speciesRecordDoc(
				facetedField(IndexConstants.FLD_TRAIT, "averageHeight"),
				facetedField(IndexConstants.FLD_TRAIT, "lifeForm"),
				facetedField("otherField", "aaa")
			),
			speciesRecordDoc(
				facetedField(IndexConstants.FLD_TRAIT, "averageHeight"),
				facetedField(IndexConstants.FLD_TRAIT, "basalArea"),
				facetedField("otherField", "bbb"))
		);
	    IndexSearcher searcher = new IndexSearcher(indexReader);
		Query q = new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, new StandardAnalyzer()).parse(IndexConstants.DocTypes.SPECIES_RECORD);
		SortedSetDocValuesFacetCounts result = SearchHelper.doFacetQuery(searcher, q);
		List<FacetResult> allDims = result.getAllDims(10);
		assertThat("Should have all faceted fields present", allDims.size(), is(2));
		assertThat("Expected all these fields in this order", allDims.stream()
				.map(e -> e.dim + "|" + e.value)
				.collect(Collectors.toList()),
				is(Arrays.asList("trait|4", "otherField|2")));
		FacetResult traitFacetResult = allDims.get(0);
		assertThat("Should be the 'trait' field facets", traitFacetResult.dim, is(IndexConstants.FLD_TRAIT));
		assertThat("Expected all these facets in this order", toEasyAssertStringList(traitFacetResult.labelValues),
				is(Arrays.asList("averageHeight|2", "basalArea|1", "lifeForm|1")));
		FacetResult otherFieldFacetResult = allDims.get(1);
		assertThat("Should be the 'trait' field facets", otherFieldFacetResult.dim, is("otherField"));
		assertThat("Expected all these facets in this order", toEasyAssertStringList(otherFieldFacetResult.labelValues),
				is(Arrays.asList("aaa|1", "bbb|1")));
		indexReader.close();
	}
	
	private List<String> toEasyAssertStringList(LabelAndValue[] labelValues) {
		return Arrays.stream(labelValues)
				.map(e -> e.label + "|" + e.value)
				.collect(Collectors.toList());
	}

	private DirectoryReader createIndex(Document...speciesRecordDoc) throws Throwable {
		Directory indexDir = new RAMDirectory();
		FacetsConfig config = new FacetsConfig();
		config.setMultiValued(IndexConstants.FLD_TRAIT, true);
		config.setMultiValued(IndexConstants.FLD_ENVIRONMENT, true);
		IndexWriter indexWriter = new IndexWriter(indexDir, new IndexWriterConfig(
		        new WhitespaceAnalyzer()).setOpenMode(OpenMode.CREATE));
		for (Document curr : speciesRecordDoc) {
			indexWriter.addDocument(config.build(curr));
		}
	    indexWriter.close();
		return DirectoryReader.open(indexDir);
	}

	private Document speciesRecordDoc(Field...fields) {
		Document result = new Document();
		result.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.SPECIES_RECORD, Field.Store.YES));
		for (Field curr : fields) {
			result.add(curr);
		}
		return result;
	}
	
	private Document envRecordDoc(Field...fields) {
		Document result = new Document();
		result.add(new StringField(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.ENV_RECORD, Field.Store.YES));
		for (Field curr : fields) {
			result.add(curr);
		}
		return result;
	}

	private Field stringField(String fieldName, String value) {
		return new StringField(fieldName, value, Field.Store.YES);
	}
	
	private Field facetedField(String fieldName, String value) {
		return new SortedSetDocValuesFacetField(fieldName, value);
	}
}
