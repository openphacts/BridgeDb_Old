Configuration - This file is out of date.
-------------
BridgeDB looks for the configuration file config.txt in the following locations. Once it 
finds a configuration file the other locations are ignored. 
* Directly in the run directory  (Mainly for java *.jar runs)
* Environment Variable OPS-IMS-CONFIG: can be used to point to any location
* Tomcat configuration folder: $CATALINA_HOME/conf/OPS-IMS
* ../org.bridgedb.utils/resources  #Main used by the build and tests
* ../conf/OPS-IMS                  #Allows tomcat to pick up $CATALINA_HOME/conf/OPS-IMS even if it can not get $CATALINA_HOME
* conf/OPS-IMS                     #Second attempt to tomcat
* ../../BridgeDb/org.bridgedb.utils/resources  #Allows other projects to pick it up if at the same level as BridgeDB
* The jar/war file using getClass().getResource(name)
* The jar/war by unzipping it

The default configuration config.txt file can be found at
	$BRIDGEDB_HOME/org.bridgedb.utils/resources
		
The default configuration files will always be found by the ../org.bridgedb.utils/resources step during testing
or by the last two steps during runs.

You must either edit the configuration files to match your local setup or setup
your data stores to use the defaults. If you edit the files to your configuration
we recommend copying the file to the tomcat configuration folder.

Note: Packages that extend BridgeDB including OPS-IMS, and OPS-QueryExpander 
   as well as ones that sit next to BridgeDB such as Ops-Validator
   Should ideally use the same config.txt file.

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

RDF Repository Dependency
-------------------------
=======
WARNING: All directories MUST exists and the (linux) user running tomcat MUST have REEAD/WRITE permission set!
Some of the OpenRDF error message are unclear if this is not the case.

See: Config.txt 
SailNativeStore(s) will be created automatically as long as loader can 
create/find the directory,

We recommend changing the relative directories to absolute directories.
Please ensure the parent directories exist and have the correct permissions. 

The settings for testing (and therefor compilation) can be left as is.
As long as the testing user would have permission to create and delete files there.

The BaseURI variable is no longer used as the file path or Uri are now used as the base uri.

IP_Address configuration
--------------
This part is not used in this version.

Accounts
------------
List a number of Uris http or ftp
For which a user name and password are required.
If you wish to read from any of these sites you will have to copy and edit this file.
Otherwise you will not be able to read from these Uris.
Note: Should not be required for OpenPhacts 1.2

--------------------------------------------------------------------------
SANDBOX:
-------
Sandbox no longer supported.

--------------------------------------------------------------------------
Other Configuration files

log4j.properties
---------------
Edit this to change the logger setup.

------
Other files in $BRIDGEDB_HOME/org.bridgedb.utils/resources
(Should not require a local change)

DataSource.ttl 
RDF format of all the BridgeDB DataSource(s) and Registered UriPatterns,

DataSource.owl
Ontology of above file. 
Included for reference only.

LinkSet,owl
Ontology used by the Validator.
Changes to this file will affect the Validator.

mediaTypes.ttl BioDataSource.ttl PrimaryDataSource.ttl
Not currently used and subject to removal at anytime

------------------------------------------------------------------------------
Data Loading

OpenPhacts style data loading is no longer supported directly from BridgeDB.

Please use the IMS bundle instead.

-------------------------------------------------------------------------------

Compilation
-----------

If you've obtained the source code of BridgeDb, you should be
able to compile with a simple: 

	mvn clean install
	
Note that for the maven build to run all tests 
1) The MySQL database must be running and configured as above.
2) http://localhost:8080/OPS-IMS to be running and include the test data
   This can be either org.bridgedb.uri.ws.server-*.war 
   or something that extends the the bridgeDB's uri.ws.server.war such as the the IMS's ws.server-*.war
3) http://localhost:8080/BridgeDb to be running and include the test data
  3 is optional as all Core client tests are repeated in OPS Client
  Maven will skip the client tests if the localhost server is not found.

Note: There is an ant build but it may not work in the OpenPhacts branch(es)
      As of writing the ant build is broken in most OpenPhacts branch(es)
	
OPS Webservice Setup.
--------------------

Make sure config files, SQL database and rdf parent directory are setup (and accessible) as above.

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
