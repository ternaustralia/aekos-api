package au.org.aekos.model;

import java.util.Collection;
import java.util.LinkedList;

public class SpeciesDataResponse {
	private final Collection<SpeciesDataRecord> data = new LinkedList<>();
	private final boolean success;
	private final String errorMessage;

	public SpeciesDataResponse() {
		this.success = true;
		this.errorMessage = null;
	}
	
	public SpeciesDataResponse(Throwable t) {
		this.success = false;
		this.errorMessage = t.getMessage();
	}

	public int getCount() {
		return data.size();
	}
	
	public void addData(SpeciesDataRecord record) {
		data.add(record);
	}
	
	public Collection<SpeciesDataRecord> getData() {
		return data;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
