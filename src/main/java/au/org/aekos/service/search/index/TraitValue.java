package au.org.aekos.service.search.index;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

public class TraitValue implements Serializable, Comparable<TraitValue> {

	private static final long serialVersionUID = -3201245080498290137L;
	private String traitValue;
	private String displayString;
	private String formattedDisplayString;
	private String description;
	private String parent;
	
	//For 'common name' type traits with corresponding scientific names
	//Change to List - might be multiple scientific names for the same 'Common' name
	//This will need to be looked at in a future iteration, but for now treat each scientific name as a possible match
	//for portal indexing
	private Set<String> scientificNames = new HashSet<String>(); 
	
	public TraitValue() {
		super();
	}
	public TraitValue(String traitValue, String displayString,
			String description) {
		super();
		this.traitValue = traitValue;
		this.displayString = displayString;
		this.description = description;
	}
	public TraitValue(String traitValue, String displayString) {
		super();
		this.traitValue = traitValue;
		this.displayString = displayString;
		this.description = displayString;
	}
	public TraitValue(String traitValue) {
		super();
		this.traitValue = traitValue;
		this.displayString = traitValue;
		this.description = traitValue;
	}
	public String getTraitValue() {
		return traitValue;
	}
	public void setTraitValue(String traitValue) {
		this.traitValue = traitValue;
	}
	public String getDisplayString() {
		return displayString;
	}
	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getFormattedDisplayString() {
		return StringUtils.hasLength(formattedDisplayString ) ? formattedDisplayString : displayString;
	}
	public void setFormattedDisplayString(String formattedDisplayString) {
		this.formattedDisplayString = formattedDisplayString;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((displayString == null) ? 0 : displayString.hashCode());
		result = prime
				* result
				+ ((formattedDisplayString == null) ? 0
						: formattedDisplayString.hashCode());
		result = prime * result
				+ ((traitValue == null) ? 0 : traitValue.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraitValue other = (TraitValue) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (displayString == null) {
			if (other.displayString != null)
				return false;
		} else if (!displayString.equals(other.displayString))
			return false;
		if (formattedDisplayString == null) {
			if (other.formattedDisplayString != null)
				return false;
		} else if (!formattedDisplayString.equals(other.formattedDisplayString))
			return false;
		if (traitValue == null) {
			if (other.traitValue != null)
				return false;
		} else if (!traitValue.equals(other.traitValue))
			return false;
		return true;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	@Override
	public int compareTo(TraitValue o) {
		return this.traitValue.compareTo(o.traitValue);
	}
	public Set<String> getScientificNames() {
		return scientificNames;
	}
	public void setScientificNames(Set<String> scientificNames) {
		this.scientificNames = scientificNames;
	}
	@Override
	public String toString() {
		return "TraitValue [traitValue=" + traitValue + ", displayString=" + displayString + ", formattedDisplayString="
				+ formattedDisplayString + ", description=" + description + ", parent=" + parent + ", scientificNames="
				+ scientificNames + "]";
	}
	
	
}
