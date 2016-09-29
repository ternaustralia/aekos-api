package au.org.aekos.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnvironmentDataRecord {

	private static final String CSV_SEPARATOR = ",";
	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String geodeticDatum;
    private final String locationID;
    private final String eventDate;
    private final int year;
    private final int month;
    private final Collection<TraitOrEnvironmentalVariable> variables = new LinkedList<>();
    private final String bibliographicCitation;
    private final String samplingProtocol;
    private final Set<String> scientificNames = new HashSet<>();
	private final Set<String> taxonRemarks = new HashSet<>();

    public EnvironmentDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String eventDate, int year, int month, String bibliographicCitation, String samplingProtocol) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationID = locationID;
		this.eventDate = eventDate;
		this.year = year;
		this.month = month;
		this.bibliographicCitation = bibliographicCitation;
		this.samplingProtocol = samplingProtocol;
	}

	public double getDecimalLatitude() {
		return decimalLatitude;
	}

	public double getDecimalLongitude() {
		return decimalLongitude;
	}

	public String getGeodeticDatum() {
		return geodeticDatum;
	}

	public String getLocationID() {
		return locationID;
	}

	public String getEventDate() {
		return eventDate;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	public Collection<TraitOrEnvironmentalVariable> getVariables() {
		return Collections.unmodifiableCollection(variables);
	}

	public void addVariable(TraitOrEnvironmentalVariable entry) {
		variables.add(entry);
	}
	
	public void addScientificNames(Set<String> names) {
		scientificNames.addAll(names);
	}
	
	public void addTaxonRemarks(Set<String> remarks) {
		taxonRemarks.addAll(remarks);
	}
	
	public Set<String> getScientificNames() {
		return Collections.unmodifiableSet(scientificNames);
	}
	
	public Set<String> getTaxonRemarks() {
		return Collections.unmodifiableSet(taxonRemarks);
	}

	public String toCsv() {
		StringBuilder result = new StringBuilder();
		result.append(decimalLatitude);
		result.append(CSV_SEPARATOR);
		result.append(decimalLongitude);
		result.append(CSV_SEPARATOR);
		result.append(quote(geodeticDatum));
		result.append(CSV_SEPARATOR);
		result.append(quote(locationID));
		result.append(CSV_SEPARATOR);
		result.append(quote(scientificNames.stream().collect(Collectors.joining("|"))));
		result.append(CSV_SEPARATOR);
		result.append(quote(taxonRemarks.stream().collect(Collectors.joining("|"))));
		result.append(CSV_SEPARATOR);
		result.append(quote(eventDate));
		result.append(CSV_SEPARATOR);
		result.append(year);
		result.append(CSV_SEPARATOR);
		result.append(month);
		result.append(CSV_SEPARATOR);
		result.append(quote(bibliographicCitation));
		result.append(CSV_SEPARATOR);
		result.append(quote(samplingProtocol));
		for (TraitOrEnvironmentalVariable curr : variables) {
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.getName()));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.getValue()));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.getUnits()));
		}
		return result.toString();
	}

	private static String quote(String value) {
		return "\"" + value + "\"";
	}

	public static String getCsvHeader() {
		StringBuilder result = new StringBuilder();
		result.append(quote("decimalLatitude"));
		result.append(CSV_SEPARATOR);
		result.append(quote("decimalLongitude"));
		result.append(CSV_SEPARATOR);
		result.append(quote("geodeticDatum"));
		result.append(CSV_SEPARATOR);
		result.append(quote("locationID"));
		result.append(CSV_SEPARATOR);
		result.append(quote("scientificNames"));
		result.append(CSV_SEPARATOR);
		result.append(quote("taxonRemarks"));
		result.append(CSV_SEPARATOR);
		result.append(quote("eventDate"));
		result.append(CSV_SEPARATOR);
		result.append(quote("year"));
		result.append(CSV_SEPARATOR);
		result.append(quote("month"));
		result.append(CSV_SEPARATOR);
		result.append(quote("bibliographicCitation"));
		result.append(CSV_SEPARATOR);
		result.append(quote("samplingProtocol"));
		return result.toString();
	}

	public boolean matchesTraitFilter(List<String> varNames) {
		return Helper.matchesFilter(varNames, variables);
	}

	@Override
	public String toString() {
		return locationID + "@" + eventDate + " " + variables.size() + " vars, " + scientificNames.size() + " scientificNames, " + taxonRemarks.size() + " taxonRemarks";
	}
}
