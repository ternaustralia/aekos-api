package au.org.aekos.api.service.search;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import au.org.aekos.api.loader.service.index.RAMDirectoryTermIndexManager;
import au.org.aekos.api.loader.service.index.Trait;
import au.org.aekos.api.loader.service.load.EnvironmentLoaderRecord;
import au.org.aekos.api.loader.service.load.LoaderClient;
import au.org.aekos.api.loader.service.load.LuceneLoaderClient;
import au.org.aekos.api.loader.service.load.SpeciesLoaderRecord;
import au.org.aekos.api.model.SpeciesName;
import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;

public class LuceneSearchServiceTest {

	/**
	 * Can we get the expected traits for a species?
	 */
	@Test
	public void testGetTraitBySpecies01() throws IOException{
		LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addSpeciesRecord(speciesRecordWithTraits("speciesOne", "traitOne", "traitTwo"));
				loader.addSpeciesRecord(speciesRecordWithTraits("speciesOne", "traitThree"));
				loader.addSpeciesRecord(speciesRecordWithTraits("speciesTwo", "traitOne", "traitFour"));
			}
		});
		PageRequest pr = new PageRequest(0, 10);
		List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getTraitBySpecies(Arrays.asList("speciesOne"), pr);
		assertEquals(3, result.size());
		assertThat(result.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems("traitOne", "traitTwo", "traitThree"));
	}

	/**
	 * Can we get traits for multiple species?
	 */
	@Test
	public void testGetTraitBySpecies02() throws IOException{
		LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addSpeciesRecord(speciesRecordWithTraits("speciesOne", "traitOne", "traitTwo"));
				loader.addSpeciesRecord(speciesRecordWithTraits("speciesTwo", "traitOne", "traitFour"));
				loader.addSpeciesRecord(speciesRecordWithTraits("speciesThree", "traitThree"));
			}
		});
		PageRequest pr = new PageRequest(0, 0);
		List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getTraitBySpecies(Arrays.asList("speciesOne", "speciesThree"), pr);
		assertEquals(3, result.size());
		assertThat(result.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems("traitOne", "traitTwo", "traitThree"));
	}
	
	/**
     * Can we get the environmental variable vocab data when it exists?
     */
    @Test
	public void testGetEnvironmentalVariableVocabData01() throws IOException{
    	LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addEnvRecord(new EnvironmentLoaderRecord("loc1", setOf("env one", "env two"), "2009-05-21"));
				loader.addEnvRecord(new EnvironmentLoaderRecord("loc2", setOf("env two", "env three"), "2009-05-21"));
			}
		});
		List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getEnvironmentalVariableVocabData();
		assertThat(result.size(), is(3));
		List<String> codes = result.stream().map(s -> s.getCode()).collect(Collectors.toList());
		assertThat(codes, hasItems("env one", "env two", "env three"));
    }
    
    /**
     * Can we get the trait vocab data when it exists?
     */
    @Test
	public void testGetTraitVocabData01() throws IOException{
		LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addSpeciesRecord(speciesRecordWithTraits("species1", "basalArea", "averageHeight"));
				loader.addSpeciesRecord(speciesRecordWithTraits("species2", "lifeForm", "averageHeight"));
			}
		});
		List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getTraitVocabData();
		assertThat(result.size(), is(3));
		List<String> codes = result.stream().map(s -> s.getCode()).collect(Collectors.toList());
		assertThat(codes, hasItems("basalArea", "averageHeight", "lifeForm"));
    }
    
    /**
	 * Can we get species using a trait?
	 */
	@Test
	public void testGetSpeciesByTrait01() throws IOException{
		LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addSpeciesRecord(speciesRecordWithTraits("species1", "basalArea", "averageHeight"));
				loader.addSpeciesRecord(speciesRecordWithTraits("species2", "lifeForm"));
				loader.addSpeciesRecord(speciesRecordWithTraits("species3", "lifeForm", "averageHeight"));
			}
		});
		PageRequest pr = new PageRequest(0, 10);
		List<SpeciesName> result = objectUnderTest.getSpeciesByTrait(Arrays.asList("averageHeight"), pr);
		assertThat(result.size(), is(2));
		assertThat(result.stream().map(e -> e.getName()).collect(Collectors.toList()), hasItems("species1", "species3"));
	}

	/**
	 * Can we get species using multiple traits?
	 */
	@Test
	public void testGetSpeciesByTrait02() throws IOException{
		LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addSpeciesRecord(speciesRecordWithTraits("species1", "basalArea", "averageHeight"));
				loader.addSpeciesRecord(speciesRecordWithTraits("species2", "lifeForm"));
				loader.addSpeciesRecord(speciesRecordWithTraits("species3", "lifeForm", "averageHeight"));
				loader.addSpeciesRecord(speciesRecordWithTraits("species4", "aspect", "erosionState"));
			}
		});
		PageRequest pr = new PageRequest(0, 0);
		List<SpeciesName> result = objectUnderTest.getSpeciesByTrait(Arrays.asList("averageHeight", "aspect"), pr);
		assertThat(result.size(), is(3));
		assertThat(result.stream().map(e -> e.getName()).collect(Collectors.toList()), hasItems("species1", "species3", "species4"));
	}
	
	/**
     * Can we get environmental variables for a species?
     */
    @Test
	public void testGetEnvironmentBySpecies01() throws IOException{
    	LuceneSearchService objectUnderTest = getObjectUnderTestWithLoadedData(new LoaderCallback() {
			@Override
			public void doLoad(LoaderClient loader) throws IOException {
				loader.addSpeciesRecord(speciesRecordWithJoinKey("species1", "loc1", "2008-04-19"));
				loader.addSpeciesRecord(speciesRecordWithJoinKey("species2", "loc1", "2005-01-01"));
				loader.addSpeciesRecord(speciesRecordWithJoinKey("species3", "loc2", "2009-05-21"));
				loader.addEnvRecord(new EnvironmentLoaderRecord("loc1", setOf("basalArea", "averageHeight"), "2008-04-19"));
				loader.addEnvRecord(new EnvironmentLoaderRecord("loc2", setOf("aspect", "lifeForm"), "2009-05-21"));
			}
		});
		List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getEnvironmentBySpecies(Arrays.asList("species1"), null);
		assertThat(result.size(), is(2));
		assertThat(result.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems("basalArea", "averageHeight"));
    }
	
	/**
     * Can we calculate the start index for an implicit "everything" query?
     */
    @Test
    public void testGetTopDocStartIndex01(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	int result = objectUnderTest.getTopDocStartIndex(null, 10);
    	assertEquals(0, result);
    }
    
    /**
     * Can we calculate the end index for an implicit "everything" query?
     */
    @Test
    public void testGetTopDocEndIndex01(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	int result = objectUnderTest.getTopDocEndIndex(null, 10);
    	assertEquals(9, result);
    }
    
    /**
     * Can we get the start index of the second page?
     */
    @Test
    public void testGetTopDocStartIndex02(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = new PageRequest(2,10);
    	int result = objectUnderTest.getTopDocStartIndex(page, 100);
    	assertEquals(10, result);
    }
    
    /**
     * Can we calculate the end of the second page?
     */
    @Test
    public void testGetTopDocEndIndex02(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = new PageRequest(2,10);
    	int result = objectUnderTest.getTopDocEndIndex(page, 100);
    	assertEquals(19, result);
    }
    
    /**
     * Can we calculate the start of a page with one item?
     */
    @Test
    public void testGetTopDocStartIndex03(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = new PageRequest(11,10);
    	int result = objectUnderTest.getTopDocStartIndex(page, 101);
    	assertEquals(100, result);
    }
    
    /**
     * Can we calculate the end of a page with one item?
     */
    @Test
    public void testGetTopDocEndIndex03(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = new PageRequest(11,10);
    	int result = objectUnderTest.getTopDocEndIndex(page, 101);
    	assertEquals(100, result);
    }
    
    /**
     * Can we calculate the start of a request for a page that's past what's available?
     */
    @Test
    public void testGetTopDocStartIndex04(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = new PageRequest(11,10);
    	int result = objectUnderTest.getTopDocStartIndex(page, 90);
    	assertEquals(-1, result);
    }
    
    /**
     * Can we calculate the end of a request for a page that's past what's available?
     */
    @Test
    public void testGetTopDocEndIndex04(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = new PageRequest(11,10);
    	int result = objectUnderTest.getTopDocEndIndex(page, 90);
    	assertEquals(89, result);
    }
    
    /**
     * Can we calculate the start of an explicit request for everything?
     */
    @Test
    public void testGetTopDocStartIndex05(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = LuceneSearchService.EVERYTHING;
    	int result = objectUnderTest.getTopDocStartIndex(page, 90);
    	assertEquals(0, result);
    }
    
    /**
     * Can we calculate the end of an explicit request for everything?
     */
    @Test
    public void testGetTopDocEndIndex05(){
    	LuceneSearchService objectUnderTest = new LuceneSearchService();
    	PageRequest page = LuceneSearchService.EVERYTHING;
    	int result = objectUnderTest.getTopDocEndIndex(page, 90);
    	assertEquals(89, result);
    }
    
    private SpeciesLoaderRecord speciesRecordWithJoinKey(String speciesName, String locationId, String eventDate) {
		return new SpeciesLoaderRecord(speciesName, Collections.emptySet(), "not important",
				"not important", locationId, eventDate);
	}
    
    private SpeciesLoaderRecord speciesRecordWithTraits(String speciesName, String...traitNames) {
		Set<Trait> traits = new HashSet<>();
		for (String curr : traitNames) {
			traits.add(new Trait(curr, "not important", "not important"));
		}
		return new SpeciesLoaderRecord(speciesName, traits, "not important", "not important",
				"not important", "not important");
	}
    
    interface LoaderCallback {
		void doLoad(LoaderClient loader) throws IOException;
	}
	
    private LuceneSearchService getObjectUnderTestWithLoadedData(LoaderCallback loaderCallback) throws IOException {
    	LuceneSearchService result = new LuceneSearchService();
		RAMDirectoryTermIndexManager termIndexManager = new RAMDirectoryTermIndexManager();
		result.setTermIndexManager(termIndexManager);
		LuceneLoaderClient loader = new LuceneLoaderClient();
		loader.setIndexManager(termIndexManager);
		result.setVocabService(new StubVocabService());
		loader.beginLoad();
		loaderCallback.doLoad(loader);
		loader.endLoad();
		return result;
	}
    
    private Set<String> setOf(String...values) {
		return new HashSet<>(Arrays.asList(values));
	}
}
