-- MySQL script to create the default user accounts

-- Test database
CREATE USER 'imstest'@'localhost' identified by 'imstest';
CREATE DATABASE imstest;
GRANT CREATE, DROP, DELETE, INDEX, SELECT, INSERT, UPDATE on imstest.* TO 'imstest'@'localhost';

-- Default IMS database
CREATE USER 'ims'@'localhost' identified by 'ims';
CREATE DATABASE ims;
GRANT CREATE, DROP, DELETE, INDEX, SELECT, INSERT, UPDATE, ALTER on ims.* TO 'ims'@'localhost';


-- HACK for Conceptwiki Gene/Protein to Uniport 
CREATE TABLE mappingSetOriginal LIKE mappingSet; 
INSERT mappingSetOriginal SELECT * FROM mappingSet;
UPDATE mappingSet SET justification = "http://semanticscience.org/resource/SIO_000985" where justification = "http://example.com/ConceptWikiGene" and sourceDataSource = "ConceptWiki";
UPDATE mappingSet SET justification = "http://semanticscience.org/resource/SIO_000985" where justification = "http://example.com/ConceptWikiProtein" and sourceDataSource = "ConceptWiki";
--
later when everything works you can do
DELETE table mappingSetOriginal