package au.org.aekos.service.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.service.search.index.DocumentType;
import au.org.aekos.service.search.index.IndexConstants;
import au.org.aekos.service.search.index.SpeciesLookupIndexService;
import au.org.aekos.service.search.index.TermIndexManager;
import au.org.aekos.service.vocab.VocabService;

@Service
public class LuceneSearchService implements SearchService {

	private static final PageRequest EVERYTHING = new PageRequest(0, Integer.MAX_VALUE);

	private static final Logger logger = LoggerFactory.getLogger(LuceneSearchService.class);
	
	@Autowired
	private SpeciesLookupIndexService speciesSearchService;
	
	@Autowired
	private TermIndexManager termIndexManager;
	//Max of 1024 species names??  or split into 2 or more queries . . . . 
	
	@Autowired
	private VocabService vocabService;
	
	@Override //TODO throw a nice exception to perhaps return a tidy error in the json response?
	public List<TraitOrEnvironmentalVariableVocabEntry> getTraitBySpecies(List<String> speciesNames, PageRequest pagination) {
		Query query = buildFieldOrQuery(speciesNames, IndexConstants.FLD_SPECIES, DocumentType.TRAIT_SPECIES);
		return performSpeciesTraitSearch(query, pagination);
	}
	
	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames,PageRequest pagination) {
		Query query = buildFieldOrQuery(traitNames, IndexConstants.FLD_TRAIT, DocumentType.TRAIT_SPECIES);
		return performTraitSpeciesSearch(query, pagination );
	}
	
	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentBySpecies(List<String> speciesNames,PageRequest pagination) {
		Query query = buildFieldOrQuery(speciesNames, IndexConstants.FLD_SPECIES, DocumentType.SPECIES_ENV);
		return performSpeciesEnvironmentSearch(query, pagination);
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
		    	for(ScoreDoc scoreDoc : td.scoreDocs){
		    		Document matchedDoc = searcher.doc(scoreDoc.doc);
		    		String trait = matchedDoc.get(IndexConstants.FLD_TRAIT);
		    		String title = vocabService.getLabelForPropertyCode(trait);
		    		if(StringUtils.hasLength(trait)){
		    			uniqueResults.add(new TraitOrEnvironmentalVariableVocabEntry(trait, title));
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
	
	private List<SpeciesName> performTraitSpeciesSearch(Query query, PageRequest pagination ){
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
		    if(totalSpecies > 0){
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
		    	LinkedHashSet<TraitOrEnvironmentalVariableVocabEntry> uniqueResults = new LinkedHashSet<>();
		    	for(ScoreDoc scoreDoc : td.scoreDocs){
		    		Document matchedDoc = searcher.doc(scoreDoc.doc);
		    		String environmentalVariable = matchedDoc.get(IndexConstants.FLD_ENVIRONMENT);
		    		String title = vocabService.getLabelForPropertyCode(environmentalVariable);
		    		if(StringUtils.hasLength(environmentalVariable)){
		    			uniqueResults.add(new TraitOrEnvironmentalVariableVocabEntry(environmentalVariable, title));
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
	
	
	@Override
	public List<SpeciesName> autocompleteSpeciesName(String partialSpeciesName) {
		try {
			return speciesSearchService.performSearch(partialSpeciesName, 50, false);
		} catch (IOException e) {
			logger.error("Failed to query index", e);
		}
		return null;
	}
	
	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getTraitVocabData() {
		Query allTraitToSpeciesDocumentsQuery = buildAllDocumentsOfTypeQuery(DocumentType.TRAIT_SPECIES);
		//FIXME will it perform with a full index? Should we pre-bake?
		return performSpeciesTraitSearch(allTraitToSpeciesDocumentsQuery, EVERYTHING);
	}

	@Override
	public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentalVariableVocabData() {
		Query allEnvVarToSpeciesDocumentsQuery = buildAllDocumentsOfTypeQuery(DocumentType.SPECIES_ENV);
		//FIXME will it perform with a full index? Should we pre-bake?
		return performSpeciesEnvironmentSearch(allEnvVarToSpeciesDocumentsQuery, EVERYTHING);
	}
	
	@Override
	public List<SpeciesSummary> getSpeciesSummary(List<String> speciesNames) {
		// FIXME make this real
		List<SpeciesSummary> result = new LinkedList<>();
		for (String curr : speciesNames) {
			try {
				result.add(new SpeciesSummary(curr, "FIXME", "FIXME", -1, new URL("https://api.aekos.org.au/FIXME"), 
						new URL("https://api.aekos.org.au/FIXME.jpg"), "FIXME"));
			} catch (MalformedURLException e) {
				logger.error("Failed to create a URL when processing " + speciesNames, e);
			}
		}
		return result;
	}
	
	private Query buildAllDocumentsOfTypeQuery(DocumentType documentType) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		Query docTypeQuery = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, documentType.name()));
		queryBuilder.add(docTypeQuery, Occur.MUST);
		Query allTraitToSpeciesDocumentsQuery = queryBuilder.build();
		return allTraitToSpeciesDocumentsQuery;
	}
}
