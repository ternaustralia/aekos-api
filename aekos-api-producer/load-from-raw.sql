-- Run this in the target DB. We expect that the target tables are empty.
-- Assumptions:
--  # the raw DB name = rawdata

INSERT INTO citations
SELECT *
FROM rawdata2.citations;
-- FROM rawdata.citations;

-- INSERT INTO env
-- SELECT DISTINCT * -- FIXME remove DISTINCT when we've fixed data
-- FROM rawdata.env
-- WHERE samplingProtocol IN (SELECT samplingProtocol FROM citations);

-- INSERT INTO envvars
-- SELECT *
-- FROM rawdata.envvars
-- WHERE (locationID, eventDate) IN (SELECT locationID, eventDate FROM env);

-- INSERT INTO species
-- SELECT *
-- FROM rawdata.species
-- WHERE (locationID, eventDate) IN (SELECT locationID, eventDate FROM env);

-- INSERT INTO traits
-- SELECT *
-- FROM rawdata.traits
-- WHERE parentId IN (SELECT id FROM species);

-- DELETE FROM rawdata.citations
-- WHERE samplingProtocol IN (SELECT samplingProtocol FROM citations);

-- DELETE FROM rawdata.env
-- WHERE (locationID, eventDate) IN (SELECT locationID, eventDate FROM env);

-- DELETE FROM rawdata.envvars
-- WHERE (locationID, eventDate) IN (SELECT locationID, eventDate FROM envvars);

-- DELETE FROM rawdata.species
-- WHERE id IN (SELECT id FROM species);

-- DELETE FROM rawdata.traits
-- WHERE parentId IN (SELECT parentId FROM traits);

SELECT count(*) AS citationOphans FROM rawdata.citations;
-- SELECT count(*) AS envOrphans FROM rawdata.env;
-- SELECT count(*) AS envvarsOrphans FROM rawdata.envvars;
-- SELECT count(*) AS speciesOrphans FROM rawdata.species;
-- SELECT count(*) AS traitsOrphans FROM rawdata.traits;

-- In case you need to wipe the slate and start again
-- DELETE FROM traits;
-- DELETE FROM species;
-- DELETE FROM envvars;
-- DELETE FROM env;
-- DELETE FROM citations;
