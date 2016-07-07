package au.org.aekos;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import au.org.aekos.model.TraitDataRecord;

public class TraitDataMatcher extends BaseMatcher<TraitDataRecord.Entry> {
	private final String name;
	private final String value;
	private final String units;
	
	public TraitDataMatcher(String name, String value, String units) {
		this.name = name;
		this.value = value;
		this.units = units;
	}

	@Override
	public boolean matches(Object item) {
		if (item == null) {
			return false;
		}
		TraitDataRecord.Entry castItem = (TraitDataRecord.Entry) item;
		if (name.equals(castItem.getName()) && value.equals(castItem.getValue()) && units.equals(castItem.getUnits())) {
			return true;
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(String.format("a '%s' variable with '%s' '%s'", name, value, units));
	}
	
	public static Matcher<TraitDataRecord.Entry> isTrait(String name, String value, String units) {
		return new TraitDataMatcher(name, value, units);
	}
}