-- base records
SELECT DISTINCT
CONCAT(e.locationID, '#', e.eventDate) AS visitKey, -- delete me during processing
e.eventDate,
e.`month`,
e.`year`,
e.decimalLatitude,
e.decimalLongitude,
e.geodeticDatum,
e.locationID,
e.locationName,
e.samplingProtocol,
c.bibliographicCitation,
c.datasetName
FROM species AS s
INNER JOIN env AS e
ON s.locationID = e.locationID
AND s.eventDate = e.eventDate
AND (
  s.scientificName IN ('Pomaderris apetala')
  OR s.taxonRemarks IN ('Pomaderris apetala')
)
INNER JOIN citations AS c
ON e.samplingProtocol = c.samplingProtocol
ORDER BY 1
LIMIT 4 OFFSET 0;

-- count for all matches
SELECT count(DISTINCT locationID, eventDate) as recordsHeld
FROM species
WHERE (
  scientificName IN ('Pomaderris apetala')
  OR taxonRemarks IN ('Pomaderris apetala')
);

-- variables for the four visitKeys we found
SELECT
CONCAT(locationID, '#', eventDate) AS visitKey, -- delete me during processing
varName,
varValue,
varUnit
FROM envvars
WHERE (locationID, eventDate) in (
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFBEL0002','2015-01-05'),
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFKIN0002','2014-11-17'),
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0001','2012-03-25'),
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002','2012-04-16')
)
ORDER BY 1; -- add all found keys


-- scientificNames and taxonRemarks for the four visitKeys we found
SELECT DISTINCT
CONCAT(locationID, '#', eventDate) AS visitKey, -- delete me during processing
scientificName,
taxonRemarks
FROM species
WHERE (
  scientificName IN ('Pomaderris apetala')
  OR taxonRemarks IN ('Pomaderris apetala')
)
AND (locationID, eventDate) in (
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFBEL0002','2015-01-05'),
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFKIN0002','2014-11-17'),
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0001','2012-03-25'),
	('aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002','2012-04-16')) -- add all found keys
ORDER BY 1;