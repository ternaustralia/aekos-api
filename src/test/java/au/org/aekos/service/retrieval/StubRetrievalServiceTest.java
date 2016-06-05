package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class StubRetrievalServiceTest {

	/**
	 * Can we tell when we're at the start of the first page when there are multiple pages?
	 */
	@Test
	public void testCalculatePageNumber01() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int start = 0;
		int numFound = 66;
		int totalPages = 7;
		int result = objectUnderTest.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(1));
	}

	/**
	 * Can we tell when we're at the start of the first page when there is only one page?
	 */
	@Test
	public void testCalculatePageNumber02() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int start = 0;
		int numFound = 9;
		int totalPages = 1;
		int result = objectUnderTest.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(1));
	}
	
	/**
	 * Can we tell when we're at the start of the last page?
	 */
	@Test
	public void testCalculatePageNumber03() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int start = 90;
		int numFound = 99;
		int totalPages = 10;
		int result = objectUnderTest.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(10));
	}
	
	/**
	 * Can we tell when we're on a page in the middle somewhere?
	 */
	@Test
	public void testCalculatePageNumber04() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int start = 50;
		int numFound = 99;
		int totalPages = 10;
		int result = objectUnderTest.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(6));
	}
	
	/**
	 * Can we handle small page sizes?
	 */
	@Test
	public void testCalculatePageNumber05() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int start = 2;
		int numFound = 6;
		int totalPages = 3;
		int result = objectUnderTest.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(2));
	}
	
	/**
	 * Can we calculate the total pages when it rounds nicely and there's more than one?
	 */
	@Test
	public void testCalculateTotalPages01() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int rows = 10;
		int numFound = 100;
		int result = objectUnderTest.calculateTotalPages(rows, numFound);
		assertThat(result, is(10));
	}
	
	/**
	 * Can we calculate the total pages when it rounds nicely and there's only one?
	 */
	@Test
	public void testCalculateTotalPages02() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int rows = 10;
		int numFound = 10;
		int result = objectUnderTest.calculateTotalPages(rows, numFound);
		assertThat(result, is(1));
	}
	
	/**
	 * Can we calculate the total pages when it doesn't round nicely and there's only one?
	 */
	@Test
	public void testCalculateTotalPages03() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int rows = 10;
		int numFound = 6;
		int result = objectUnderTest.calculateTotalPages(rows, numFound);
		assertThat(result, is(1));
	}
	
	/**
	 * Can we calculate the total pages when it doesn't round nicely and there's more than one?
	 */
	@Test
	public void testCalculateTotalPages04() {
		StubRetrievalService objectUnderTest = new StubRetrievalService();
		int rows = 10;
		int numFound = 101;
		int result = objectUnderTest.calculateTotalPages(rows, numFound);
		assertThat(result, is(11));
	}
}
