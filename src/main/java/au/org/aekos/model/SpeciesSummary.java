package au.org.aekos.model;

import java.net.URL;

public class SpeciesSummary {

	private final String id;
	private String commonName;
	private String scientificName;
	private final int recordsHeld;
	private URL imageUrl;
	private final URL moreInfoUrl;
	private final String classs;

	public SpeciesSummary(String id, String commonName, String scientificName, int recordsHeld, URL imageUrl,
			URL moreInfoUrl, String classs) {
				this.id = id;
				this.commonName = commonName;
				this.scientificName = scientificName;
				this.recordsHeld = recordsHeld;
				this.imageUrl = imageUrl;
				this.moreInfoUrl = moreInfoUrl;
				this.classs = classs;
	}

	public String getId() {
		return id;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getScientificName() {
		return scientificName;
	}

	public int getRecordsHeld() {
		return recordsHeld;
	}

	public String getImageUrl() {
		return imageUrl.toString();
	}

	public String getMoreInfoUrl() {
		return moreInfoUrl.toString();
	}

	public String getClasss() {
		return classs;
	}
}
