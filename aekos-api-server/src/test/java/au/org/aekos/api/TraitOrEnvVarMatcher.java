package au.org.aekos.api;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import au.org.aekos.api.model.TraitOrEnvironmentalVariable;

public class TraitOrEnvVarMatcher extends BaseMatcher<TraitOrEnvironmentalVariable> {
	private final String name;
	private final String value;
	private final String units;
	
	public TraitOrEnvVarMatcher(String name, String value, String units) {
		this.name = name;
		this.value = value;
		this.units = units;
	}

	@Override
	public boolean matches(Object item) {
		if (item == null) {
			return false;
		}
		TraitOrEnvironmentalVariable castItem = (TraitOrEnvironmentalVariable) item;
		if (name.equals(castItem.getName()) && value.equals(castItem.getValue()) && units.equals(castItem.getUnits())) {
			return true;
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(String.format("a '%s' variable with '%s' '%s'", name, value, units));
	}
	
	public static Matcher<TraitOrEnvironmentalVariable> isTraitOrVar(String name, String value, String units) {
		return new TraitOrEnvVarMatcher(name, value, units);
	}
}