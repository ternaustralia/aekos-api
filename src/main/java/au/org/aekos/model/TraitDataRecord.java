package au.org.aekos.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TraitDataRecord extends SpeciesOccurrenceRecord {

    private final Collection<Entry> traits = new LinkedList<>();

    public static class Entry {
    	private final String name;
    	private final String value;
    	private final String units;
		public Entry(String name, String value, String units) {
			this.name = name;
			this.value = value;
			this.units = units;
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
		public String getUnits() {
			return units;
		}
		@Override
		public String toString() {
			return "[" + name + "=" + value + " " + units + "]";
		}
    }
    
	public TraitDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month,
			String bibliographicCitation, String samplingProtocol) {
		super(decimalLatitude, decimalLongitude, geodeticDatum, locationID, scientificName, individualCount, eventDate,
				year, month, bibliographicCitation, samplingProtocol);
	}

	public Collection<Entry> getTraits() {
		return Collections.unmodifiableCollection(traits);
	}
	
	public void addTraitValue(Entry entry) {
		traits.add(entry);
	}

	public String toCsv() {
		StringBuilder result = new StringBuilder(super.toCsv());
		for (Entry curr : traits) {
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.name));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.value));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.units));
		}
		return result.toString();
	}

	public boolean matchesTraitFilter(List<String> traitNames) {
		if (traitNames.size() == 0) {
			return true;
		}
		for (Entry curr : traits) {
			if (traitNames.contains(curr.name)) {
				return true;
			}
		}
		return false;
	}
}
