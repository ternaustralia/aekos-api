package au.org.aekos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVReader;

@RestController()
@RequestMapping("/v1")
public class AekosRecordController {

	private static final Logger logger = LoggerFactory.getLogger(AekosRecordController.class);
	
    @RequestMapping("/data.json")
    public AekosRecordResponse dataJson(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getParsedData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			return new AekosRecordResponse(e);
		}
    }

    @RequestMapping("/data.csv")
    public String dataCsv(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getRawData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			return "Server Error: [" + e.getClass().toString() + "] " + e.getMessage();
		}
    }
    
	private String getRawData(int limit) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv")));
		String currLine;
		int lineCounter = 0;
		StringBuilder result = new StringBuilder();
		while (lineCounter < limit && (currLine = in.readLine()) != null) {
			result.append(currLine + "\n");
			lineCounter++;
		}
		return result.toString();
	}
	
	private AekosRecordResponse getParsedData(int limit) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv"))));
		String[] currLine;
		int lineCounter = 0;
		AekosRecordResponse result = new AekosRecordResponse();
		while (lineCounter < limit && (currLine = reader.readNext()) != null) {
			result.addData(AekosRecord.deserialiseFrom(currLine));
			lineCounter++;
		}
		reader.close();
		return result;
	}
}
