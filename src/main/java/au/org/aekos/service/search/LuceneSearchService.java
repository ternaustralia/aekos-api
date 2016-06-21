package au.org.aekos.service.search;

import java.io.IOException;
import java.util.ArrayList;
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
		List<TraitVocabEntry> responseList = new ArrayList<TraitVocabEntry>();
		IndexSearcher searcher = null;
		try {
			searcher = termIndexManager.getIndexSearcher();
		} catch (IOException e) {
			logger.error("Can't search trait by species - IOException - returning empty list", e);
			return responseList;
		}
		
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		Query docTypeQuery = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, DocumentType.TRAIT_SPECIES.name()));
		queryBuilder.add(docTypeQuery, Occur.MUST);
		queryBuilder.setMinimumNumberShouldMatch(1); //For optional field matches- i.e. the OR condition
		for(String speciesName : speciesNames ){
			Query q = new TermQuery(new Term(IndexConstants.FLD_SPECIES, speciesName));
			queryBuilder.add(q, Occur.SHOULD);
		}
		Query query = queryBuilder.build();
		//We want all of the results so num results MAX_VALUE
		try {
			TopDocs td = searcher.search(query, Integer.MAX_VALUE, new Sort(new SortField(IndexConstants.FLD_TRAIT, SortField.Type.STRING)));
		    int totalTraits = td.totalHits;
		    if(td.totalHits > 0){
		    	for(ScoreDoc scoreDoc : td.scoreDocs){
		    		Document matchedDoc = searcher.doc(scoreDoc.doc);
		    		String trait = matchedDoc.get(IndexConstants.FLD_TRAIT);
		    		if(StringUtils.hasLength(trait)){
		    			responseList.add(new TraitVocabEntry(trait, trait));
		    		}
		    	}
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseList;
	}
	
	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<EnvironmentVariable> getEnvironmentBySpecies(List<String> speciesNames) {
		// TODO Auto-generated method stub
		return null;
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
