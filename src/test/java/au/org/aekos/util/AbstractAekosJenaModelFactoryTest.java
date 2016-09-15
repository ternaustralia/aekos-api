package au.org.aekos.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class AbstractAekosJenaModelFactoryTest {

	/**
	 * Can we load a model from a TDB data directory when it exists with data?
	 */
	@Test
	public void testGetInstance01() throws Throwable {
		ConcreteAekosJenaModelFactory objectUnderTest = new ConcreteAekosJenaModelFactory();
		String tdbDataDir = Files.createTempDirectory("testGetInstance01").toString();
		Dataset dataset = TDBFactory.createDataset(tdbDataDir);
		Model model = dataset.getDefaultModel();
		Resource s = model.createResource("aekos.org.au#foo");
		Property p = model.createProperty("has_value");
		s.addLiteral(p, "bar");
		dataset.close();
		ReflectionTestUtils.setField(objectUnderTest, "notDefined", "NOT_DEFINED");
		objectUnderTest.setPath(tdbDataDir);
		Model result = objectUnderTest.getInstance();
		Resource foo = result.createResource("aekos.org.au#foo");
		assertThat(foo.getProperty(p).getString(), is("bar"));
	}
	
	/**
	 * Is the expected exception thrown when we're in prod and we don't define a TDB path?
	 */
	@Test(expected=IllegalStateException.class)
	public void testGetInstance02() throws Throwable {
		ConcreteAekosJenaModelFactory objectUnderTest = new ConcreteAekosJenaModelFactory();
		ReflectionTestUtils.setField(objectUnderTest, "notDefined", "NOT_DEFINED");
		ReflectionTestUtils.setField(objectUnderTest, "isProd", true);
		// leave path as not defined
		objectUnderTest.getInstance();
	}
	
	/**
	 * Can we create an in-memory dataset when no path is provided?
	 */
	@Test
	public void testGetInstance03() throws Throwable {
		ConcreteAekosJenaModelFactory objectUnderTest = new ConcreteAekosJenaModelFactory();
		ReflectionTestUtils.setField(objectUnderTest, "notDefined", "NOT_DEFINED");
		// leave path as not defined
		Dataset result = objectUnderTest.getDatasetInstance();
		assertNotNull("We should create an in-memory dataset when no path is provided", result);
	}
}

class ConcreteAekosJenaModelFactory extends AbstractAekosJenaModelFactory {
	private String path = "NOT_DEFINED";

	@Override
	String getModelPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@Override String getModelName() { return "test model"; }
	@Override void doPostConstructStats(Model newInstance) { }
}