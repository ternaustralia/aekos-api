DROP TABLE IF EXISTS traits;
DROP TABLE IF EXISTS envvars;
DROP TABLE IF EXISTS species;
DROP TABLE IF EXISTS env;
DROP TABLE IF EXISTS citations;

CREATE TABLE citations (
	samplingProtocol VARCHAR(100) NOT NULL,
	bibliographicCitation VARCHAR(500),
	datasetName VARCHAR(150),
	PRIMARY KEY (samplingProtocol)
);

CREATE TABLE env (
	locationID VARCHAR(100) NOT NULL,
	eventDate CHAR(10) NOT NULL,
	month INT,
	year INT,
	decimalLatitude DOUBLE,
	decimalLongitude DOUBLE,
	geodeticDatum VARCHAR(5),
	locationName VARCHAR(50),
	samplingProtocol VARCHAR(100),
	PRIMARY KEY (locationID, eventDate)
);

CREATE TABLE envvars (
	locationID VARCHAR(100) NOT NULL,
	eventDate CHAR(10) NOT NULL,
	varName varchar(30),
	varValue varchar(100),
	varUnit varchar(30),
	FOREIGN KEY (locationID, eventDate) REFERENCES env(locationID, eventDate)
);

CREATE TABLE species (
	id CHAR(36) NOT NULL,
	locationID VARCHAR(100) NOT NULL,
	eventDate CHAR(10) NOT NULL,
	individualCount INT,
	scientificName VARCHAR(100),
	taxonRemarks VARCHAR(100),
	PRIMARY KEY (id),
	FOREIGN KEY (locationID, eventDate) REFERENCES env(locationID, eventDate)
);
CREATE INDEX idx_species_scientificName ON species(scientificName);
CREATE INDEX idx_species_taxonRemarks ON species(taxonRemarks);

CREATE TABLE traits (
	parentId CHAR(37) NOT NULL,
	traitName varchar(30),
	traitValue varchar(100),
	traitUnit varchar(30),
	FOREIGN KEY (parentId) REFERENCES species(id)
);
