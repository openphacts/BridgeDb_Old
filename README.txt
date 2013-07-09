Configuration - This file is out of date.
-------------
Properties File Location:
BridgeDB looks for the configuration files in the following locations. 
Once it finds a configuration file the other locations are ignored. 
* Directly in the run directory  (Mainly for java *.jar runs)
* Environment Variable OPS-IMS-CONFIG: can be used to point to any location
* Tomcat configuration folder: $CATALINA_HOME/conf/OPS-IMS
* ../conf/OPS-IMS                  #Allows tomcat to pick up $CATALINA_HOME/conf/OPS-IMS even if it can not get $CATALINA_HOME
* Using classLoader getResource. This will pick up the files included in Jars and Wars.

======================
The configuration files used (and described below are)
BridgeDB.properties
log4j.properties
DataSource.ttl 
lens.properties
local.properties
======================
local.properties

This is the recommended place to overwrite individual property values of any other *.property file.

Local properties will overwrite values with the same key in any other properties file.
Properties not overwritten in local will keep their original values.

To install local properties you need to.
1. Create a local.properties file
2. Store it in a high priority location as described above
3. Copy the keys from the original file 
4. Addition Key values pairs added to the original properties file will not be blocked by a local.properties file

To remove all properties from a properties file you will need to.
1. Create a copy of that file in a high priority location as described above
2. Edit your copy of that properties file
3. Any additions to the original properties file will have to be manual added as required

======================
BridgeDB.properties  (Must be checked with local values saved to local.properties)
------------

This file contain the local setup information which MUST be checked and ideally changed to match the local setup.

The default configuration BridgeDB.properties file can be found at
	$BRIDGEDB_HOME/org.bridgedb.utils/resources
		
You must either supply local values matching your local setup 
or setup your data stores to use the defaults. 

Database Dependency
-------------------
MySQL version 5 or above must be installed and running
MySQL databases and users must be created with CREATE, DROP, INDEX, INSERT, 
UPDATE, DELETE, and SELECT permissions.

Consult the Config.txt file for the defaults, or copy and amend the configuration file
to your own setup.

If you are using the default accounts and databases then execute the file 
mysqlConfig.sql to create the accounts with appropriate permissions and the 
databases
	mysql -u root -p < mysqlConfig.sql
Note that the sql script will fail, without reverting changes made up to the 
point of failure, if any of the user accounts or databases already exist.

RDF Repository and Transitive Directory Dependency
-------------------------
The RDF engine is automatically included by maven

WARNING: All directories MUST exists and the (linux) user running tomcat MUST have READ/WRITE permission set!
Some of the OpenRDF error message are unclear if this is not the case.

See: Config.txt 
SailNativeStore(s) will be created automatically as long as loader can create/find the directory,

We recommend changing the relative directories to absolute directories.
Please ensure the parent directories exist and have the correct permissions. 

The settings for testing (and therefor compilation) can be left as is.
As long as the testing user would have permission to create and delete files there.

The BaseURI variable is no longer used but may be in the future so is worth setting correctly.

======================================================
Other Configuration files

log4j.properties
----------------
Edit this to change the logger setup.
The default can be found in the Utils Resource directory
Please refer to the log4j documentation for more information.

DataSource.ttl 
--------------
RDF format of all the BridgeDB DataSource(s) and Registered UriPatterns,
Found in $BRIDGEDB_HOME/org.bridgedb.rdf/resources

This file defines all the URI patterns that will match every BridgeDB DataSource.
Warning: As additional UriPatterns are constantly being found and created this file is subject to continuous updates. 
Having a local DataSource.ttl is therefor highly discouraged as it will block future updates being discovered.
Instead please push any changes into the version inside the source code.

If local additions that should not become general usage (such as commercial uriPatterns) 
  the suggested approach is to change the code to use multiple dataSource files.
  Note: This file is NOT effected by local.properties

DataSource.owl
--------------
Ontology of above file. 
Included for reference only and may not be update.

lens.properties
---------------
This file defines the lens to be used in the system.
It can be found in the $BRIDGEDB_HOME/org.bridgedb.rdf/resources

Warning: As Lens is still subject to alterations the format of this file could be changed at any time.
Also as additional Lens are constantly being found and created this file is subject to continuous updates. 
Having a local lens.properties is therefor highly discouraged as it will block future updates being discovered.
Instead please push any changes into the version inside the source code.

Local additions that should not become general usage (such as commercial lens) 
    can be added to the local.properties file.

Note: the fourth part of the key
   lens.lenkey.justification.***
   only serves to keep the keys unique and can have any value.
   If extending a key we suggest using local** as the fourth part of the justification key to ensure not overwriting general additions.

Other files found in resources folders
--------------------------------------
Bio Project: These are out of date. Please see Original BridgeDB project for latest versions.

These do not form part of the current code and could be removed without notice.
They are from ideas that have been discarded or not implemented yet.
These include:
AndraDataSource.ttl
BioDataSource.ttl
BioDataSourceWithPrimary.ttl
BridgeDBDataSource.ttl
ChristianDataSource.ttl
ExportDataSource.ttl
ImsDataSources.ttl
mediaTypes.ttl
MergedDataSource.ttl
MergedDataSourceWithPrimary.ttl
PrimaryDataSource.ttl

------------------------------------------------------------------------------
Data Loading

All tests should load their required data at the start of the tests.
To load the test data into the live sql use the method SetupWithTestData in the URI loader package.

OpenPhacts style data loading is no longer supported directly from BridgeDB.
This is because it depends on the code in the standalone Validator code.
Please use the IMS bundle instead.  (See the readme file their for more information)
Code can be found at:
https://github.com/openphacts/Validator
and
https://github.com/openphacts/IdentityMappingService

However if none OpenPhacts loading of linksets is required the code in SetupWithTestData can be adapted.
-------------------------------------------------------------------------------

Compilation
-----------

If you've obtained the source code of BridgeDb, you should be
able to compile with a simple: 

	mvn clean install
	
Note that for the maven build to run all tests 
1) The MySQL database must be running and configured as above.
2 {Optional}) http://localhost:8080/OPS-IMS to be running the war created by the URI webserver Server module.
   With test data which can be loaded using the class SetupWithTestData in the URI Loader module
   Maven will skip the client tests if the localhost server is not found.

Note: There is an ant build but it may not work in the OpenPhacts branch(es)
      As of writing the ant build is broken in most OpenPhacts branch(es)
	
OPS Webservice Setup.
--------------------

Make sure your local.properties file matches:
The SQL databases included user names and password
The rdf parent directories are setup (and accessible) as above.

Deploy $BridgeDb/org.bridgedb.uri.ws.service/target/org.bridgedb.uri.ws.server-*.war to something like tomcat/webapps
 To setup databases and add test data run org.bridgedb.uri.loader.SetupLoaderWithTestData
(Optional) Depoly $BridgeDb/org.bridgedb.ws.service/target/BridgeDb.war
   Both wars share the same SQL data.

Note: If Installing the OpenPhacts IMS and or the OpenPhacts QueryExpander the org.bridgedb.uri.ws.server-*.war is not required 
  as both the IMS and the QueryExpander extends org.bridgedb.uri.ws.server-*.war

Library dependencies 
--------------------
Note: For org.bridgedb.uri.ws.server-*.war none of the below as required!

If you don't use all mappers, you do not need to include all
libraries in the dist directory in your project.

Here is a brief overview that will help you to find out
which ones you need. For questions, you can always contact our mailing list.

org.bridgedb.jar - always needed. 
    This includes the tab-delimited file driver.
org.bridgedb.bio.jar - includes the BioDataSource enum, often needed
org.bridgedb.webservice.cronos.jar - needed for CRONOS webservice
org.bridgedb.webservice.synergizer.jar - needed for Synergizer webservice
org.bridgedb.webservice.picr.jar - needed for PICR webservice
org.bridgedb.server.jar - the BridgeRest SERVER, not needed if you only 
	want to access BridgeRest or BridgeWebservice as client
org.bridgedb.tools.batchmapper.jar - Contains the batchmapper command line tool

org.bridgedb.jar and org.bridgedb.bio.jar do not need any other jar files to work.
Most of the other jar files in dist/ are part of the SOAP libraries needed only for
some of the webservices. Look in the lib directory and build.xml of the 
respective mappers to find clues which libraries are needed by which service.

Contact
-------

For OpenPhacts specific please contact Christian and use the OPS Jira.

For geneneral BridgeDB:
Website, wiki and bug tracker: http://www.bridgedb.org
Mailing list: http://groups.google.com/group/bridgedb-discuss/
Source code can be obtained from http://svn.bigcat.unimaas.nl/bridgedb

Authors
-------

BridgeDb and related tools are developed by

Jianjiong Gao
Isaac Ho
Martijn van Iersel
Alex Pico

OpenPhacts BridgeDB Team:
Christian Brenninkmeijer
Alasdair Gray
Egon Willighagen

License
-------

BridgeDb is free and open source. It is available under
the conditions of the Apache 2.0 License. 
See License-2.0.txt for details.
