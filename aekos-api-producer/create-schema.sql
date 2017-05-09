DROP TABLE species;
DROP TABLE traits;

CREATE TABLE species (
	id int,
	scientificName varchar(50)
);

CREATE TABLE traits (
	parentId int,
	traitName varchar(50),
	traitValue varchar(50),
	traitUnit varchar(50)
);
