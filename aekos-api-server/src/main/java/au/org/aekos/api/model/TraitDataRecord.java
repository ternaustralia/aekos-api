package au.org.aekos.api.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TraitDataRecord extends SpeciesOccurrenceRecordV1_0 {

    private final Collection<TraitOrEnvironmentalVariable> traits = new LinkedList<>();

	public TraitDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month,
			String bibliographicCitation, String samplingProtocol) {
		super(decimalLatitude, decimalLongitude, geodeticDatum, locationID, scientificName, individualCount, eventDate,
				year, month, bibliographicCitation, samplingProtocol);
	}
	
	public TraitDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			int individualCount, String eventDate, int year, int month,
			String bibliographicCitation, String samplingProtocol, String taxonRemarks) {
		super(decimalLatitude, decimalLongitude, geodeticDatum, locationID, individualCount, eventDate,
				year, month, bibliographicCitation, samplingProtocol, taxonRemarks);
	}

	public Collection<TraitOrEnvironmentalVariable> getTraits() {
		return Collections.unmodifiableCollection(traits);
	}
	
	public void addTraitValue(TraitOrEnvironmentalVariable TraitOrEnvironmentalVariable) {
		traits.add(TraitOrEnvironmentalVariable);
	}

	public String toCsv() {
		StringBuilder result = new StringBuilder(super.toCsv());
		for (TraitOrEnvironmentalVariable curr : traits) {
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.getName()));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.getValue()));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.getUnits()));
		}
		return result.toString();
	}

	public boolean matchesTraitFilter(List<String> traitNames) {
		return Helper.matchesFilter(traitNames, traits);
	}
}
