package au.org.aekos.service.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocValuesTermsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitVocabEntry;
import au.org.aekos.service.search.index.DocumentType;
import au.org.aekos.service.search.index.IndexConstants;
import au.org.aekos.service.search.index.SpeciesLookupIndexService;
import au.org.aekos.service.search.index.TermIndexManager;

@Service
public class LuceneSearchService implements SearchService {

	private static final Logger logger = LoggerFactory.getLogger(LuceneSearchService.class);
	
	@Autowired
	private SpeciesLookupIndexService speciesSearchService;
	
	@Autowired
	private TermIndexManager termIndexManager;
	//Max of 1024 species names??  or split into 2 or more queries . . . . 
	
	@Override //TODO throw a nice exception to perhaps return a tidy error in the json response?
	public List<TraitVocabEntry> getTraitBySpecies(List<String> speciesNames) {
		Query query = buildFieldOrQuery(speciesNames, IndexConstants.FLD_SPECIES, DocumentType.TRAIT_SPECIES);
		return performSpeciesTraitSearch(query);
	}
	
	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames) {
		Query query = buildFieldOrQuery(traitNames, IndexConstants.FLD_TRAIT, DocumentType.TRAIT_SPECIES);
		return performTraitSpeciesSearch(query );
	}
	
	@Override
	public List<EnvironmentVariable> getEnvironmentBySpecies(List<String> speciesNames) {
		Query query = buildFieldOrQuery(speciesNames, IndexConstants.FLD_SPECIES, DocumentType.SPECIES_ENV);
		return performSpeciesEnvironmentSearch(query);
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
	
	private List<TraitVocabEntry> performSpeciesTraitSearch(Query query ){
		List<TraitVocabEntry> responseList = new ArrayList<TraitVocabEntry>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search trait by species - IOException - returning empty list", e);
			return responseList;
		}
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE, new Sort(new SortField(IndexConstants.FLD_TRAIT, SortField.Type.STRING)));
		    int totalTraits = td.totalHits;
		    if(td.totalHits > 0){
		    	LinkedHashSet<TraitVocabEntry> uniqueResults = new LinkedHashSet<>();
		    	for(ScoreDoc scoreDoc : td.scoreDocs){
		    		Document matchedDoc = searcher.doc(scoreDoc.doc);
		    		String trait = matchedDoc.get(IndexConstants.FLD_TRAIT);
		    		if(StringUtils.hasLength(trait)){
		    			uniqueResults.add(new TraitVocabEntry(trait, trait));
		    		}
		    	}
		    	responseList.addAll(uniqueResults);
		    }
		    termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseList;
	}
	
	private List<SpeciesName> performTraitSpeciesSearch(Query query ){
		List<SpeciesName> responseList = new ArrayList<SpeciesName>();
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
		    if(td.totalHits > 0){
		    	LinkedHashSet<SpeciesName> uniqueResults = new LinkedHashSet<>();
		    	for(ScoreDoc scoreDoc : td.scoreDocs){
		    		Document matchedDoc = searcher.doc(scoreDoc.doc);
		    		String speciesName = matchedDoc.get(IndexConstants.FLD_SPECIES);
		    		if(StringUtils.hasLength(speciesName)){
		    			uniqueResults.add(new SpeciesName(speciesName));
		    		}
		    	}
		    	responseList.addAll(uniqueResults);
		    }
		    termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseList;
	}
	
	private List<EnvironmentVariable> performSpeciesEnvironmentSearch(Query query ){
		List<EnvironmentVariable> responseList = new ArrayList<EnvironmentVariable>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search trait by species - IOException - returning empty list", e);
			return responseList;
		}
		
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE, new Sort(new SortField(IndexConstants.FLD_ENVIRONMENT, SortField.Type.STRING)));
		    int totalTraits = td.totalHits;
		    if(td.totalHits > 0){
		    	LinkedHashSet<EnvironmentVariable> uniqueResults = new LinkedHashSet<>();
		    	for(ScoreDoc scoreDoc : td.scoreDocs){
		    		Document matchedDoc = searcher.doc(scoreDoc.doc);
		    		String environment = matchedDoc.get(IndexConstants.FLD_ENVIRONMENT);
		    		if(StringUtils.hasLength(environment)){
		    			uniqueResults.add(new EnvironmentVariable(environment, environment));
		    		}
		    	}
		    	responseList.addAll(uniqueResults);
		    }
			termIndexManager.releaseIndexSearcher(searcher);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseList;
	}
	
	
	@Override
	public List<SpeciesName> autocompleteSpeciesName(String partialSpeciesName) {
		try {
			return speciesSearchService.performSearch(partialSpeciesName, 50, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override //TODO What is this one??
	public List<TraitVocabEntry> getTraitVocabData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<SpeciesSummary> getSpeciesSummary(List<String> speciesNames) {
		// TODO Auto-generated method stub
		return null;
	}
}
