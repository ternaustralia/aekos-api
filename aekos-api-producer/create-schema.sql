DROP TABLE IF EXISTS traits;
DROP TABLE IF EXISTS envvars;
DROP TABLE IF EXISTS species;
DROP TABLE IF EXISTS env;
DROP TABLE IF EXISTS citations;

CREATE TABLE citations (
	samplingProtocol VARCHAR(100) NOT NULL,
	bibliographicCitation VARCHAR(1000),
	datasetName VARCHAR(150),
	licence VARCHAR(100)
	,PRIMARY KEY (samplingProtocol) -- NOT_RAW
);

CREATE TABLE env (
	locationID VARCHAR(200) NOT NULL,
	eventDate CHAR(10) NOT NULL,
	month INT,
	year INT,
	decimalLatitude DOUBLE,
	decimalLongitude DOUBLE,
	geodeticDatum VARCHAR(5),
	locationName VARCHAR(50),
	samplingProtocol VARCHAR(100)
	,PRIMARY KEY (locationID, eventDate) -- NOT_RAW
	,FOREIGN KEY (samplingProtocol) REFERENCES citations(samplingProtocol) -- NOT_RAW
);

CREATE TABLE envvars (
	locationID VARCHAR(200) NOT NULL,
	eventDate CHAR(10) NOT NULL,
	varName varchar(30) NOT NULL,
	varValue varchar(100),
	varUnit varchar(30)
	,FOREIGN KEY (locationID, eventDate) REFERENCES env(locationID, eventDate) -- NOT_RAW
);
CREATE INDEX idx_envvars_varName ON envvars(varName); -- NOT_RAW

CREATE TABLE species (
	id CHAR(36) NOT NULL,
	locationID VARCHAR(200) NOT NULL,
	eventDate CHAR(10) NOT NULL,
	individualCount INT,
	scientificName VARCHAR(100),
	taxonRemarks VARCHAR(100)
	,PRIMARY KEY (id) -- NOT_RAW
	,FOREIGN KEY (locationID, eventDate) REFERENCES env(locationID, eventDate) -- NOT_RAW
);
CREATE INDEX idx_species_scientificName ON species(scientificName); -- NOT_RAW
CREATE INDEX idx_species_taxonRemarks ON species(taxonRemarks); -- NOT_RAW

CREATE TABLE traits (
	parentId CHAR(37) NOT NULL,
	traitName varchar(30) NOT NULL,
	traitValue varchar(100),
	traitUnit varchar(30)
	,FOREIGN KEY (parentId) REFERENCES species(id) -- NOT_RAW
);
CREATE INDEX idx_traits_traitName ON traits(traitName); -- NOT_RAW
