package au.org.aekos.service.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.aekos.controller.ProgressTracker;
import au.org.aekos.service.retrieval.IndexLoaderCallback;
import au.org.aekos.service.retrieval.IndexLoaderRecord;
import au.org.aekos.service.retrieval.RetrievalService;
import au.org.aekos.service.search.load.LoaderClient;

@Service
public class LuceneIndexingService implements IndexingService {

	@Autowired
	private RetrievalService retrievalService;
	
	@Autowired
	private LoaderClient loader;
	
	@Override
	public String doIndexing() throws IOException {
		int totalRecordCount = retrievalService.getTotalSpeciesRecordsHeld();
		ProgressTracker tracker = new ProgressTracker(10000, totalRecordCount);
		Map<String, Integer> speciesCounts = new HashMap<>();
		// FIXME clear index before starting
		loader.beginLoad();
		retrievalService.getIndexStream(new IndexLoaderCallback() {
			@Override
			public void accept(IndexLoaderRecord record) {
				try {
					loader.addSpeciesTraitTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getTraitNames()));
					loader.addSpeciesEnvironmentTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getEnvironmentalVariableNames()));
					Integer speciesCount = speciesCounts.get(record.getSpeciesName());
					if (speciesCount == null) {
						speciesCount = 0;
					}
					speciesCounts.put(record.getSpeciesName(), ++speciesCount);
				} catch (IOException e) {
					throw new RuntimeException("Failed to add a record to the index: " + record.toString(), e);
				}
				tracker.addRecord();
			}
		});
		processSpeciesCounts(speciesCounts);
		loader.endLoad();
		return tracker.getFinishedMessage();
	}
	
	private void processSpeciesCounts(Map<String, Integer> speciesCounts) throws IOException {
		for (Entry<String, Integer> curr : speciesCounts.entrySet()) {
			loader.addSpecies(curr.getKey(), curr.getValue());
		}
	}

	public void setRetrievalService(RetrievalService retrievalService) {
		this.retrievalService = retrievalService;
	}

	public void setLoader(LoaderClient loader) {
		this.loader = loader;
	}
}
