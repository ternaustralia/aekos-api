package au.org.aekos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import au.org.aekos.model.TraitVocabEntry;

@Service
public class TraitDataFactory {

	@Value("${data-file.trait}")
	private String dataFilePath;
	
	public List<TraitVocabEntry> getData() throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(dataFilePath))));
		String[] currLine;
		List<TraitVocabEntry> result = new LinkedList<>();
		while ((currLine = reader.readNext()) != null) {
			result.add(TraitVocabEntry.deserialiseFrom(currLine));
		}
		reader.close();
		return result;
	}
}
