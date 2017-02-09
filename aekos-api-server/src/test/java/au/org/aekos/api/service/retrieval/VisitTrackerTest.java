package au.org.aekos.api.service.retrieval;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import au.org.aekos.api.model.VisitInfo;
import au.org.aekos.api.service.retrieval.VisitTracker;

public class VisitTrackerTest {

	/**
	 * Can we get the location ID and event date param list?
	 */
	@Test
	public void testGetEventDateSparqlParamList01() {
		VisitTracker objectUnderTest = new VisitTracker();
		objectUnderTest.addVisitInfo("loc1", "2011-01-01", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc1", "2012-02-02", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc2", "2012-02-02", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc2", "2013-03-03", new VisitInfo("not important", "not important"));
		String result = objectUnderTest.getLocationIDAndEventDateSparqlParamList();
		System.out.println(result);
		assertThat(result, containsString("(\"loc1\" \"2011-01-01\")"));
		assertThat(result, containsString("(\"loc1\" \"2012-02-02\")"));
		assertThat(result, containsString("(\"loc2\" \"2012-02-02\")"));
		assertThat(result, containsString("(\"loc2\" \"2013-03-03\")"));
		assertThat(result.length(), is(87));
	}
	
	/**
	 * Can we correctly count visits?
	 */
	@Test
	public void testVisitSize01() {
		VisitTracker objectUnderTest = new VisitTracker();
		objectUnderTest.addVisitInfo("loc1", "2011-01-01", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc1", "2012-02-02", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc2", "2012-02-02", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc2", "2013-03-03", new VisitInfo("not important", "not important"));
		int result = objectUnderTest.visitSize();
		assertThat(result, is(4));
	}
}
