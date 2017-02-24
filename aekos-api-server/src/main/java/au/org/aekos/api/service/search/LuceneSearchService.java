package au.org.aekos.api.service.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import au.org.aekos.api.loader.service.SearchHelper;
import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.load.DocumentType;
import au.org.aekos.api.loader.service.load.IndexConstants;
import au.org.aekos.api.model.SpeciesName;
import au.org.aekos.api.model.SpeciesSummary;
import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.api.service.vocab.VocabService;

@Service
public class LuceneSearchService implements SearchService {

	//Can also pass in a null page request!!
	static final PageRequest EVERYTHING = new PageRequest(0, Integer.MAX_VALUE);

	private static final Logger logger = LoggerFactory.getLogger(LuceneSearchService.class);
	
	@Autowired
	private TermIndexManager termIndexManager;
	//Max of 1024 species names??  or split into 2 or more queries . . . .
	
	@Value("${lucene.page.defaultResutsPerPage}")
	private int defaultResultsPerPage = 100;
	
	@Autowired
	private VocabService vocabService;
	
	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getTraitBySpecies(List<String> speciesNames, PageRequest pagination) {
		List<String> lowerCasedSpeciesNames = speciesNames.stream().map(e -> e.toLowerCase()).collect(Collectors.toList());
		Query query = buildFieldOrQuery(lowerCasedSpeciesNames, IndexConstants.FLD_SPECIES, DocumentType.SPECIES_RECORD);
		return performSpeciesTraitSearch(query, pagination);
	}
	
	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames,PageRequest pagination) {
		Query query = buildFieldOrQuery(traitNames, IndexConstants.FLD_TRAIT, DocumentType.SPECIES_RECORD);
		return performTraitSpeciesSearch(query, pagination);
	}
	
	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentBySpecies(List<String> speciesNames, PageRequest pagination) {
		return performSpeciesEnvironmentSearch(speciesNames, pagination);
	}

	@Override
	public List<SpeciesSummary> speciesAutocomplete(String term, int numResults) throws IOException{
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't do species autocomplete - IOException - returning empty list", e);
			return Collections.emptyList();
		}
		List<SpeciesSummary> primaryResults = new ArrayList<SpeciesSummary>();
		String searchTerm = term.toLowerCase().replace(" ", "").replace(".", "");
		TopDocs td = doPrimaryAutocomplete(numResults, searcher, primaryResults, searchTerm);
		List<SpeciesSummary> secondaryResults = new ArrayList<SpeciesSummary>();
		TopDocs td2 = doSecondaryAutocomplete(numResults, searcher, searchTerm, secondaryResults);
		if(td.totalHits == 0 && td2.totalHits == 0 ){
			return Collections.unmodifiableList(doWildcardQuery(searchTerm, numResults, searcher));
		}
		return Collections.unmodifiableList(mergePrimaryAndSecondaryResults(primaryResults, secondaryResults, numResults, 30, 20));
	}

	private TopDocs doPrimaryAutocomplete(int numResults, IndexSearcher searcher, List<SpeciesSummary> primaryResults, String searchTerm) throws IOException {
		Query q = new PrefixQuery(new Term(IndexConstants.FLD_SEARCH, searchTerm));
		SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader());
		FacetsCollector fc = new FacetsCollector();
		TopDocs result = FacetsCollector.search(searcher, q, numResults, new Sort(new SortField(IndexConstants.FLD_SEARCH, SortField.Type.STRING)), fc);
		if( result.totalHits > 0 ){
			SortedSetDocValuesFacetCounts facets = new SortedSetDocValuesFacetCounts(state, fc);
			FacetResult speciesFacet = facets.getTopChildren(numResults, IndexConstants.FLD_SPECIES); // TODO is 'numResults' enough? Is it possible to have the result but not the facet?
			Map<String, Number> facetMap = Arrays.stream(speciesFacet.labelValues).collect(Collectors.toMap(e -> e.label, e -> e.value));
			for(ScoreDoc sd :result.scoreDocs){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					String speciesName = d.getField(IndexConstants.FLD_SPECIES).stringValue();
					int count = facetMap.get(speciesName).intValue();
					primaryResults.add(new SpeciesSummary(speciesName, count));
				}
			}
		}
		return result;
	}
	
	private TopDocs doSecondaryAutocomplete(int numResults, IndexSearcher searcher, String searchTerm, List<SpeciesSummary> secondaryResults)
			throws IOException {
		Query q = new PrefixQuery(new Term(IndexConstants.FLD_SEARCH_SUB, searchTerm));
		SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader());
		FacetsCollector fc = new FacetsCollector();
		TopDocs result = FacetsCollector.search(searcher, q, numResults, new Sort(new SortField(IndexConstants.FLD_SEARCH, SortField.Type.STRING)), fc);
		if( result.totalHits > 0 ){
			SortedSetDocValuesFacetCounts facets = new SortedSetDocValuesFacetCounts(state, fc);
			FacetResult speciesFacet = facets.getTopChildren(numResults, IndexConstants.FLD_SPECIES); // TODO is 'numResults' enough? Is it possible to have the result but not the facet?
			Map<String, Number> facetMap = Arrays.stream(speciesFacet.labelValues).collect(Collectors.toMap(e -> e.label, e -> e.value));
			for(ScoreDoc sd :result.scoreDocs){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					String speciesName = d.getField(IndexConstants.FLD_SPECIES).stringValue();
					int count = facetMap.get(speciesName).intValue();
					secondaryResults.add(new SpeciesSummary(speciesName, count));
				}
			}
		}
		return result;
	}
	
	private List<SpeciesSummary> doWildcardQuery(String searchTerm, int numResults, IndexSearcher searcher) throws IOException{
		List<SpeciesSummary> result = new ArrayList<SpeciesSummary>();
		Query q = new WildcardQuery(new Term(IndexConstants.FLD_SEARCH, "*" + searchTerm + "*" ));
		SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader());
		FacetsCollector fc = new FacetsCollector();
		TopDocs td = FacetsCollector.search(searcher, q, numResults, new Sort(new SortField(IndexConstants.FLD_SEARCH, SortField.Type.STRING)), fc);
		SortedSetDocValuesFacetCounts facets = new SortedSetDocValuesFacetCounts(state, fc);
		FacetResult speciesFacet = facets.getTopChildren(numResults, IndexConstants.FLD_SPECIES); // TODO is 'numResults' enough? Is it possible to have the result but not the facet?
		Map<String, Number> facetMap = Arrays.stream(speciesFacet.labelValues).collect(Collectors.toMap(e -> e.label, e -> e.value));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					String speciesName = d.getField(IndexConstants.FLD_SPECIES).stringValue();
					int count = facetMap.get(speciesName).intValue();
					result.add(new SpeciesSummary(speciesName, count));
				}
			}
		}
		return result;
	}
	
	private List<SpeciesSummary> mergePrimaryAndSecondaryResults(List<SpeciesSummary> tvListPrimary, List<SpeciesSummary> tvListSecondary, int numResults, int numPrimaryFirst, int numSecondary){
		if(tvListSecondary.size() == 0){
			return tvListPrimary;
		}
		if(tvListPrimary.size() == 0){
			return tvListSecondary;
		}
		
		Set<SpeciesSummary> tvSet = new LinkedHashSet<SpeciesSummary>();
		
		int primSz = tvListPrimary.size();
		int secSz = tvListSecondary.size();
		
		//Lets load up the linked hash set -
		//Add 'numPrimaryFirst' primaries
		//Add numSecondary secondaries
		//Fill up with primaries, if we run out, fill up with secondaries.
		for(int x = 0 ; x < primSz ; x++ ){
			tvSet.add(tvListPrimary.get(x));
			if(x == numPrimaryFirst - 1 ){
				break;
			}
		}
		//Now try and add 5 secondaries
		for(int y = 0 ; y < secSz ; y++ ){
			tvSet.add(tvListSecondary.get(y));
			if(y == numSecondary - 1){
				break;
			}
		}
		if(tvListPrimary.size() > numPrimaryFirst){
			for(int x = numPrimaryFirst; x < tvListPrimary.size(); x++){
				tvSet.add(tvListPrimary.get(x));
				if(tvSet.size() == numResults){
					break;
				}
			}
		}
		if(tvSet.size() < numResults && tvListSecondary.size() > numSecondary){
			for(int y = numSecondary; y < tvListSecondary.size() ; y ++) {
				tvSet.add(tvListSecondary.get(y));
				if(tvSet.size() == numResults){
					break;
				}
			}
		}
		List<SpeciesSummary> result = new ArrayList<SpeciesSummary>();
		result.addAll(tvSet);
		return result;
	}
	
	private Query buildFieldOrQuery(List<String> fieldStrings, String searchField, DocumentType documentType) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		Query docTypeQuery = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, documentType.name().toLowerCase()));
		queryBuilder.add(docTypeQuery, Occur.MUST);
		queryBuilder.setMinimumNumberShouldMatch(1); //For optional field matches- i.e. the OR condition
		for(String fieldString : fieldStrings) {
			Query q = new TermQuery(new Term(searchField, fieldString));
			queryBuilder.add(q, Occur.SHOULD);
		}
		return queryBuilder.build();
	}
	
	private List<TraitOrEnvironmentalVariableVocabEntry> performSpeciesTraitSearch(Query query, PageRequest pagination){
		List<TraitOrEnvironmentalVariableVocabEntry> result = new ArrayList<>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			throw new RuntimeException("Can't access index", e);
		}
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE); // TODO add sorting back
		    if (td.totalHits == 0) {
		    	return Collections.emptyList();
		    }
		    Set<TraitOrEnvironmentalVariableVocabEntry> uniqueResults = new LinkedHashSet<>();
		    int startDocIndex = getTopDocStartIndex(pagination, td.totalHits);
		    if (startDocIndex == -1) {
		    	return Collections.emptyList();
		    }
		    int endDocIndex = getTopDocEndIndex(pagination, td.totalHits);
		    for(int x = startDocIndex; x <= endDocIndex; x++ ){
		    	ScoreDoc scoreDoc = td.scoreDocs[x];
		    	Document matchedDoc = searcher.doc(scoreDoc.doc);
		    	IndexableField[] traits = matchedDoc.getFields(IndexConstants.FLD_TRAIT);
		    	for (IndexableField curr : traits) {
		    		String trait = curr.stringValue();
		    		String title = vocabService.getLabelForPropertyCode(trait);
		    		if(StringUtils.hasLength(trait)){
		    			uniqueResults.add(new TraitOrEnvironmentalVariableVocabEntry(trait, title));
		    		}
		    	}
		    }
		    result.addAll(uniqueResults);
		} catch (IOException e) {
			throw new RuntimeException("Failed to query index", e);
		} finally {
			try {
				termIndexManager.releaseIndexSearcher(searcher);
			} catch (IOException e) {
				throw new RuntimeException("Failed to release the searcher", e);
			}
		}
		return result;
	}
	
	private List<SpeciesName> performTraitSpeciesSearch(Query query, PageRequest pagination){
		List<SpeciesName> result = new ArrayList<>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search trait by species - IOException - returning empty list", e);
			return result;
		}
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE); // TODO add sorting back
		    int totalSpecies = td.totalHits;
		    if(totalSpecies > 0){
		    	Set<SpeciesName> uniqueResults = new LinkedHashSet<>();
		    	int startDocIndex = getTopDocStartIndex(pagination, td.totalHits);
		    	if(startDocIndex > -1){
		    		int endDocIndex = getTopDocEndIndex(pagination, td.totalHits);
		    		for(int x = startDocIndex; x <= endDocIndex; x++ ){
			    	    ScoreDoc scoreDoc = td.scoreDocs[x];
			    		Document matchedDoc = searcher.doc(scoreDoc.doc);
			    		String speciesName = matchedDoc.get(IndexConstants.FLD_SPECIES);
			    		if(StringUtils.hasLength(speciesName)){
			    			uniqueResults.add(new SpeciesName(speciesName));
			    		}
			    	}
		    	}
		    	result.addAll(uniqueResults);
		    }
		    termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			logger.error("Failed to query index", e);
		}
		return result;
	}
	
	private List<TraitOrEnvironmentalVariableVocabEntry> performSpeciesEnvironmentSearch(List<String> speciesNames, PageRequest pagination){
		boolean multipleValuesPerDocument = false;
		ScoreMode scoreMode = ScoreMode.Max; // Defines how the scores are translated into the other side of the join.
String FIXMEfirstname = speciesNames.get(0); // FIXME iterate to add all
		Query fromQuery = new BooleanQuery.Builder()
				.add(new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.SPECIES_RECORD)), BooleanClause.Occur.MUST)
				.add(new TermQuery(new Term(IndexConstants.FLD_SPECIES, FIXMEfirstname)), BooleanClause.Occur.MUST)
				.build();
		IndexSearcher searcher = null;
		List<TraitOrEnvironmentalVariableVocabEntry> result = new ArrayList<TraitOrEnvironmentalVariableVocabEntry>();
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search species by environment - IOException - returning empty list", e);
			return result;
		}
		try {
//TopDocs fromQueryTopDocs = searcher.search(fromQuery, 1);
//assertThat("fromQuery should find the employee record", fromQueryTopDocs.totalHits, is(1));
//SearchHelper.dumpFirstNDocs(searcher, 10);
			Query joinQuery = JoinUtil.createJoinQuery(IndexConstants.FLD_JOIN_KEY, multipleValuesPerDocument,
					IndexConstants.FLD_JOIN_KEY+"a", fromQuery, searcher, scoreMode);
			TopDocs td = searcher.search(joinQuery, 2);// FIXME Integer.MAX_VALUE); // TODO add sorting
		    int totalHits = td.totalHits;
//assertThat(totalHits, is(2));
		    if(totalHits > 0){
		    	Set<TraitOrEnvironmentalVariableVocabEntry> uniqueResults = new LinkedHashSet<>();
		    	int startDocIndex = getTopDocStartIndex(pagination, td.totalHits);
		    	if(startDocIndex > -1){
		    		int endDocIndex = getTopDocEndIndex(pagination, td.totalHits);
		    		for(int x = startDocIndex; x <= endDocIndex; x++ ){
			    	    ScoreDoc scoreDoc = td.scoreDocs[x];
			    		Document matchedDoc = searcher.doc(scoreDoc.doc);
			    		String environmentalVariable = matchedDoc.get(IndexConstants.FLD_ENVIRONMENT);
			    		String title = vocabService.getLabelForPropertyCode(environmentalVariable);
			    		if(StringUtils.hasLength(environmentalVariable)){
			    			uniqueResults.add(new TraitOrEnvironmentalVariableVocabEntry(environmentalVariable, title));
			    		}
			    	}
		    	}
		    	result.addAll(uniqueResults);
		    }
			termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			logger.error("Failed to query index", e);
		}
		return result;
	}
	
	/**
	 * If requested pageNumber * resultsPerPage > totalHits return -1
	 * @param pagination
	 * @param totalHits
	 * @return startIndex or -1 to signify return nothing
	 */
	int getTopDocStartIndex(PageRequest pagination, int totalHits){
		if(pagination == null){
			return 0;
		}
		int pageNumber = pagination.getPageNumber();
		if(pageNumber == -1 || pageNumber == 0 || pageNumber == 1){
			return 0;
		}
		int resultsPerPage = pagination.getResultsPerPage() > 0 ?  pagination.getResultsPerPage() : defaultResultsPerPage;
		int startIndex = ( pageNumber - 1 ) * resultsPerPage;
		if(startIndex >= totalHits){
			return -1;
		}
	    return startIndex;
	}
	
	int getTopDocEndIndex(PageRequest pagination, int totalHits){
		if(pagination == null ){ //Still might like to cap the everything query at a number of docs.
			return totalHits - 1;
		}
		int pageNumber = pagination.getPageNumber();
		if(pageNumber == -1 ){
			return -1;
		}
	    if(pageNumber == 0){ //zero index just in case
	        pageNumber = 1;
	    }
	    int resultsPerPage = pagination.getResultsPerPage() > 0 ?  pagination.getResultsPerPage() : defaultResultsPerPage;
		int endIndex = (pageNumber * resultsPerPage) - 1;
		if(endIndex > totalHits - 1){
			endIndex = totalHits - 1; //If pagination too far startIndex should already be -1
		}
		return endIndex;
	}
	
	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getTraitVocabData() {
		try {
			IndexSearcher searcher = termIndexManager.getIndexSearcher();
			FacetResult traitFacet = SearchHelper.facetSpeciesRecordTraits(searcher);
			List<TraitOrEnvironmentalVariableVocabEntry> result = new LinkedList<>();
			for (LabelAndValue curr : traitFacet.labelValues) {
				String traitCode = curr.label;
				String title = vocabService.getLabelForPropertyCode(traitCode);
				result.add(new TraitOrEnvironmentalVariableVocabEntry(traitCode, title));
				// TODO add count
			}
			return result;
		} catch (IOException | ParseException e) {
			throw new RuntimeException("Index problem: failed to query the index", e);
		}
	}

	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentalVariableVocabData() {
		try {
			IndexSearcher searcher = termIndexManager.getIndexSearcher();
			FacetResult envVarsFacet = SearchHelper.facetEnvironmentRecordVariables(searcher);
			List<TraitOrEnvironmentalVariableVocabEntry> result = new LinkedList<>();
			for (LabelAndValue curr : envVarsFacet.labelValues) {
				String traitCode = curr.label;
				String title = vocabService.getLabelForPropertyCode(traitCode);
				result.add(new TraitOrEnvironmentalVariableVocabEntry(traitCode, title));
				// TODO add count
			}
			return result;
		} catch (IOException | ParseException e) {
			throw new RuntimeException("Index problem: failed to query the index", e);
		}
	}

	public void setTermIndexManager(TermIndexManager termIndexManager) {
		this.termIndexManager = termIndexManager;
	}

	public void setVocabService(VocabService vocabService) {
		this.vocabService = vocabService;
	}
}
