package au.org.aekos.api.service.server.retrieval;

import java.io.Writer;
import java.util.List;

import org.springframework.stereotype.Service;

import au.org.aekos.api.controller.RetrievalResponseHeader;
import au.org.aekos.api.model.EnvironmentDataResponse;
import au.org.aekos.api.model.SpeciesDataResponseV1_0;
import au.org.aekos.api.model.SpeciesDataResponseV1_1;
import au.org.aekos.api.model.TraitDataResponse;
import au.org.aekos.api.service.retrieval.AekosApiRetrievalException;
import au.org.aekos.api.service.retrieval.RetrievalService;

@Service
public class LuceneRetrievalService implements RetrievalService {

	@Override
	public SpeciesDataResponseV1_0 getSpeciesDataJsonV1_0(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpeciesDataResponseV1_0 getAllSpeciesDataJsonV1_0(int start, int rows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpeciesDataResponseV1_1 getSpeciesDataJsonV1_1(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpeciesDataResponseV1_1 getAllSpeciesDataJsonV1_1(int start, int rows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getSpeciesDataCsvV1_0(List<String> speciesNames, int start, int rows, Writer respWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getAllSpeciesDataCsvV1_0(int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getSpeciesDataCsvV1_1(List<String> speciesNames, int start, int rows, Writer respWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getAllSpeciesDataCsvV1_1(int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TraitDataResponse getTraitDataJson(List<String> speciesNames, List<String> traitNames, int start, int rows) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getTraitDataCsv(List<String> speciesNames, List<String> traitNames, int start, int rows, Writer respWriter)
			throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnvironmentDataResponse getEnvironmentalDataJson(List<String> speciesNames, List<String> environmentalVariableNames, int start, int rows)
			throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getEnvironmentalDataCsv(List<String> speciesNames, List<String> environmentalVariableNames, int start, int rows,
			Writer responseWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalRecordsHeldForSpeciesName(String speciesName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalSpeciesRecordsHeld() {
		// TODO Auto-generated method stub
		return 0;
	}

}
