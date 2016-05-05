package au.org.aekos;

import java.util.Collection;
import java.util.LinkedList;

public class AekosRecordResponse {
	private final Collection<AekosRecord> data = new LinkedList<>();
	private final boolean success;
	private final String errorMessage;

	public AekosRecordResponse() {
		this.success = true;
		this.errorMessage = null;
	}
	
	public AekosRecordResponse(Throwable t) {
		this.success = false;
		this.errorMessage = t.getMessage();
	}

	public int getCount() {
		return data.size();
	}
	
	public void addData(AekosRecord record) {
		data.add(record);
	}
	
	public Collection<AekosRecord> getData() {
		return data;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
