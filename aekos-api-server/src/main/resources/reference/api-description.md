The ÆKOS API is used for Machine2Machine (M2M) REST access to ÆKOS ecological data.
###High level workflow
 - Start with a trait or species
 - Find what species have that trait or what *traits*/*environmental variables* are available for that species
 - Use your traits/species/environmental variables to retrieve the data with the retrieval services

###Detailed workflow
Firstly, start with the search services. You should either find a species with `speciesAutocomplete` 
or find a trait with `getTraitVocab`. Then, for traits, find the species with that trait with 
`getSpeciesByTrait`. For species, you can find what traits or environmental variables are available with
`getEnvironmentBySpecies` or `getTraitsBySpecies`.
Now you can retrieve the data. You can get Darwin Core species records (using species names), Darwin Core + 
traits (using species names and optionally filtering by trait names) or environmental variable records 
(using species names and optionally filtering by environmental variable names).

###A note about species names
We have used Darwin Core terms (version 2015-06-02) as field names for the result of the data retrieval resources. 
There are two fields that are relevant to species names: `scientificName` and `taxonRemarks`. The former is, as the name
suggests, for scientific species names and the latter is for records that have a common species name or a 
commentary about the organism e.g.: "Grass", "Absent" or "Clover". See http://rs.tdwg.org/dwc/terms/index.htm#scientificName
and http://rs.tdwg.org/dwc/terms/index.htm#taxonRemarks for more information.
