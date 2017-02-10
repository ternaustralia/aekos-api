package au.org.aekos.api.service.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
	public List<TraitOrEnvironmentalVariableVocabEntry> getTraitBySpecies(List<String> SpeciesSummarys, PageRequest pagination) {
		Query query = buildFieldOrQuery(SpeciesSummarys, IndexConstants.FLD_SPECIES, DocumentType.TRAIT_SPECIES);
		return performSpeciesTraitSearch(query, pagination);
	}
	
	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames,PageRequest pagination) {
		Query query = buildFieldOrQuery(traitNames, IndexConstants.FLD_TRAIT, DocumentType.TRAIT_SPECIES);
		return performTraitSpeciesSearch(query, pagination );
	}
	
	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentBySpecies(List<String> SpeciesSummarys,PageRequest pagination) {
		Query query = buildFieldOrQuery(SpeciesSummarys, IndexConstants.FLD_SPECIES, DocumentType.SPECIES_ENV);
		return performSpeciesEnvironmentSearch(query, pagination);
	}

	@Override
	public List<SpeciesSummary> speciesAutocomplete(String term, int numResults) throws IOException{
		List<SpeciesSummary> primaryResults = new ArrayList<SpeciesSummary>();
		List<SpeciesSummary> secondaryResults = new ArrayList<SpeciesSummary>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't do species autocomplete - IOException - returning empty list", e);
			return primaryResults;
		}
		String searchTerm = term.toLowerCase().replace(" ", "").replace(".", "");
		Query q = new PrefixQuery(new Term(IndexConstants.FLD_SEARCH, searchTerm ));
		Query q2 = new PrefixQuery(new Term(IndexConstants.FLD_SEARCH_SUB, searchTerm ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField(IndexConstants.FLD_SEARCH, SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					String speciesName = d.getField(IndexConstants.FLD_DISPLAY_VALUE).stringValue();
					int count = Integer.parseInt(d.get(IndexConstants.FLD_INSTANCE_COUNT));
					primaryResults.add(new SpeciesSummary(speciesName, count));
				}
			}
		}
		TopDocs td2 = searcher.search(q2, numResults, new Sort(new SortField(IndexConstants.FLD_SEARCH, SortField.Type.STRING)));
		if( td2.totalHits > 0 ){
			for(ScoreDoc sd :td2.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					String speciesName = d.getField(IndexConstants.FLD_DISPLAY_VALUE).stringValue();
					int count = Integer.parseInt(d.get(IndexConstants.FLD_INSTANCE_COUNT));
					secondaryResults.add(new SpeciesSummary(speciesName, count));
				}
			}
		}
		List<SpeciesSummary> resultList ;
		if(td.totalHits == 0 && td2.totalHits == 0 ){
			resultList = doWildcardQuery(searchTerm, numResults, searcher );
		}else{
		    resultList = mergePrimaryAndSecondaryResults(primaryResults, secondaryResults, numResults, 30, 20);
		}
		return Collections.unmodifiableList(resultList);
	}
	
	private List<SpeciesSummary> doWildcardQuery(String searchTerm, int numResults, IndexSearcher searcher) throws IOException{
		List<SpeciesSummary> result = new ArrayList<SpeciesSummary>();
		Query q = new WildcardQuery(new Term("search", "*" + searchTerm + "*" ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField("search", SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					String speciesName = d.getField(IndexConstants.FLD_DISPLAY_VALUE).stringValue();
					int count = Integer.parseInt(d.get(IndexConstants.FLD_INSTANCE_COUNT));
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
		Query docTypeQuery = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, documentType.name()));
		queryBuilder.add(docTypeQuery, Occur.MUST);
		queryBuilder.setMinimumNumberShouldMatch(1); //For optional field matches- i.e. the OR condition
		for(String fieldString : fieldStrings ){
			Query q = new TermQuery(new Term(searchField, fieldString));
			queryBuilder.add(q, Occur.SHOULD);
		}
		Query query = queryBuilder.build();
		return query;
	}
	
	private List<TraitOrEnvironmentalVariableVocabEntry> performSpeciesTraitSearch(Query query, PageRequest pagination){
		List<TraitOrEnvironmentalVariableVocabEntry> responseList = new ArrayList<>();
		IndexSearcher searcher = null;
		PageResultMetadata pageMeta = new PageResultMetadata();
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search trait by species - IOException - returning empty list", e);
			return responseList;
		}
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE, new Sort(new SortField(IndexConstants.FLD_TRAIT, SortField.Type.STRING)));
			pageMeta.totalResults = td.totalHits;
		    if(td.totalHits > 0){
		    	LinkedHashSet<TraitOrEnvironmentalVariableVocabEntry> uniqueResults = new LinkedHashSet<>();
		    	int startDocIndex = getTopDocStartIndex(pagination, td.totalHits);
		    	if(startDocIndex > -1){
		    		int endDocIndex = getTopDocEndIndex(pagination, td.totalHits);
		    		for(int x = startDocIndex; x <= endDocIndex; x++ ){
			    	    ScoreDoc scoreDoc = td.scoreDocs[x];
			    		Document matchedDoc = searcher.doc(scoreDoc.doc);
			    		String trait = matchedDoc.get(IndexConstants.FLD_TRAIT);
			    		String title = vocabService.getLabelForPropertyCode(trait);
			    		if(StringUtils.hasLength(trait)){
			    			uniqueResults.add(new TraitOrEnvironmentalVariableVocabEntry(trait, title));
			    		}
			    	}
		    	}
		    	responseList.addAll(uniqueResults);
		    }
		    termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			logger.error("Failed to query index", e);
		}
		return responseList;
	}
	
	private List<SpeciesName> performTraitSpeciesSearch(Query query, PageRequest pagination){
		List<SpeciesName> responseList = new ArrayList<>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search trait by species - IOException - returning empty list", e);
			return responseList;
		}
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE, new Sort(new SortField(IndexConstants.FLD_SPECIES, SortField.Type.STRING)));
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
		    	responseList.addAll(uniqueResults);
		    }
		    termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			logger.error("Failed to query index", e);
		}
		return responseList;
	}
	
	private List<TraitOrEnvironmentalVariableVocabEntry> performSpeciesEnvironmentSearch(Query query, PageRequest pagination ){
		List<TraitOrEnvironmentalVariableVocabEntry> responseList = new ArrayList<TraitOrEnvironmentalVariableVocabEntry>();
		IndexSearcher searcher = null;
		
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search species by environment - IOException - returning empty list", e);
			return responseList;
		}
		
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE, new Sort(new SortField(IndexConstants.FLD_ENVIRONMENT, SortField.Type.STRING)));
		    int totalTraits = td.totalHits;
		    if(totalTraits > 0){
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
		    	responseList.addAll(uniqueResults);
		    }
			termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			logger.error("Failed to query index", e);
		}
		return responseList;
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
		Query allTraitToSpeciesDocumentsQuery = buildAllDocumentsOfTypeQuery(DocumentType.TRAIT_SPECIES);
		return performSpeciesTraitSearch(allTraitToSpeciesDocumentsQuery, EVERYTHING);
	}

	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentalVariableVocabData() {
		Query allEnvVarToSpeciesDocumentsQuery = buildAllDocumentsOfTypeQuery(DocumentType.SPECIES_ENV);
		return performSpeciesEnvironmentSearch(allEnvVarToSpeciesDocumentsQuery, EVERYTHING);
	}
	
	private Query buildAllDocumentsOfTypeQuery(DocumentType documentType) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		Query docTypeQuery = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, documentType.getCode()));
		queryBuilder.add(docTypeQuery, Occur.MUST);
		Query allTraitToSpeciesDocumentsQuery = queryBuilder.build();
		return allTraitToSpeciesDocumentsQuery;
	}
}
