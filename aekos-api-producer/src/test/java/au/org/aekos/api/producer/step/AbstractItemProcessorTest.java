package au.org.aekos.api.producer.step;

import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

public class AbstractItemProcessorTest {

	/**
	 * Is the expected log message printed (unfortunately we can't assert anything)?
	 */
	@Test
	public void testReportProblems01() {
		ConcreteItemProcessor objectUnderTest = new ConcreteItemProcessor();
		AttributeExtractor extractor = new AttributeExtractor() {
			@Override public String getId() { return "textExtractor1"; }
			@Override public AttributeRecord doExtractOn(Resource subject) { return null; }
			@Override public boolean canHandle(Resource subject) { return false; }
		};
		objectUnderTest.logErrorFor(extractor);
		objectUnderTest.logErrorFor(extractor);
		objectUnderTest.logErrorFor(extractor);
		objectUnderTest.reportProblems();
		// logger output should mention 3 errors were found
	}
	
	private static class ConcreteItemProcessor extends AbstractItemProcessor<AttributeExtractor> { }
}
