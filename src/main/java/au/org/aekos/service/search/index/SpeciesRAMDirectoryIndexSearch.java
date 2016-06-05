package au.org.aekos.service.search.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


//@Service
public class SpeciesRAMDirectoryIndexSearch {

	
	@Value("${autocomplete.taxon.listlength}")
	protected Integer listLength = 100;
	//
	@Autowired
	//protected ControlledVocabularyService controlledVocabService;
	
	protected Map<String, RAMDirectory> traitIndexMap = new HashMap<String, RAMDirectory>();
	
	private void buildDirectoryForTrait(String traitName) throws IOException{
		List<TraitValue> tvList = new ArrayList<TraitValue>();
		RAMDirectory idx = new RAMDirectory();
		IndexWriterConfig conf = new IndexWriterConfig( new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(idx, conf);
		int commitCounter = 0;
		int commitLimit = 1000;
		for(TraitValue tv : tvList){
			Document doc = null;//createIndexDocument(tv);
			writer.addDocument(doc);
			if(++commitCounter == commitLimit ){
				writer.commit();
				commitCounter = 0;
			}
		}
		writer.commit();
        writer.close();
		traitIndexMap.put(traitName, idx);
	}
	/*
	public List<TraitValue> performSearch(String term, String traitName, boolean termHighlight ) throws IOException {
		return performSearch(term, traitName, listLength, termHighlight);
	}
	
	//Used for finding name sp. values, for flora taxa ( fauna is'nt as sophisticated yet )
	public TraitValue performExactMatchSearchTraitValue(String term, String traitName) throws IOException{
		if(! traitIndexMap.containsKey(traitName) ){
			buildDirectoryForTrait(traitName);
		}
	/*	RAMDirectory idx = traitIndexMap.get(traitName);
		IndexSearcher searcher = new IndexSearcher( DirectoryReader.open(idx) );
		BooleanQuery qry = new BooleanQuery();
		qry.add(new TermQuery(new Term("traitValue", term)), Occur.MUST);
		TopDocs td = searcher.search(qry, 1);
		if(td != null && td.scoreDocs != null &&td.scoreDocs.length > 0 && td.scoreDocs[0] != null){
			ScoreDoc sd = td.scoreDocs[0] ;
			Document d = searcher.doc(sd.doc);
			if(d != null){
				return new TraitValue(d.getField("traitValue").stringValue() ,  d.getField("displayValue").stringValue());
			}
		}
		return null;
	}
	
	public List<TraitValue> performSearch(String term, String traitName, int numResults, boolean termHighlight) throws IOException{
		List<TraitValue> traitValueList1 = new ArrayList<TraitValue>();
		List<TraitValue> traitValueList2 = new ArrayList<TraitValue>();
		if(! traitIndexMap.containsKey(traitName) ){
			buildDirectoryForTrait(traitName);
		}
		
		RAMDirectory idx = traitIndexMap.get(traitName);
		IndexSearcher searcher = new IndexSearcher( DirectoryReader.open(idx) );
		String searchTerm = term.toLowerCase().replace(" ", "").replace(".", "");
		Query q = new PrefixQuery(new Term("search", searchTerm ));
		Query q2 = new PrefixQuery(new Term("search_sub", searchTerm ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField("search", SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					traitValueList1.add(new TraitValue(d.getField("traitValue").stringValue() ,  d.getField("displayValue").stringValue()));
				}
			}
		}
		
		TopDocs td2 = searcher.search(q2, numResults, new Sort(new SortField("search", SortField.Type.STRING)));
		if( td2.totalHits > 0 ){
			for(ScoreDoc sd :td2.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					traitValueList2.add(new TraitValue(d.getField("traitValue").stringValue() ,  d.getField("displayValue").stringValue()));
				}
			}
		}
		List<TraitValue> resultList ;
		if(td.totalHits == 0 && td2.totalHits == 0 ){
			resultList = doWildcardQuery(searchTerm, numResults, searcher );
		}else{
		    resultList = mergePrimaryAndSecondaryResults(traitValueList1, traitValueList2, numResults, 15, 20);
		}
		if(termHighlight){
			applyHighlightToResults(term, resultList);
		}
		return resultList;
	}
	
	private List<TraitValue> doWildcardQuery(String searchTerm, int numResults, IndexSearcher searcher) throws IOException{
		List<TraitValue> traitValueList = new ArrayList<TraitValue>();
		Query q = new WildcardQuery(new Term("search", "*" + searchTerm + "*" ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField("search", SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					traitValueList.add(new TraitValue(d.getField("traitValue").stringValue() ,  d.getField("displayValue").stringValue()));
				}
			}
		}
		return traitValueList;
	}
	
	protected List<TraitValue> mergePrimaryAndSecondaryResults(List<TraitValue> tvListPrimary, List<TraitValue> tvListSecondary, int numResults, int numPrimaryFirst, int numSecondary){
		if(tvListSecondary.size() == 0){
			return tvListPrimary;
		}
		if(tvListPrimary.size() == 0){
			return tvListSecondary;
		}
		
		Set<TraitValue> tvSet = new LinkedHashSet<TraitValue>();
		
		int primSz = tvListPrimary.size();
		int secSz = tvListSecondary.size();
		
		//Lets load up the linkled hash set - 
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
		List<TraitValue> result = new ArrayList<TraitValue>();
		result.addAll(tvSet);
		return result;
	}
	
	private static Document createIndexDocument(TraitValue tv){
		Document doc = new Document();
		doc.add(new StringField("traitValue" , tv.getTraitValue(), Field.Store.YES ));
		doc.add(new StringField("displayValue" , tv.getFormattedDisplayString(), Field.Store.YES ));
		if(tv.getScientificNames() != null && tv.getScientificNames().size() > 0 ){
			for(String scientificName : tv.getScientificNames() ){
				if(StringUtils.hasLength(scientificName)){
					doc.add(new StringField("scientificName" , scientificName, Field.Store.YES ));
				}
			}
		}
		//Case insensitive tokenisation with boosting
		manualLowercaseTokeniseAndAddBoost(tv.getDisplayString(), doc);
		return doc;
	}
	
	
	
	static private void manualLowercaseTokeniseAndAddBoost(String traitDisplayValue, Document doc){
		String lowercase = traitDisplayValue.toLowerCase().replace(" ", "").replace(".", "");
		Field textField = new TextField("search", lowercase, Field.Store.NO );
		textField.setBoost(10.0f);
		doc.add(textField);
		textField.setBoost(10.0f);
		doc.add(textField);
        
		//Text field for levenstein distance search
		String lev = lowercase.replaceAll(",", "").replaceAll("'", "").replaceAll("-","");
	    Field textFieldLev = new TextField("search_lev", lev, Field.Store.NO );
		doc.add(textFieldLev);
		if(traitDisplayValue.contains(" ")){
			String lc = traitDisplayValue.toLowerCase();
			String [] tokens = lc.split(" ");
			if(tokens.length > 0){
				for(int x = 1; x < tokens.length; x++ ){
					String token = tokens[x];
					if(StringUtils.hasLength(token) && ! token.contains(".") ){
					    Field field = new TextField("search_sub", token, Field.Store.NO);
					    doc.add(field);
					}
				}
			}
		}
	}
	
	protected void applyHighlightToResults(String term, List<TraitValue> resultList){
		term = addUpperAndLowerFirstCharSearchTermCharacterOption(term);
		Pattern p = Pattern.compile("(.*?)("+term+")(.*?)");
		boolean replaceEm = false;
		if("em".equals(term) || "Em".equals(term)){
			replaceEm = true;
		}
		for(TraitValue tv : resultList){
			String displayString = tv.getDisplayString();
			if(replaceEm){
				displayString = displayString.replaceAll("em>", "xxx>");
			}
			Matcher matcher = p.matcher(displayString);
			if(matcher.matches() && matcher.groupCount() == 3 ){
				displayString = matcher.group(1) + "<strong>" + matcher.group(2) + "</strong>" + matcher.group(3);
			}
			if(replaceEm){
				displayString = displayString.replaceAll("xxx>", "em>");
			}
			tv.setDisplayString(displayString);
		}
	}
	
	public TraitValue getBestFuzzyMatch(String term, String traitName) throws IOException{
    	RAMDirectory idx = traitIndexMap.get(traitName);
		IndexSearcher searcher = new IndexSearcher( DirectoryReader.open(idx) );
		String searchTerm = term.toLowerCase().replaceAll(" ", "").replaceAll("\\.", "").replaceAll(",", "").replaceAll("'", "").replaceAll("-","");
        Query q = new FuzzyQuery(new Term("search_lev", searchTerm ), 2, 1, 10, false);
		TopDocs td = searcher.search(q, 1);
		if( td.totalHits > 0 && td.scoreDocs[0] != null){
			Document d = searcher.doc(td.scoreDocs[0].doc);
			if(d != null){
				TraitValue tv = new TraitValue(d.getField("traitValue").stringValue() , d.getField("displayValue").stringValue());
			    IndexableField[] indxFields = d.getFields("scientificName");
			    if(indxFields != null && indxFields.length > 0 ){
			    	for(int x = 0; x< indxFields.length; x++){
			    	    tv.getScientificNames().add(indxFields[x].stringValue());
			    	}
			    }
			    return tv;
			}
		}
		return null;
	}
	
	
    public List<TraitValue> performSearchForSpeciesMatching(String term, String traitName,  int listLength ) throws IOException{
    	List<TraitValue> traitValueList = new ArrayList<TraitValue>();
    	RAMDirectory idx = traitIndexMap.get(traitName);
		IndexSearcher searcher = new IndexSearcher( DirectoryReader.open(idx) );
		String searchTerm = term.toLowerCase().replaceAll(" ", "").replaceAll("\\.", "").replaceAll(",", "").replaceAll("'", "").replaceAll("-","");
        Query q = new FuzzyQuery(new Term("search_lev", searchTerm ), 2, 1, 10, false);
		TopDocs td = searcher.search(q, listLength);
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					System.out.println( Float.toString(sd.score ) + "  " + d.getField("traitValue").stringValue() );
					traitValueList.add(new TraitValue(d.getField("traitValue").stringValue() ,  d.getField("displayValue").stringValue()));
					System.out.println( d.getField("traitValue").stringValue() );
				}
			}
		}
		return traitValueList;
	}
	
	/*
	
	private List<TraitValue> doFuzzyQuery(String searchTerm, int numResults, IndexSearcher searcher) throws IOException{
		List<TraitValue> traitValueList = new ArrayList<TraitValue>();
	//	Query q = new FuzzyQuery(new Term("search", searchTerm ), numResults, numResults, numResults, false)
		
		//int maxEdits, int prefixLength, int maxExpansions, boolean transpositions
		
		
		//Query q = new WildcardQuery(new Term("search", "*" + searchTerm + "*" ));
		TopDocs td = searcher.search(q, numResults, new Sort(new SortField("search", SortField.Type.STRING)));
		if( td.totalHits > 0 ){
			for(ScoreDoc sd :td.scoreDocs ){
				Document d = searcher.doc(sd.doc);
				if(d != null){
					traitValueList.add(new TraitValue(d.getField("traitValue").stringValue() ,  d.getField("displayValue").stringValue()));
				}
			}
		}
		return traitValueList;
	}
	
	*/
	
	
	
	static protected String addUpperAndLowerFirstCharSearchTermCharacterOption(String term){
	    char c = term.charAt(0);
		if(! Character.isLetter(c)){
			return term;
		}
		char [] charArray = new char[4];
		charArray[0] = '[';
		charArray[3] = ']';
		if(Character.isUpperCase(c)){
			charArray[1] = c;
			charArray[2] = Character.toLowerCase(c);
		}
		else{
			charArray[1] = Character.toUpperCase(c);
			charArray[2] = c;
		}
		return String.copyValueOf(charArray) + term.substring(1);
	}

	//@Override
	public void afterPropertiesSet() throws Exception {
		//Initialise the 'taxon names' index directory
		//buildDirectoryForTrait(ControlledVocabularyServiceImpl.FLORA_TAXA_TRAIT_NAME);
		//buildDirectoryForTrait(ControlledVocabularyServiceImpl.FAUNA_TAXA_TRAIT_NAME);
		//buildDirectoryForTrait(ControlledVocabularyServiceImpl.COMMON_FLORA_TRAIT_NAME);
		//buildDirectoryForTrait(ControlledVocabularyServiceImpl.COMMON_FAUNA_TRAIT_NAME);
	}
	
}
