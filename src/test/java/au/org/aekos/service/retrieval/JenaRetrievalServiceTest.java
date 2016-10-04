package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class JenaRetrievalServiceTest {
	
	/**
	 * Can we substitute the scientificNames value list into the SPARQL query when there is one value?
	 */
	@Test
	public void testGetProcessedSparql01() {
		List<String> s = Arrays.asList("Goodenia havilandii");
		int start = 0;
		int rows = 20;
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SPECIES_NAMES_PLACEHOLDER + " }"
				+ " OFFSET %OFFSET_PLACEHOLDER% LIMIT %LIMIT_PLACEHOLDER% }");
		String result = objectUnderTest.getProcessedDarwinCoreSparql(s, start, rows);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Goodenia havilandii\" }"
				+ " OFFSET 0 LIMIT 20 }"));
	}
	
	/**
	 * Can we substitute the scientificNames value list into the SPARQL query when there is more than one value?
	 */
	@Test
	public void testGetProcessedSparql02() {
		List<String> s = Arrays.asList("Goodenia havilandii", "Rosa canina");
		int start = 5000;
		int rows = 100;
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SPECIES_NAMES_PLACEHOLDER + " }"
				+ " OFFSET %OFFSET_PLACEHOLDER% LIMIT %LIMIT_PLACEHOLDER% }");
		String result = objectUnderTest.getProcessedDarwinCoreSparql(s, start, rows);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Goodenia havilandii\" \"Rosa canina\" }"
				+ " OFFSET 5000 LIMIT 100 }"));
	}
	
	/**
	 * Do we retrieve all rows (or at least a lot) when the limit is 0?
	 */
	@Test
	public void testGetProcessedSparql03() {
		List<String> s = Arrays.asList("Rosa canina");
		int start = 0;
		int rows = 0;
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SPECIES_NAMES_PLACEHOLDER + " }"
				+ " OFFSET %OFFSET_PLACEHOLDER% LIMIT %LIMIT_PLACEHOLDER% }");
		String result = objectUnderTest.getProcessedDarwinCoreSparql(s, start, rows);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Rosa canina\" }"
				+ " OFFSET 0 LIMIT " + Integer.MAX_VALUE + " }"));
	}
	
	/**
	 * Can we generate the env data query?
	 */
	@Test
	public void testGetProcessedEnvDataSparql01() {
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setEnvironmentDataQueryTemplate(
			"SELECT *" +
			"WHERE {" +
			"  ?s a api:LocationVisit ." +
			"  ?s api:decimalLatitude ?decimalLatitude ." +
			"  ?s api:decimalLongitude ?decimalLongitude ." +
			"  ?s api:geodeticDatum ?geodeticDatum ." +
			"  ?s api:locationID ?locationID ." +
			"  ?s api:eventDate ?eventDate ." +
			"  ?s api:year ?year ." +
			"  ?s api:month ?month ." +
			"  VALUES (?locationID ?eventDate) { " + JenaRetrievalService.LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER + " } ." +
			"}" +
			"ORDER BY ?s" +
			"OFFSET %OFFSET_PLACEHOLDER%" +
			"LIMIT %LIMIT_PLACEHOLDER%");
		VisitTracker visitTracker = new VisitTracker();
		visitTracker.addVisitInfo("loc1", "date1", null);
		visitTracker.addVisitInfo("loc1", "date2", null);
		visitTracker.addVisitInfo("loc2", "date1", null);
		visitTracker.addVisitInfo("loc3", "date1", null);
		String result = objectUnderTest.getProcessedEnvDataSparql(visitTracker, 0, 20);
		assertEquals(
			"SELECT *" +
			"WHERE {" +
			"  ?s a api:LocationVisit ." +
			"  ?s api:decimalLatitude ?decimalLatitude ." +
			"  ?s api:decimalLongitude ?decimalLongitude ." +
			"  ?s api:geodeticDatum ?geodeticDatum ." +
			"  ?s api:locationID ?locationID ." +
			"  ?s api:eventDate ?eventDate ." +
			"  ?s api:year ?year ." +
			"  ?s api:month ?month ." +
			"  VALUES (?locationID ?eventDate) { (\"loc2\" \"date1\") (\"loc3\" \"date1\") (\"loc1\" \"date2\") (\"loc1\" \"date1\") } ." +
			"}" +
			"ORDER BY ?s" +
			"OFFSET 0" +
			"LIMIT 20", result);
	}
	
	/**
	 * Can we generate the env data count query WITH a variable name filter?
	 */
	@Test
	public void testGetProcessedEnvDataCountSparql01() {
		List<String> varsFilter = Arrays.asList("var1", "var2");
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setEnvironmentDataCountQueryTemplate(
			"SELECT (COUNT(DISTINCT ?s) as ?count)" +
			"WHERE {" +
			"     ?s a api:LocationVisit ." +
			"     ?s api:locationID ?locationID ." +
			"     ?s api:eventDate ?eventDate ." +
			"     VALUES (?locationID ?eventDate) { " + JenaRetrievalService.LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER + " } ." +
			"#OFF ?s api:disturbanceEvidenceVars | api:landscapeVars | api:noUnitVars | api:soilVars ?envVar ." +
			"#OFF ?envVar api:name ?varName ." +
			"#OFF VALUES ?varName { %ENV_VAR_PLACEHOLDER% } ." +
			"}");
		VisitTracker visitTracker = new VisitTracker();
		visitTracker.addVisitInfo("loc1", "date1", null);
		visitTracker.addVisitInfo("loc1", "date2", null);
		visitTracker.addVisitInfo("loc2", "date1", null);
		visitTracker.addVisitInfo("loc3", "date1", null);
		String result = objectUnderTest.getProcessedEnvDataCountSparql(varsFilter, visitTracker);
		assertEquals(
			"SELECT (COUNT(DISTINCT ?s) as ?count)" +
			"WHERE {" +
			"     ?s a api:LocationVisit ." +
			"     ?s api:locationID ?locationID ." +
			"     ?s api:eventDate ?eventDate ." +
			"     VALUES (?locationID ?eventDate) { (\"loc2\" \"date1\") (\"loc3\" \"date1\") (\"loc1\" \"date2\") (\"loc1\" \"date1\") } ." +
			"     ?s api:disturbanceEvidenceVars | api:landscapeVars | api:noUnitVars | api:soilVars ?envVar ." +
			"     ?envVar api:name ?varName ." +
			"     VALUES ?varName { \"var1\" \"var2\" } ." +
			"}", result);
	}
	
	/**
	 * Can we generate the env data count query WITHOUT a variable name filter?
	 */
	@Test
	public void testGetProcessedEnvDataCountSparql02() {
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setEnvironmentDataCountQueryTemplate(
			"SELECT (COUNT(DISTINCT ?s) as ?count)" +
			"WHERE {" +
			"     ?s a api:LocationVisit ." +
			"     ?s api:locationID ?locationID ." +
			"     ?s api:eventDate ?eventDate ." +
			"     VALUES (?locationID ?eventDate) { " + JenaRetrievalService.LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER + " } ." +
			"#OFF ?s api:disturbanceEvidenceVars | api:landscapeVars | api:noUnitVars | api:soilVars ?envVar ." +
			"#OFF ?envVar api:name ?varName ." +
			"#OFF VALUES ?varName { %ENV_VAR_PLACEHOLDER% } ." +
			"}");
		VisitTracker visitTracker = new VisitTracker();
		visitTracker.addVisitInfo("loc1", "date1", null);
		visitTracker.addVisitInfo("loc1", "date2", null);
		visitTracker.addVisitInfo("loc2", "date1", null);
		visitTracker.addVisitInfo("loc3", "date1", null);
		String result = objectUnderTest.getProcessedEnvDataCountSparql(Collections.emptyList(), visitTracker);
		assertEquals(
			"SELECT (COUNT(DISTINCT ?s) as ?count)" +
			"WHERE {" +
			"     ?s a api:LocationVisit ." +
			"     ?s api:locationID ?locationID ." +
			"     ?s api:eventDate ?eventDate ." +
			"     VALUES (?locationID ?eventDate) { (\"loc2\" \"date1\") (\"loc3\" \"date1\") (\"loc1\" \"date2\") (\"loc1\" \"date1\") } ." +
			"#OFF ?s api:disturbanceEvidenceVars | api:landscapeVars | api:noUnitVars | api:soilVars ?envVar ." +
			"#OFF ?envVar api:name ?varName ." +
			"#OFF VALUES ?varName { %ENV_VAR_PLACEHOLDER% } ." +
			"}", result);
	}
	
	/**
	 * Can we replace spaces in a URL?
	 */
	@Test
	public void testReplaceSpaces01() {
		String locationID = "aekos.org.au/collection/sydney.edu.au/DERG/Cravens Peak";
		String result = JenaRetrievalService.replaceSpaces(locationID);
		assertThat(result, is("aekos.org.au/collection/sydney.edu.au/DERG/Cravens%20Peak"));
	}
	
	/**
	 * Can we sanitise a SPARQL param that contains a double quote (")?
	 */
	@Test
	public void testSanitise01() {
		String sparqlParam = "injection\" attack";
		String result = JenaRetrievalService.sanitise(sparqlParam);
		assertThat(result, is("injection\\\" attack"));
	}
	
	/**
	 * Can we sanitise a SPARQL param that contains a backslash (\)?
	 */
	@Test
	public void testSanitise02() {
		String sparqlParam = "injection\\attack";
		String result = JenaRetrievalService.sanitise(sparqlParam);
		assertThat(result, is("injection\\\\attack"));
	}
	
	/**
	 * Can we sanitise a SPARQL param that contains multiple illegal characters?
	 */
	@Test
	public void testSanitise03() {
		String sparqlParam = "\"injection\\att\\ack\"blah";
		String result = JenaRetrievalService.sanitise(sparqlParam);
		assertThat(result, is("\\\"injection\\\\att\\\\ack\\\"blah"));
	}
}
