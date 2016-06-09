package au.org.aekos.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;

public class ModelLoaderTest {

	/**
	 * Can we load a model from a TDB data directory when it exists with data?
	 */
	@Test
	public void testLoadModel01() throws Throwable {
		ModelLoader objectUnderTest = new ModelLoader();
		String tdbDataDir = Files.createTempDirectory("ModelLoaderTest").toString();
		Dataset dataset = TDBFactory.createDataset(tdbDataDir);
		Model model = dataset.getDefaultModel();
		Resource s = model.createResource("aekos.org.au#foo");
		Property p = model.createProperty("has_value");
		s.addLiteral(p, "bar");
		dataset.close();
		objectUnderTest.setTdbDataDir(tdbDataDir);
		Model result = objectUnderTest.loadModel();
		Resource foo = result.createResource("aekos.org.au#foo");
		assertThat(foo.getProperty(p).getString(), is("bar"));
	}
}
