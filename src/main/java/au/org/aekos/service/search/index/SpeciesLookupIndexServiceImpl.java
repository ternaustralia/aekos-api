package au.org.aekos.service.search.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.aekos.model.SpeciesName;

@Service
public class SpeciesLookupIndexServiceImpl implements InitializingBean{
	
	private Logger logger = LoggerFactory.getLogger(SpeciesLookupIndexServiceImpl.class);
	
	private RAMDirectory speciesIndex;

	@Autowired
	private LuceneIndexBuilderService indexBuilderService;

	public List<SpeciesName> performSearch(String term, int numResults, boolean termHighlight) throws IOException{
		List<SpeciesName> SpeciesNameList1 = new ArrayList<SpeciesName>();
		List<SpeciesName> SpeciesNameList2 = new ArrayList<SpeciesName>();
		RAMDirectory idx = getSpeciesIndex();
		IndexSearcher searcher = new IndexSearcher( DirectoryReader.open(idx) );
		String searchTerm = term.toLowerCase().replace(" ", "").replace(".", "");
		Query q = new PrefixQuery(new Term(IndexConstants.SEARCH, searchTerm ));
		Query q2 = new PrefixQuery(new Term(IndexConstants.SEARCH_SUB, searchTerm ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField(IndexConstants.SEARCH, SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					SpeciesNameList1.add(new SpeciesName(  d.getField(IndexConstants.DISPLAY_VALUE).stringValue()));
				}
			}
		}
		
		TopDocs td2 = searcher.search(q2, numResults, new Sort(new SortField(IndexConstants.SEARCH, SortField.Type.STRING)));
		if( td2.totalHits > 0 ){
			for(ScoreDoc sd :td2.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					SpeciesNameList2.add(new SpeciesName( d.getField(IndexConstants.DISPLAY_VALUE).stringValue()));
				}
			}
		}
		List<SpeciesName> resultList ;
		if(td.totalHits == 0 && td2.totalHits == 0 ){
			resultList = doWildcardQuery(searchTerm, numResults, searcher );
		}else{
		    resultList = mergePrimaryAndSecondaryResults(SpeciesNameList1, SpeciesNameList2, numResults, 15, 20);
		}
		if(termHighlight){
			//applyHighlightToResults(term, resultList);
		}
		return resultList;
	}
	
	private List<SpeciesName> doWildcardQuery(String searchTerm, int numResults, IndexSearcher searcher) throws IOException{
		List<SpeciesName> SpeciesNameList = new ArrayList<SpeciesName>();
		Query q = new WildcardQuery(new Term("search", "*" + searchTerm + "*" ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField("search", SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					SpeciesNameList.add(new SpeciesName(d.getField(IndexConstants.DISPLAY_VALUE).stringValue()));
				}
			}
		}
		return SpeciesNameList;
	}
	
	protected List<SpeciesName> mergePrimaryAndSecondaryResults(List<SpeciesName> tvListPrimary, List<SpeciesName> tvListSecondary, int numResults, int numPrimaryFirst, int numSecondary){
		if(tvListSecondary.size() == 0){
			return tvListPrimary;
		}
		if(tvListPrimary.size() == 0){
			return tvListSecondary;
		}
		
		Set<SpeciesName> tvSet = new LinkedHashSet<SpeciesName>();
		
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
		List<SpeciesName> result = new ArrayList<SpeciesName>();
		result.addAll(tvSet);
		return result;
	}
	
	
	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		speciesIndex = indexBuilderService.buildSpeciesRAMDirectory();
		logger.info("Species index created - ram bytes " + speciesIndex.ramBytesUsed());
	}
	
	public RAMDirectory getSpeciesIndex() {
		return speciesIndex;
	}
	
	

	
	
}
