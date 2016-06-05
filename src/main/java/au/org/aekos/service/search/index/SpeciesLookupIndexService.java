package au.org.aekos.service.search.index;

import java.io.IOException;
import java.util.List;

import au.org.aekos.model.SpeciesName;

public interface SpeciesLookupIndexService {
	List<SpeciesName> performSearch(String term, int numResults, boolean termHighlight) throws IOException;
}
