package au.org.aekos.service.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LuceneSearchServiceTest {
	
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
}
