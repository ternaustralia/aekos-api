DROP TABLE IF EXISTS traits;
DROP TABLE IF EXISTS envvars;
DROP TABLE IF EXISTS species;
DROP TABLE IF EXISTS env;
DROP TABLE IF EXISTS citations;

CREATE TABLE citations (
	samplingProtocol VARCHAR(100) NOT NULL,
	bibliographicCitation VARCHAR(500),
	datasetName VARCHAR(100),
	PRIMARY KEY (samplingProtocol)
);

CREATE TABLE env (
	locationID VARCHAR(100) NOT NULL,
	decimalLatitude DOUBLE,
	decimalLongitude DOUBLE,
	geodeticDatum VARCHAR(5),
	locationName VARCHAR(50),
	samplingProtocol VARCHAR(100),
	eventDate CHAR(10),
	month INT,
	year INT,
	PRIMARY KEY (locationID)
);

CREATE TABLE envvars (
	parentId VARCHAR(100) NOT NULL,
	traitName varchar(30),
	traitValue varchar(100),
	traitUnit varchar(30),
	FOREIGN KEY (parentId) REFERENCES env(locationID)
);

CREATE TABLE species (
	id CHAR(37) NOT NULL,
	individualCount INT,
	locationID VARCHAR(100) NOT NULL,
	scientificName VARCHAR(100),
	taxonRemarks VARCHAR(100),
	PRIMARY KEY (id),
	FOREIGN KEY (locationID) REFERENCES env(locationID)
);

CREATE TABLE traits (
	parentId CHAR(37) NOT NULL,
	traitName varchar(30),
	traitValue varchar(100),
	traitUnit varchar(30),
	FOREIGN KEY (parentId) REFERENCES species(id)
);
