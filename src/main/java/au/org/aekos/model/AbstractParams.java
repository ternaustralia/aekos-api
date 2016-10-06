package au.org.aekos.model;

public abstract class AbstractParams {
	private final int start;
	private final int rows;

	public AbstractParams(int start, int rows) {
		this.start = start;
		this.rows = rows;
	}

	public int getStart() {
		return start;
	}

	public int getRows() {
		return rows;
	}
}