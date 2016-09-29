package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import au.org.aekos.model.VisitInfo;

public class VisitTrackerTest {

	/**
	 * Can we get the event date param list when there are multiple event dates per visit
	 * and duplicates should be removed?
	 */
	@Test
	public void testGetEventDateSparqlParamList01() {
		VisitTracker objectUnderTest = new VisitTracker();
		objectUnderTest.addVisitInfo("loc1", "2011-01-01", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc1", "2012-02-02", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc2", "2012-02-02", new VisitInfo("not important", "not important"));
		objectUnderTest.addVisitInfo("loc2", "2013-03-03", new VisitInfo("not important", "not important"));
		String result = objectUnderTest.getEventDateSparqlParamList();
		assertThat(result, containsString("\"2013-03-03\""));
		assertThat(result, containsString("\"2012-02-02\""));
		assertThat(result, containsString("\"2011-01-01\""));
		assertThat(result.length(), is(38));
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
