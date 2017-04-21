package au.org.aekos.api.controller;

import static au.org.aekos.api.controller.ControllerHelper.TEXT_CSV_MIME;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import au.org.aekos.api.Constants;
import au.org.aekos.api.model.SpeciesName;
import au.org.aekos.api.model.SpeciesSummary;
import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.api.service.search.PageRequest;
import au.org.aekos.api.service.search.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description="Find species, traits and environmental variables", produces=MediaType.APPLICATION_JSON_VALUE, tags="Search")
@RestController
@RequestMapping(path=Constants.V1_0, method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
public class ApiV1SearchController {

	// TODO do we accept LSID/species ID and/or a species name for the species related services?

	private static final Logger logger = LoggerFactory.getLogger(ApiV1SearchController.class);
	private static final String DEFAULT_PAGE_NUM = "1";
	private static final String DEFAULT_PAGE_SIZE = "20";

	@Autowired
	private SearchService searchService;
	
	@RequestMapping(path="/bqTest", produces=TEXT_CSV_MIME)
	public void bqTest(Writer responseWriter) throws Throwable {
		long start = new Date().getTime();
		List<String> result = runSimpleQuery("SELECT * FROM acacia.test WHERE eventDate = '2013-08-29'");
		System.out.println("Query took " + (new Date().getTime() - start) + "ms");
		for (String curr : result) {
			responseWriter.write(curr + "\n");
		}
//		return Arrays.asList("asdf,2314", "asdfsd,87687");
	}

	public static List<String> runSimpleQuery(String queryString) throws TimeoutException, InterruptedException {
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryString).build();
		return runQuery(queryConfig);
	}

	public static List<String> runQuery(QueryJobConfiguration queryConfig) throws TimeoutException, InterruptedException {
		List<String> result = new LinkedList<>();
		BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
long start = new Date().getTime();
		// Create a job ID so that we can safely retry.
		JobId jobId = JobId.of(UUID.randomUUID().toString());
		Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

		// Wait for the query to complete.
		queryJob = queryJob.waitFor();
System.out.println("initial query execution took " + (new Date().getTime() - start) + "ms");
		// Check for errors
		if (queryJob == null) {
			throw new RuntimeException("Job no longer exists");
		} else if (queryJob.getStatus().getError() != null) {
			// You can also look at queryJob.getStatus().getExecutionErrors()
			// for all
			// errors, not just the latest one.
			throw new RuntimeException(queryJob.getStatus().getError().toString());
		}

		// Get the results.
		QueryResponse response = bigquery.getQueryResults(jobId);
		QueryResult queryResult = response.getResult();

		// Print all pages of the results.
		while (queryResult != null) {
			for (List<FieldValue> row : queryResult.getValues()) {
				StringBuilder sb = new StringBuilder();
				for (FieldValue val : row) {
					if (val.isNull()) {
						sb.append(",");
						continue;
					}
					sb.append(val.getStringValue());
					sb.append(",");
				}
				result.add(sb.toString());
			}
			long pagingStart = new Date().getTime();
			queryResult = queryResult.getNextPage();
			System.out.println("paging took " + (new Date().getTime() - pagingStart) + "ms");
		}
		return result;
	}
	
	@RequestMapping(path="dsTest", produces=TEXT_CSV_MIME)
	public void dsTest(Writer responseWriter) throws Throwable {
		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("thingy")
//				.setFilter(PropertyFilter.eq("favorite_food", "pizza"))
				.build();
		long start = new Date().getTime();
		QueryResults<Entity> results = datastore.run(query);
		System.out.println("query run took " + (new Date().getTime() - start) + "ms");
		while (results.hasNext()) {
			Entity currentEntity = results.next();
			StringBuilder sb = new StringBuilder();
			for (String curr : currentEntity.getNames()) {
				sb.append(currentEntity.getValue(curr).get().toString());
			}
			responseWriter.write(sb.toString() + "\n");
		}
		System.out.println("total processing took " + (new Date().getTime() - start) + "ms");
	}
	
	@RequestMapping("/getTraitVocab.json")
	@ApiOperation(value = "Get trait vocabulary",
		notes = "Gets a distinct list of all the traits that appear in the system. The code and label are "
				+ "supplied for each trait. The codes are required to use as parameters for other resources "
				+ "and the label information is useful for creating UIs.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getTraitVocab() {
		return searchService.getTraitVocabData();
	}

	@RequestMapping("/getEnvironmentalVariableVocab.json")
	@ApiOperation(value = "Get environmental variable vocabulary",
			notes = "Gets a distinct list of all the environmental variables that appear in the system. The code and label are "
					+ "supplied for each variable. The codes are required to use as parameters for other resources "
					+ "and the label information is useful for creating UIs.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentalVariableVocab() {
		return searchService.getEnvironmentalVariableVocabData();
	}
	
	@RequestMapping("/speciesAutocomplete.json")
	@ApiOperation(value = "Autocomplete partial species names",
			notes = "Performs an autocomplete on the partial species name supplied. Results starting with the supplied fragment"
					+ "will be returned ordered by most relevant.")
    public List<SpeciesSummary> speciesAutocomplete(
    		@RequestParam(name="q") @ApiParam("partial species name") String partialSpeciesName,
    		HttpServletResponse resp) throws IOException {
		// TODO do we need propagating headers to enable browser side caching
		// TODO look at returning a complex object with meta information like total results, curr page, etc
		return searchService.speciesAutocomplete(partialSpeciesName, 50);
	}

	@RequestMapping("/getTraitsBySpecies.json")
	@ApiOperation(value = "Get all available traits for specified species",
			notes = "Finds the traits that the supplied species have. Note that the result doesn't include the value"
					+ "of the traits, it only shows that the supplied species have values for those traits. To get the"
					+ "values, you need to use the Data Retrieval services.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getTraitsBySpecies(
    		@RequestParam(name="speciesName", required=true) @ApiParam("species name to search for") String[] speciesNames,
    		@RequestParam(required = false, defaultValue=DEFAULT_PAGE_NUM) @ApiParam("1-indexed page number") int pageNum,
    		@RequestParam(required=false, defaultValue=DEFAULT_PAGE_SIZE) @ApiParam("records per page") int pageSize,
    		HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(pageNum, pageSize);
		
		return searchService.getTraitBySpecies(Arrays.asList(speciesNames), pagination);
	}
	
	@RequestMapping("/getSpeciesByTrait.json")
	@ApiOperation(value = "Get all available species for specified traits",
			notes = "Finds the species names that the supplied trait(s). Note that the result only shows that the "
					+ "supplied traits have species records present in the system. To get the values, you need "
					+ "to use the Data Retrieval services.")
    public List<SpeciesName> getSpeciesByTrait(
    		@RequestParam(name="traitName") String[] traitNames,
    		@RequestParam(required = false, defaultValue=DEFAULT_PAGE_NUM) @ApiParam("1-indexed page number") int pageNum,
    		@RequestParam(required=false, defaultValue=DEFAULT_PAGE_SIZE) @ApiParam("records per page") int pageSize,
			HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(pageNum, pageSize);
		return searchService.getSpeciesByTrait(Arrays.asList(traitNames), pagination);
	}
	
	@RequestMapping("/getEnvironmentBySpecies.json")
	@ApiOperation(value = "Get all available environment variable names for specified species",
			notes = "Finds the environmental variables that the supplied species have. Note that the result doesn't include the value"
					+ "of the environmental variables, it only shows that the supplied species have values for those variable. To get the"
					+ "values, you need to use the Data Retrieval services.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentBySpecies(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required = false, defaultValue=DEFAULT_PAGE_NUM) @ApiParam("1-indexed page number") int pageNum,
    		@RequestParam(required=false, defaultValue=DEFAULT_PAGE_SIZE) @ApiParam("records per page") int pageSize,
    		HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(pageNum, pageSize);
		return searchService.getEnvironmentBySpecies(Arrays.asList(speciesNames), pagination);
	}
	
	@RequestMapping("/speciesSummary.json")
	@ApiOperation(value = "Get a summary of the specified species",
			notes = "A summary of the information that the system holds on the supplied species name(s) "
					+ "including a count of records. If the system doesn't have any data on a species name, "
					+ "it will return a record with id=0 and recordsHeld=0.")
    public List<SpeciesSummary> speciesSummary(
    		@RequestParam(name="speciesName") @ApiParam("list of species names") String[] speciesNames,
    		HttpServletResponse resp) {
		List<SpeciesSummary> result = new LinkedList<>();
		for (String curr : speciesNames) {
			try {
				List<SpeciesSummary> records = searchService.speciesAutocomplete(curr, 1);
				if (records.size() == 0) {
					result.add(new SpeciesSummary("0", curr, 0));
					continue;
				}
				SpeciesSummary match = records.get(0);
				boolean searchReturnedSomethingButItsNotWhatWeAskedFor = !match.getSpeciesName().equalsIgnoreCase(curr);
				if (searchReturnedSomethingButItsNotWhatWeAskedFor) {
					result.add(new SpeciesSummary("0", curr, 0));
					continue;
				}
				result.add(match);
			} catch (IOException e) {
				logger.error("Failed when retrieving species summaries, died on: " + curr, e);
				resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
		}
		return result;
	}
}
