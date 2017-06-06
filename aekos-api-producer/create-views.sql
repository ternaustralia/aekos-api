CREATE OR REPLACE VIEW speciesView
AS
  SELECT
  s.id,
  s.scientificName,
  s.taxonRemarks,
  s.individualCount,
  s.eventDate,
  e.`month`,
  e.`year`,
  e.decimalLatitude,
  e.decimalLongitude,
  e.geodeticDatum,
  s.locationID,
  e.locationName,
  e.samplingProtocol,
  c.bibliographicCitation,
  c.datasetName
  FROM species AS s
  INNER JOIN env AS e
  ON s.locationID = e.locationID
  AND s.eventDate = e.eventDate
  INNER JOIN citations AS c
  ON e.samplingProtocol = c.samplingProtocol;

CREATE OR REPLACE VIEW siteVisitView
AS
SELECT DISTINCT
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
  FROM env AS e
  INNER JOIN citations AS c
  ON e.samplingProtocol = c.samplingProtocol;
