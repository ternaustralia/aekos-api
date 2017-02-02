package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.loader.service.load.LuceneIndexingService;

public class LuceneIndexingServiceTest {

	/**
	 * Can we get the value when it exists as a string literal?
	 */
	@Test
	public void testGetString01() {
		LuceneIndexingService objectUnderTest = TestGetString01Helper.INSTANCE.getObjectUnderTest();
		Resource res = TestGetString01Helper.res1;
		String result = objectUnderTest.getString(res, "foo");
		assertThat(result, is("bananas"));
	}
	
	/**
	 * Can we get the value when it exists as a number literal?
	 */
	@Test
	public void testGetString02() {
		LuceneIndexingService objectUnderTest = TestGetString02Helper.INSTANCE.getObjectUnderTest();
		Resource res = TestGetString02Helper.res2;
		String result = objectUnderTest.getString(res, "foo");
		assertThat(result, is("123"));
	}
	
	/**
	 * Is the expected exception thrown when we ask for a predicate that doesn't exist?
	 */
	@Test(expected=IllegalStateException.class)
	public void testGetString03() {
		LuceneIndexingService objectUnderTest = TestGetString03Helper.INSTANCE.getObjectUnderTest();
		Resource res = TestGetString03Helper.res3;
		objectUnderTest.getString(res, "notThere");
	}

	private static class TestGetString01Helper {
		static TestGetString01Helper INSTANCE = new TestGetString01Helper();
		static Resource res1;

		LuceneIndexingService getObjectUnderTest() {
			LuceneIndexingService result = new LuceneIndexingService();
			Dataset ds = DatasetFactory.create();
			Model model = ds.getDefaultModel();
			populate(model);
			result.setDs(ds);
			return result;
		}

		void populate(Model model) {
			res1 = model.createResource(ns("res1"));
			res1.addLiteral(model.createProperty(ns("foo")), "bananas");
		}
	}
	
	private static class TestGetString02Helper extends TestGetString01Helper {
		static TestGetString02Helper INSTANCE = new TestGetString02Helper();
		static Resource res2;

		@Override
		void populate(Model model) {
			res2 = model.createResource(ns("res2"));
			res2.addLiteral(model.createProperty(ns("foo")), 123);
		}
	}
	
	private static class TestGetString03Helper extends TestGetString01Helper {
		static TestGetString03Helper INSTANCE = new TestGetString03Helper();
		static Resource res3;

		@Override
		void populate(Model model) {
			Resource type = model.createResource("urn:SomeType");
			res3 = model.createResource(ns("res2"), type);
			res3.addLiteral(model.createProperty(ns("somePred")), "blah");
		}
	}
	
	private static String ns(String localName) {
		return LuceneIndexingService.API_NS + localName;
	}
}
