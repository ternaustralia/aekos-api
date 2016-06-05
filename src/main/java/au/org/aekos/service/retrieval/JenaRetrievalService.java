package au.org.aekos.service.retrieval;

import java.io.Writer;
import java.util.List;

import org.springframework.stereotype.Service;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.SpeciesOccurrenceRecord;
import au.org.aekos.model.TraitDataResponse;

@Service
public class JenaRetrievalService implements RetrievalService {

	// TODO Tom to implement
	
	@Override
	public List<SpeciesOccurrenceRecord> getSpeciesDataJson(List<String> speciesNames, Integer limit)
			throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getSpeciesDataCsv(List<String> speciesNames, Integer limit, boolean triggerDownload,
			Writer responseWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<EnvironmentDataRecord> getEnvironmentalData(List<String> speciesNames,
			List<String> environmentalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TraitDataResponse getTraitData(List<String> speciesNames, List<String> traitNames, int start, int count)
			throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}
}
