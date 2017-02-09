package au.org.aekos.api.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

import au.org.aekos.api.model.ResponseHeader;

public class ResponseHeaderTest {

	/**
	 * Can we tell when we're at the start of the first page when there are multiple pages?
	 */
	@Test
	public void testCalculatePageNumber01() {
		int start = 0;
		int numFound = 66;
		int totalPages = 7;
		int result = ResponseHeader.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(1));
	}

	/**
	 * Can we tell when we're at the start of the first page when there is only one page?
	 */
	@Test
	public void testCalculatePageNumber02() {
		int start = 0;
		int numFound = 9;
		int totalPages = 1;
		int result = ResponseHeader.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(1));
	}
	
	/**
	 * Can we tell when we're at the start of the last page?
	 */
	@Test
	public void testCalculatePageNumber03() {
		int start = 90;
		int numFound = 99;
		int totalPages = 10;
		int result = ResponseHeader.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(10));
	}
	
	/**
	 * Can we tell when we're on a page in the middle somewhere?
	 */
	@Test
	public void testCalculatePageNumber04() {
		int start = 50;
		int numFound = 99;
		int totalPages = 10;
		int result = ResponseHeader.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(6));
	}
	
	/**
	 * Can we handle small page sizes?
	 */
	@Test
	public void testCalculatePageNumber05() {
		int start = 2;
		int numFound = 6;
		int totalPages = 3;
		int result = ResponseHeader.calculatePageNumber(start, numFound, totalPages);
		assertThat(result, is(2));
	}
	
	/**
	 * Can we calculate the total pages when it rounds nicely and there's more than one?
	 */
	@Test
	public void testCalculateTotalPages01() {
		int rows = 10;
		int numFound = 100;
		int result = ResponseHeader.calculateTotalPages(rows, numFound);
		assertThat(result, is(10));
	}
	
	/**
	 * Can we calculate the total pages when it rounds nicely and there's only one?
	 */
	@Test
	public void testCalculateTotalPages02() {
		int rows = 10;
		int numFound = 10;
		int result = ResponseHeader.calculateTotalPages(rows, numFound);
		assertThat(result, is(1));
	}
	
	/**
	 * Can we calculate the total pages when it doesn't round nicely and there's only one?
	 */
	@Test
	public void testCalculateTotalPages03() {
		int rows = 10;
		int numFound = 6;
		int result = ResponseHeader.calculateTotalPages(rows, numFound);
		assertThat(result, is(1));
	}
	
	/**
	 * Can we calculate the total pages when it doesn't round nicely and there's more than one?
	 */
	@Test
	public void testCalculateTotalPages04() {
		int rows = 10;
		int numFound = 101;
		int result = ResponseHeader.calculateTotalPages(rows, numFound);
		assertThat(result, is(11));
	}
}
