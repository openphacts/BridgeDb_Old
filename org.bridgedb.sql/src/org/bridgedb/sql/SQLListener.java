// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  OpenPhacts
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.bridgedb.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourceOverwriteLevel;
import org.bridgedb.mapping.MappingListener;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.utils.BridgeDBException;

/**
 * This is the root class of the SQL stack.
 * It handles the creation of all the tables and handles all inserts, except those only required for URL and UriSpaces
 * 
 * See CreateSQLTables method for an explanation of the tables.
 * 
 * @author Christian
 */
public class SQLListener extends SQLBase implements MappingListener{

    //Numbering should not clash with any GDB_COMPAT_VERSION;
	public static final int SQL_COMPAT_VERSION = 23;
  
    //Maximumn size in database
    protected static final int SYSCODE_LENGTH = 100;
    protected static final int FULLNAME_LENGTH = 100;
    private static final int MAINURL_LENGTH = 100;
    private static final int URLPATTERN_LENGTH = 400;
    protected static final int ID_LENGTH = 100;
    private static final int TYPE_LENGTH = 100;
    private static final int URNBASE_LENGTH = 100;
    protected static final int PREDICATE_LENGTH = 100;
    protected static final int JUSTIFICATION_LENGTH = 150;

    private static final int LINK_SET_ID_LENGTH = 100;
    private static final int KEY_LENGTH= 100; 
    private static final int PROPERTY_LENGTH = 100;
    private static final int MAX_BLOCK_SIZE = 1000;
protected static final int MAPPING_URI_LENGTH = 200;
    
    //static final String DATASOURCE_TABLE_NAME = "DataSource";
    public static final String CHAIN_TABLE_NAME = "chain";
    static final String INFO_TABLE_NAME = "info";  //Do not change as used by RDG packages as well
    static final String MAPPING_TABLE_NAME = "mapping";
    public static final String MAPPING_SET_TABLE_NAME = "mappingSet";
    static final String PROPERTIES_TABLE_NAME = "properties";
    static final String VIA_TABLE_NAME = "via";

    public static final String CHAIN_ID_COLUMN_NAME = "chainId";
    public static final String ID_COLUMN_NAME = "id";
    static final String IS_PUBLIC_COLUMN_NAME = "isPublic";
    public static final String JUSTIFICATION_COLUMN_NAME = "justification";
    static final String KEY_COLUMN_NAME = "theKey";
    static final String MAPPING_NAME_COLUMN_NAME = "mappingName";
    public static final String MAPPING_SET_ID_COLUMN_NAME = "mappingSetId";
    static final String MAPPING_SET_DOT_ID_COLUMN_NAME = MAPPING_SET_TABLE_NAME + "." + ID_COLUMN_NAME;

    static final String PREDICATE_COLUMN_NAME = "predicate";
    static final String PROPERTY_COLUMN_NAME = "property";
    static final String SCHEMA_VERSION_COLUMN_NAME = "schemaversion"; //Do not change as used by RDG packages as well
    static final String SOURCE_DATASOURCE_COLUMN_NAME = "sourceDataSource";
    static final String SOURCE_ID_COLUMN_NAME = "sourceId";
    static final String SYMMETRIC_COLUMN_NAME = "symmetric";
    static final String SYSCODE_COLUMN_NAME = "sysCode";
    static final String TARGET_ID_COLUMN_NAME = "targetId";
    static final String TARGET_DATASOURCE_COLUMN_NAME = "targetDataSource";
    static final String TYPE_COLUMN_NAME = "type";
    static final String URL_PATTERN_COLUMN_NAME = "urlPattern";
    static final String URN_BASE_COLUMN_NAME = "urnBase";
    static final String LAST_UDPATES = "LastUpdates";
    static final String VIA_DATASOURCE_COLUMN_NAME = "viaDataSource";
    
    static final String FULL_NAME_PREFIX = "_";
    
    private final int blockSize;
    private int blockCount = 0;
    private int insertCount = 0;
    private int doubleCount = 0;  
    private StringBuilder insertQuery;
    protected final String autoIncrement;
    
    static final Logger logger = Logger.getLogger(SQLListener.class);

    public SQLListener(boolean dropTables) throws BridgeDBException{
        super();
        DataSource.setOverwriteLevel(DataSourceOverwriteLevel.STRICT);
        BridgeDBRdfHandler.init();
        this.autoIncrement = SqlFactory.getAutoIncrementCommand();
        if (dropTables){
            dropSQLTables();
            createSQLTables();
            logger.info("Recreated all tables!");
        } else {
            checkVersion();
            //loadDataSources();
        }
        if (SqlFactory.supportsMultipleInserts()){
            blockSize = MAX_BLOCK_SIZE;
        } else {
            blockSize = 1;
        }
        //Starting with a block will cause a new query to start.
        blockCount = blockSize ;
        insertCount = 0;
        doubleCount = 0;    
    }
        
    @Override
    public synchronized int registerMappingSet(DataSource source, String predicate, String justification, 
            DataSource target, String mappingName, boolean symetric, Set<String> viaLabels, Set<Integer> chainedLinkSets) throws BridgeDBException {
        //checkDataSourceInDatabase(source);
        //checkDataSourceInDatabase(target);
        int forwardId = registerMappingSet(source, target, predicate, justification, mappingName, 0);
        registerVia(forwardId, viaLabels);
        registerChain(forwardId, chainedLinkSets);
        if (symetric){
            int symetricId = registerMappingSet(target, source, predicate, justification, mappingName, forwardId);
            registerVia(symetricId, viaLabels);
            registerChain(symetricId, chainedLinkSets);
        }
        return forwardId;
    }

    final String getDataSourceKey(DataSource dataSource){
        if (dataSource.getSystemCode() == null){
            return insertEscpaeCharacters(FULL_NAME_PREFIX + dataSource.getFullName());
        } else {
            return insertEscpaeCharacters(dataSource.getSystemCode());
        }
    }
    
    final DataSource keyToDataSource(String key){
        if (key.startsWith(FULL_NAME_PREFIX)){
            String fullName = key.substring(FULL_NAME_PREFIX.length());
            return DataSource.getByFullName(fullName);
        } else {
            return DataSource.getBySystemCode(key);
        }
    }
    
    /**
     * One way registration of Mapping Set.
     * @param justification 
     * 
     */
    protected final int registerMappingSet(DataSource source, DataSource target, String predicate, String justification, 
            String mappingName, int symmetric) throws BridgeDBException {
        String mappingUri = null;
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(MAPPING_SET_TABLE_NAME);
        query.append(" ("); 
        query.append(SOURCE_DATASOURCE_COLUMN_NAME);
        query.append(", ");
        query.append(PREDICATE_COLUMN_NAME);
        query.append(", "); 
        query.append(JUSTIFICATION_COLUMN_NAME);
        query.append(", ");
        query.append(TARGET_DATASOURCE_COLUMN_NAME); 
        if (mappingName != null && !mappingName.isEmpty()){
            query.append(", ");
            query.append(MAPPING_NAME_COLUMN_NAME); 
        }
        query.append(") VALUES ('"); 
        query.append(getDataSourceKey(source));
        query.append("', '");
        query.append(predicate);
        query.append("', '");
        query.append(justification);
        query.append("', '");
        query.append(getDataSourceKey(target));
        if (mappingName != null && !mappingName.isEmpty()){
            query.append("', '");
            query.append(mappingName);
        }
        query.append("')");
        Statement statement = createStatement();
        try {
            statement.executeUpdate(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error inserting link with " + query.toString(), ex);
        }
        statement = createStatement();
        int autoinc = 0;
        String getId = "SELECT @@identity";
        try {
            ResultSet rs = statement.executeQuery(getId);
            if (rs.next())
            {
                autoinc = rs.getInt(1);
            } else {
                throw new BridgeDBException ("No result getting new indetity with " + getId);
            }
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error getting new indetity with " + getId, ex);
        }
        logger.info("Registered new Mapping " + autoinc + " from " + getDataSourceKey(source) + " to " + getDataSourceKey(target));
        return autoinc;
    }

protected void registerVia(int mappingSetId, Set<String> viaLabels) throws BridgeDBException {
        if (viaLabels == null || viaLabels.isEmpty() ){
            return;
        }
        Iterator<String> labels = viaLabels.iterator(); 
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(VIA_TABLE_NAME);
        insert.append(" (");
        insert.append(MAPPING_SET_ID_COLUMN_NAME);
        insert.append(", ");
        insert.append(VIA_DATASOURCE_COLUMN_NAME);
        insert.append(") VALUES ");
        insert.append("('");
        insert.append(mappingSetId);
        insert.append("', '");
        insert.append(labels.next());
        insert.append("')");
        while (labels.hasNext()){
            insert.append(", ('");
            insert.append(mappingSetId);
            insert.append("', '");
            insert.append(labels.next());
            insert.append("')");        
        }
        Statement statement = createStatement();
        try {
            statement.executeUpdate(insert.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error inserting via with " +  insert, ex);
        }
    }

    
protected void registerChain(int mappingSetId, Set<Integer> chainedLinkSets) throws BridgeDBException {
        if (chainedLinkSets == null || chainedLinkSets.isEmpty() ){
            return;
        }
        Iterator<Integer> chainLinkSetId = chainedLinkSets.iterator(); 
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(CHAIN_TABLE_NAME);
        insert.append(" (");
        insert.append(MAPPING_SET_ID_COLUMN_NAME);
        insert.append(", ");
        insert.append(CHAIN_ID_COLUMN_NAME);
        insert.append(") VALUES ");
        insert.append("('");
        insert.append(mappingSetId);
        insert.append("', ");
        insert.append(chainLinkSetId.next());
        insert.append(")");
        while (chainLinkSetId.hasNext()){
            insert.append(", ('");
            insert.append(mappingSetId);
            insert.append("', ");
            insert.append(chainLinkSetId.next());
            insert.append(")");        
        }
        Statement statement = createStatement();
        try {
            statement.executeUpdate(insert.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error inserting via with " +  insert, ex);
        }
    }

    @Override
    public synchronized void closeInput() throws BridgeDBException {
        runInsert();
        Statement statement = createStatement();
        try {
            statement.execute("analyze table mapping");
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error analyze table mapping ", ex);
        }        
        insertQuery = null;
        logger.info("Finished processing linkset");
        if (possibleOpenConnection != null){
            try {
                //possibleOpenConnection.commit();
                possibleOpenConnection.close();
            } catch (SQLException ex) {
               throw new BridgeDBException ("Error closing connection ", ex);
            }
        }
        //Starting with a block will cause a new query to start.
        blockCount = blockSize ;
        insertCount = 0;
        doubleCount = 0;   
        updateLastUpdated();
        closeConnection();
        logger.debug("Closed input");
    }
    
    @Override
    public synchronized void insertLink(String sourceId, String targetId, int mappingSet, boolean symetric) throws BridgeDBException {
        insertLink(sourceId, targetId, mappingSet);
        if (symetric){
            insertLink(targetId, sourceId, mappingSet + 1);
        }
    }
    
    public static String addEscapeCharacters(String original){
        String result = original.replaceAll("\\\\", "\\\\\\\\");
        result = result.replaceAll("'", "\\\\'");
        result = result.replaceAll("\"", "\\\\\"");
        return result;
    }
    /**
     * One way insertion of a link.
     * <p>
     * May store link updates in a StringBuilder to make one large call rather than many small calls.
     */
    private void insertLink(String sourceId, String targetId, int mappingSetId) throws BridgeDBException{
        if (blockCount >= blockSize){
            runInsert();
            insertQuery = new StringBuilder("INSERT INTO ");
            insertQuery.append(MAPPING_TABLE_NAME);
            insertQuery.append(" (");
            insertQuery.append(SOURCE_ID_COLUMN_NAME);
            insertQuery.append(", ");
            insertQuery.append(TARGET_ID_COLUMN_NAME);
            insertQuery.append(", ");
            insertQuery.append(MAPPING_SET_ID_COLUMN_NAME);
            insertQuery.append(") VALUES ");
        } else {
            try {
                insertQuery.append(", ");        
            } catch (NullPointerException ex){
                throw new BridgeDBException("Please run openInput() before insertLink");
            }
        }
        blockCount++;
        insertQuery.append("('");
        insertQuery.append(addEscapeCharacters(sourceId));
        insertQuery.append("', '");
        insertQuery.append(addEscapeCharacters(targetId));
        insertQuery.append("', ");
        insertQuery.append(mappingSetId);
        insertQuery.append(")");

    }

    /**
     * Runs the insert using the StringBuilder built up by one or more Insert calls.
     * @throws BridgeDBException 
     */
    private void runInsert() throws BridgeDBException{
        if (insertQuery != null) {
           try {
                Statement statement = createStatement();
                //long start = new Date().getTime();
                int changed = statement.executeUpdate(insertQuery.toString());
                //Reporter.report("insertTook " + (new Date().getTime() - start));
                insertCount += changed;
                doubleCount += blockCount - changed;
           } catch (SQLException ex) {
                System.err.println(ex);
                throw new BridgeDBException ("Error inserting link ", ex, insertQuery.toString());
            }
        }   
        insertQuery = null;
        blockCount = 0;
    }
    
     /**
	 * Excecutes several SQL statements to drop the tables 
	 * @throws BridgeDBException 
	 */
	protected void dropSQLTables() throws BridgeDBException
	{
 		dropTable(INFO_TABLE_NAME);
 		dropTable(MAPPING_TABLE_NAME);
 		dropTable(MAPPING_SET_TABLE_NAME);
 		dropTable(PROPERTIES_TABLE_NAME);
        dropTable(VIA_TABLE_NAME);
        dropTable(CHAIN_TABLE_NAME);
     }
    
    /**
     * Drops a single table if it exists.
     * <p>
     * Virtuosos appears not to have the if exists syntax so errors are assumed to be table not found.
     * @param name
     * @throws BridgeDBException 
     */
    protected void dropTable(String name) throws BridgeDBException{
        //"IF NOT EXISTS" is unsupported 
       Statement sh = createStatement();
        try 
        {
            sh.execute("DROP TABLE " + name);
            sh.close();
        } catch (SQLException e) {
            System.err.println("Unable to drop table " + name + " assuming it does not exist");
        }
    }
 
    /**
	  * Excecutes several SQL statements to create the tables and indexes in the database.
      * <p>
      * Table "info" is a control table used by all database version of BridgeDB, inlcuing none OPS ones.
      * If verifies that database is called with the code that matches the schema version.
      * <p>
      * Table DataSource holds the org.bridgedb.DataSource registry between deployments of the service.
      * The whole table is loaded into the DataSource.class regisrty in the constructor.
      * @See org.bridgedb.DataSource.
      * <p>
      * Table "mapping" holds the Id part of the mapping. (The DataSource part is handled by MappingSet)
      * The "id" field is purely for provenace tracking. Ie getting a particular mapping based on its Id.
      * Mappings are only looked up in one direction, so ids are specically source and target.
      * "mappingSetId" is a foreign key to the "mappingSet" table.
      * <p>
      * Table "mappingSet" holds the DataSource part of each Mapping.
      * Specifically it holds the SysCodes for which org.bridgedb.DataSource objects can be looked up.
      * The Ops version will also map SysCodes to UriSpace(s).
      * "predicate" is purely for provenace. (but could be used for Ops Profiles)
      * "isTransitive" is a flag set at the time of loading to identify mappinSet generated using transativity of other sets.
      *     Currently only used by OPS to draw a different line in the graphviz but could be used by lenses.
      * "mappingCount" is a precomputed value @see countLinks() method.
      * <p>
      * Table "properties" underpins bridgeDB properties methods.
      * "isPublic" field dettermines if the key will be returned by the getKeys() method.
	  * @throws BridgeDBException 
	  */
	protected void createSQLTables() throws BridgeDBException
	{
        //"IF NOT EXISTS " is not supported
        String query = "";
		try 
		{
			Statement sh = createStatement();
 			sh.execute("CREATE TABLE                            "
					+ INFO_TABLE_NAME 
					+ " (    " + SCHEMA_VERSION_COLUMN_NAME + " INTEGER PRIMARY KEY	"
                    + ")");
  			sh.execute( //Add compatibility version of GDB
					"INSERT INTO " + INFO_TABLE_NAME + " VALUES ( " + SQL_COMPAT_VERSION + ")");
            query = "CREATE TABLE " + MAPPING_TABLE_NAME 
                    + "( " + SOURCE_ID_COLUMN_NAME      + " VARCHAR(" + ID_LENGTH + ") NOT NULL, "
        			+ "  " + TARGET_ID_COLUMN_NAME      + " VARCHAR(" + ID_LENGTH + ") NOT NULL, " 
                    + "  " + MAPPING_SET_ID_COLUMN_NAME + " INT(" + LINK_SET_ID_LENGTH + "), "
                    + "INDEX `setFind` (" + MAPPING_SET_ID_COLUMN_NAME + "), " 
                    + "INDEX `sourceFind` (" + SOURCE_ID_COLUMN_NAME + "), " 
                    + "INDEX `sourceMappingSetFind` (" + MAPPING_SET_ID_COLUMN_NAME + ", " + SOURCE_ID_COLUMN_NAME + ") "
                    + " ) " ;
			sh.execute(query);
         	query =	"CREATE TABLE " + MAPPING_SET_TABLE_NAME 
                    + " (" + ID_COLUMN_NAME                   + " INT " + autoIncrement + " PRIMARY KEY, " 
                        + SOURCE_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ") NOT NULL, "
                        + PREDICATE_COLUMN_NAME         + " VARCHAR(" + PREDICATE_LENGTH + ") NOT NULL, "
                        + JUSTIFICATION_COLUMN_NAME     + " VARCHAR(" + JUSTIFICATION_LENGTH + ") NOT NULL, "
                        + TARGET_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ")  NOT NULL, "
                        + MAPPING_NAME_COLUMN_NAME  + " VARCHAR(" + MAPPING_URI_LENGTH + ") "
					+ " ) "; 
            sh.execute(query);
            sh.execute ("CREATE TABLE  "
                    + "    " + PROPERTIES_TABLE_NAME
                    + "(   " + KEY_COLUMN_NAME +   "      VARCHAR(" + KEY_LENGTH + ") NOT NULL, "
                    + "    " + PROPERTY_COLUMN_NAME + "    VARCHAR(" + PROPERTY_LENGTH + ") NOT NULL, "
                    + "    " + IS_PUBLIC_COLUMN_NAME + "    SMALLINT "
					+ " ) "); 
         	query =	"CREATE TABLE " + VIA_TABLE_NAME 
                    + " (" + MAPPING_SET_ID_COLUMN_NAME + " INT(" + LINK_SET_ID_LENGTH + ") NOT NULL, "
                    + "     " + VIA_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ")  NOT NULL "
					+ " ) "; 
            sh.execute(query);
         	query =	"CREATE TABLE " + CHAIN_TABLE_NAME 
                    + " (" + MAPPING_SET_ID_COLUMN_NAME + " INT(" + LINK_SET_ID_LENGTH + ") NOT NULL, "
                    + "     " + CHAIN_ID_COLUMN_NAME + " INT(" + LINK_SET_ID_LENGTH + ") NOT NULL"
					+ " ) "; 
            sh.execute(query);
            sh.close();
		} catch (SQLException e){
 			throw new BridgeDBException ("Error creating the tables using " + query, e);
		}
	}
     
    /**
     * Checks that the schema is for this version.
     * 
     * @throws BridgeDBException If the schema version is not the expected one.
     */
	private void checkVersion() throws BridgeDBException
	{
        Statement stmt = createStatement();
        ResultSet r = null;
        int version = 0;
        try {
            r = stmt.executeQuery("SELECT schemaversion FROM info");
            if(r.next()) version = r.getInt(1);
        } catch (SQLException ex) {
            throw new BridgeDBException("Error checking the version. ", ex);
        }
		finally
		{
            if (r != null) try { r.close(); } catch (SQLException ignore) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException ignore) {}
		}
        if (version == SQL_COMPAT_VERSION) return;
 		switch (version)
		{
    		case 2:
        		throw new BridgeDBException("Please use the SimpleGdbFactory in the org.bridgedb.rdb package");
            case 3:
                throw new BridgeDBException("Please use the SimpleGdbFactory in the org.bridgedb.rdb package");
            //NB add future schema versions here
            default:
                throw new BridgeDBException ("Unrecognized schema version '" + version + "', please make sure you have the latest " +
					"version of this software and databases");
		}		
	}
	   
    /*
     * Verifies that the Data Source is saved in the database.
     * Updating or adding the Data Source as required.
     * <p>
     * This is required to allow the DataSource registry to be rebuilt if the service is restarted.
     * @param source A DataSource to check
     * @throws BridgeDBException 
     * /
    void checkDataSourceInDatabase(DataSource source) throws BridgeDBException{
        Statement statement = this.createStatement();
        String query = "SELECT " + SYSCODE_COLUMN_NAME
                + "   from " + DATASOURCE_TABLE_NAME
                + "   where "
                + "      " + SYSCODE_COLUMN_NAME + " = '" + getDataSourceKey(source) + "'"; 
        boolean found;
        try {
            ResultSet rs = statement.executeQuery(query);
            found = rs.next();
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to check provenace " +  query, ex);
        }
        if (found){
            updateDataSource(source);
        } else {
            writeDataSource(source);
        }
    }*/
    
    /*
     * Adds a DataSource to the SQL database.
     * <p>
     * By the time this methods is called the assumption is that the DataSource did not yet exist in the database.
     * @param source DataSource to save.
     * @throws BridgeDBException 
     * /
    private void writeDataSource(DataSource source) throws BridgeDBException{
        StringBuilder insert = new StringBuilder ("INSERT INTO ");
        insert.append(DATASOURCE_TABLE_NAME);
        insert.append(" ( ");
        insert.append(SYSCODE_COLUMN_NAME);
        insert.append(", ");
        insert.append(IS_PRIMARY_COLUMN_NAME);
        StringBuilder values = new StringBuilder ("Values ( ");
        if (getDataSourceKey(source).length() > SYSCODE_LENGTH ){
            throw new BridgeDBException("Maximum length supported for SystemCode is " + SYSCODE_LENGTH + 
                    " so unable to save " + getDataSourceKey(source));
        }
        values.append("'");
        values.append(insertEscpaeCharacters(getDataSourceKey(source)));
        values.append("' , ");
        if (source.isPrimary()){
            values.append (1);
        } else {
           values.append (0);
        }
        String value = source.getFullName(); 
        if (value != null && !value.isEmpty()){
            if (value.length() > FULLNAME_LENGTH){
                throw new BridgeDBException("Maximum length supported for fullName is " + FULLNAME_LENGTH + 
                        " so unable to save " + value);
            }
            insert.append (", ");
            insert.append (FULL_NAME_COLUMN_NAME);
            insert.append (" ");
            values.append (", '");
            values.append (insertEscpaeCharacters(value));
            values.append ("' ");
        }
        value = source.getMainUrl();
        if (value != null && !value.isEmpty()){
            if (value.length() > MAINURL_LENGTH){
                throw new BridgeDBException("Maximum length supported for mainUrl is " + MAINURL_LENGTH + 
                        " so unable to save " + value);
            }
            insert.append (", ");
            insert.append (MAIN_URL_COLUMN_NAME);
            insert.append (" ");
            values.append (", '");
            values.append (insertEscpaeCharacters(value));
            values.append ("' ");
        }
        value = source.getUrl("$id");
        if (value != null && !value.isEmpty()){
            if (value.length() > URLPATTERN_LENGTH){
                throw new BridgeDBException("Maximum length supported for URLPattern is " + URLPATTERN_LENGTH + 
                        " so unable to save " +value.length() + " " + value);
            }
            insert.append (", ");
            insert.append (URL_PATTERN_COLUMN_NAME);
            insert.append (" ");
            values.append (", '");
            values.append (insertEscpaeCharacters(value));
            values.append ("' ");
        }
        value = source.getExample().getId();
        if (value != null && !value.isEmpty()){
            if (value.length() > ID_LENGTH){
                throw new BridgeDBException("Maximum length supported for exampleId is " + ID_LENGTH + 
                        " so unable to save " + value);
            }
            insert.append (", ");
            insert.append (ID_EXAMPLE_COLUMN_NAME);
            insert.append (" ");
            values.append (", '");
            values.append (insertEscpaeCharacters(value));
            values.append ("' ");
        }
        value = source.getType();
        if (value != null && !value.isEmpty()){
            if (value.length() > TYPE_LENGTH){
                throw new BridgeDBException("Maximum length supported for type is " + TYPE_LENGTH + 
                        " so unable to save " + value);
            }
            insert.append (", ");
            insert.append (TYPE_COLUMN_NAME);
            insert.append (" ");
            values.append (", '");
            values.append (insertEscpaeCharacters(value));
            values.append ("' ");
        }
        value = source.getURN("");
        //remove the :
        value = value.substring(0, value.length()-1);
        if (value != null && !value.isEmpty()){
            if (value.length() > URNBASE_LENGTH){
                throw new BridgeDBException("Maximum length supported for urnBase is " + URNBASE_LENGTH + 
                        " so unable to save " + value);
            }
            insert.append (", ");
            insert.append (URN_BASE_COLUMN_NAME);
            insert.append (" ");
            values.append (", '");
            values.append (insertEscpaeCharacters(value));
            values.append ("' ");
        }
        //if (source.getOrganism() != null){
        //    throw new BridgeDBException("Sorry DataSource oraginism filed is upsupported");
        //}
        Statement statement = this.createStatement();
        String update = insert.toString() + ") " + values.toString() + " )";
        try {
            statement.executeUpdate(update);
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to writeDataSource " + update, ex);
        }
    }*/

    /**
     * Writes all booleans as 1 or 0 because Virtuoso appears not able to handle the boolean type.
     * 1 and 0 can be read as booleans even when saved as integers.
     * @param bool Value to be saved.
     * @return 
     */
    private String booleanIntoQuery(boolean bool){
        if (bool) return "1";
        return "0";
    }
    
    /*
     * Updates the DataBase record assoicated with a DataSource that has previous been dettermined to already exist.
     * 
     * @param source DataSource whose info will be updated/ confirmed.
     * @throws BridgeDBException 
     * /
    private void updateDataSource(DataSource source) throws BridgeDBException{
        StringBuilder update = new StringBuilder("UPDATE ");
        update.append (DATASOURCE_TABLE_NAME);
        update.append (" SET ");
        update.append (IS_PRIMARY_COLUMN_NAME);
        update.append (" = ");
        update.append (booleanIntoQuery(source.isPrimary()));
        update.append (" ");       
        String value = source.getFullName();
        if (value != null && !value.isEmpty()){
            if (value.length() > FULLNAME_LENGTH){
                throw new BridgeDBException("Maximum length supported for fullName is " + FULLNAME_LENGTH + 
                        " so unable to save " + value);
            }
            update.append (", ");
            update.append (FULL_NAME_COLUMN_NAME);
            update.append (" = '");
            update.append (insertEscpaeCharacters(value));
            update.append ("' ");
        }       
        value = source.getMainUrl();
        if (value != null && !value.isEmpty()){
            if (value.length() > MAINURL_LENGTH){
                throw new BridgeDBException("Maximum length supported for mainUrl is " + MAINURL_LENGTH + 
                        " so unable to save " + value);
            }
            update.append (", ");
            update.append (MAIN_URL_COLUMN_NAME);
            update.append (" = '");
            update.append (insertEscpaeCharacters(value));
            update.append ("' ");
        }
        value = source.getUrl("$id");
        if (value != null && !value.isEmpty()){
            if (value.length() > URLPATTERN_LENGTH){
                throw new BridgeDBException("Maximum length supported for URLPattern is " + URLPATTERN_LENGTH + 
                        " so unable to save " + value);
            }
            update.append (", ");
            update.append (URL_PATTERN_COLUMN_NAME);
            update.append (" = '");
            update.append (insertEscpaeCharacters(value));
            update.append ("' ");
        }
        value = source.getExample().getId();
        if (value != null && !value.isEmpty()){
            if (value.length() > ID_LENGTH){
                throw new BridgeDBException("Maximum length supported for exampleId is " + ID_LENGTH + 
                        " so unable to save " + value);
            }
            update.append (", ");
        update.append (ID_EXAMPLE_COLUMN_NAME);
        update.append (" = '");
            update.append (insertEscpaeCharacters(value));
            update.append ("' ");
        }
        value = source.getType();
        if (value != null && !value.isEmpty()){
            if (value.length() > TYPE_LENGTH){
                throw new BridgeDBException("Maximum length supported for type is " + TYPE_LENGTH + 
                        " so unable to save " + value);
            }
            update.append (", ");
        update.append (TYPE_COLUMN_NAME);
        update.append (" = '");
            update.append (insertEscpaeCharacters(value));
            update.append ("' ");
        }
        value = source.getURN("");
        //remove the :
        value = value.substring(0, value.length()-1);
        if (value != null && !value.isEmpty()){
            if (value.length() > URNBASE_LENGTH){
                throw new BridgeDBException("Maximum length supported for urnBase is " + URNBASE_LENGTH + 
                        " so unable to save " + value);
            }
            update.append (", ");
        update.append (URN_BASE_COLUMN_NAME);
        update.append (" = '");
            update.append (insertEscpaeCharacters(value));
            update.append ("' ");
        }
        if (getDataSourceKey(source).length() > SYSCODE_LENGTH ){
            throw new BridgeDBException("Maximum length supported for SystemCode is " + SYSCODE_LENGTH + 
                    " so unable to save " + getDataSourceKey(source));
        }
        update.append ("WHERE ");
        update.append (SYSCODE_COLUMN_NAME);
        update.append ("  = '");
        update.append (getDataSourceKey(source));
        update.append ("' ");
        //if (source.getOrganism() != null){
        //    throw new BridgeDBException("Sorry DataSource oraginism feildd is upsupported");
        //}
        Statement statement = this.createStatement();
        try {
            statement.executeUpdate(update.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to updateDataSource " + update, ex);
        }
    }*/

    /**
     * Updates the property LastUpdayes with the current date and time.
     * 
     * @throws BridgeDBException 
     */
    private void updateLastUpdated() throws BridgeDBException {
        String date = new Date().toString();
        putProperty(LAST_UDPATES, date);
    }

    public synchronized void putProperty(String key, String value) throws BridgeDBException {
        String delete = "DELETE from " + PROPERTIES_TABLE_NAME + " where " + KEY_COLUMN_NAME + " = '" + key + "'";
        Statement statement = this.createStatement();
        try {
            statement.executeUpdate(delete.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Error Deleting property " + delete, ex);
        }
        String update = "INSERT INTO " + PROPERTIES_TABLE_NAME
                    + " (" + KEY_COLUMN_NAME + ", " + PROPERTY_COLUMN_NAME + ", " + IS_PUBLIC_COLUMN_NAME + " )" 
                    + " VALUES ('" + key + "', '" + value  + "' , 1)  ";
        try {
            statement.executeUpdate(update.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Error inserting Property " + update, ex);
        }
    }

    /*
     * Loads all the DataSources stored in the database into the DataSource registry.
     * <p>
     * This together with checkDataSourceInDatabase ensures that the DataSource registry is constant between runs.
     * @throws BridgeDBException 
     * /
    private void loadDataSources() throws BridgeDBException{
        try {
            Statement statement = this.createStatement();
            String query = "SELECT " + SYSCODE_COLUMN_NAME + ", " + IS_PRIMARY_COLUMN_NAME + ", " 
                    + FULL_NAME_COLUMN_NAME + ", " + MAIN_URL_COLUMN_NAME + ", " + URL_PATTERN_COLUMN_NAME + ", " 
                    + ID_EXAMPLE_COLUMN_NAME + ", " + TYPE_COLUMN_NAME + ", " + URN_BASE_COLUMN_NAME
                    + "   from " + DATASOURCE_TABLE_NAME;           
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()){
                String sysCode = rs.getString(SYSCODE_COLUMN_NAME);
                if (sysCode.equals("null")){
                    sysCode = null;
                }
                String fullName = rs.getString(FULL_NAME_COLUMN_NAME);
                DataSource.Builder builder = DataSource.register(sysCode, fullName);
                builder.primary(rs.getBoolean(IS_PRIMARY_COLUMN_NAME));
                String mainUrl = rs.getString(MAIN_URL_COLUMN_NAME);
                if (mainUrl != null && !mainUrl.isEmpty() && mainUrl.equals("null")){
                    builder.mainUrl(mainUrl);
                }
                String urlPattern = rs.getString(URL_PATTERN_COLUMN_NAME);
                if (urlPattern != null && !urlPattern.isEmpty() && urlPattern.equals("null")){
                    builder.urlPattern(urlPattern);
                }
                String idExample = rs.getString(ID_EXAMPLE_COLUMN_NAME);
                if (idExample != null && !idExample.isEmpty() && idExample.equals("null")){
                    builder.idExample(idExample);
                }
                String type = rs.getString(TYPE_COLUMN_NAME);
                if (type != null && !type.isEmpty() && type.equals("null")){
                    builder.type(type);
                }
                String urnBase = rs.getString(URN_BASE_COLUMN_NAME);
                if (urnBase != null && !urnBase.isEmpty() && urnBase.equals("null")){
                    builder.urnBase(urnBase);
                }
            }
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to load DataSources");
        }
    }*/
    
}
