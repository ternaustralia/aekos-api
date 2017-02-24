package au.org.aekos.api.loader.service;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.aekos.api.loader.service.load.IndexConstants;

public class SearchHelper {

	private static final Logger logger = LoggerFactory.getLogger(SearchHelper.class);
	private static final int FACET_VALUES_TO_RETURN = 100;

	/**
	 * Builds a query to find species records with the specified trait.
	 * 
	 * @param traitName	trait name to look for
	 * @return			query
	 */
	public static Query queryForSpeciesRecordTraitValue(String traitName) {
		return queryForRecordTypeAndSomeTerm(IndexConstants.DocTypes.SPECIES_RECORD, IndexConstants.FLD_TRAIT, traitName);
	}
	
	/**
	 * Builds a query to find env records with the specified variable.
	 * 
	 * @param variableName	name of variable to look for
	 * @return				query
	 */
	public static Query queryForEnvRecordVariableValue(String variableName) {
		return queryForRecordTypeAndSomeTerm(IndexConstants.DocTypes.ENV_RECORD, IndexConstants.FLD_ENVIRONMENT, variableName);
	}

	/**
	 * Executes a search for all species records and returns the facet values for the trait field.
	 * 
	 * @param searcher			searcher to execute against
	 * @return					facet values
	 */
	public static FacetResult facetSpeciesRecordTraits(IndexSearcher searcher) throws IOException, ParseException {
		Query q = new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, new StandardAnalyzer()).parse(IndexConstants.DocTypes.SPECIES_RECORD);
		return doFacetQueryForField(searcher, q, IndexConstants.FLD_TRAIT);
	}
	
	public static FacetResult facetEnvironmentRecordVariables(IndexSearcher searcher) throws IOException, ParseException {
		Query q = new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, new StandardAnalyzer()).parse(IndexConstants.DocTypes.ENV_RECORD);
		return doFacetQueryForField(searcher, q, IndexConstants.FLD_ENVIRONMENT);
	}

	static Query queryForRecordTypeAndSomeTerm(String docType, String fieldName, String term) {
		return new BooleanQuery.Builder()
				.add(new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, docType)), Occur.MUST)
				.add(new TermQuery(new Term(fieldName, term)), Occur.MUST)
				.build();
	}
	
	static FacetResult doFacetQueryForField(IndexSearcher searcher, Query q, String facetFieldName) throws IOException, ParseException {
		SortedSetDocValuesFacetCounts allFacets = doFacetQuery(searcher, q);
		FacetResult result = allFacets.getTopChildren(FACET_VALUES_TO_RETURN, facetFieldName);
		if (result == null) {
			String template = "Data problem: couldn't get facets for '%s' field with '%s' query";
			throw new IllegalStateException(String.format(template, facetFieldName, q.toString()));
		}
		return result;
	}
	
	static SortedSetDocValuesFacetCounts doFacetQuery(IndexSearcher searcher, Query q) throws IOException, ParseException {
		SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader());
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, q, 0, fc);
		return new SortedSetDocValuesFacetCounts(state, fc);
	}
	
	/**
	 * Intended mainly for debugging and testing. Dumps the first N docs to the logger.
	 * 
	 * @param searcher		searcher to use
	 * @param docsToDump	how many docs to dump
	 */
	public static void dumpFirstNDocs(IndexSearcher searcher, int docsToDump) {
		try {
			Query query = new MatchAllDocsQuery();
			TopDocs td = searcher.search(query, docsToDump);
			logger.info(String.format("DocsDump: index contains %d documents", td.totalHits));
			for (ScoreDoc curr : td.scoreDocs) {
				logger.info("DocsDump: " + searcher.doc(curr.doc).toString());
			}
			logger.info("DocsDump: end dump of documents");
		} catch (IOException e) {
			throw new RuntimeException("Failed to query the index", e);
		}
	}
}
