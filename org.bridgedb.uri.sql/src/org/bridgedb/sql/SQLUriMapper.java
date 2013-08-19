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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.statistics.DataSetInfo;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.Lens;
import org.bridgedb.uri.MappingsBySet;
import org.bridgedb.uri.Mapping;
import org.bridgedb.uri.UriListener;
import org.bridgedb.uri.UriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.openrdf.rio.RDFHandlerException;

/**
 * Implements the UriMapper and UriListener interfaces using SQL.
 *
 * Takes into accounts the specific factors for the SQL version being used.
 *
 * @author Christian
 */
public class SQLUriMapper extends SQLIdMapper implements UriMapper, UriListener {

    private static final int PREFIX_LENGTH = 400;
    private static final int POSTFIX_LENGTH = 100;
//    private static final int LENS_URI_LENGTH = 100;
    private static final int MIMETYPE_LENGTH = 50;
    private static final int CREATED_BY_LENGTH = 150;
    
    private static final String URI_TABLE_NAME = "uri";
    private static final String MIMETYPE_TABLE_NAME = "mimeType";
//    private static final String LENS_JUSTIFICATIONS_TABLE_NAME = "lensJustifications";
//    private static final String LENS_TABLE_NAME = "lens";
    
    private static final String CREATED_BY_COLUMN_NAME = "createdBy";
    private static final String CREATED_ON_COLUMN_NAME = "createdOn";
    private static final String DATASOURCE_COLUMN_NAME = "dataSource";
    private static final String PREFIX_COLUMN_NAME = "prefix";
 //   private static final String LENS_ID_COLUMN_NAME = "lensId";
//    private static final String LENS_URI_COLUMN_NAME = "lensUri";
    private static final String POSTFIX_COLUMN_NAME = "postfix";
    private static final String MIMETYPE_COLUMN_NAME = "mimetype";
    private static final String NAME_COLUMN_NAME = "name";
    
    private static SQLUriMapper mapper = null;
    private HashMap<Integer,UriPattern> subjectUriPatterns;
    private HashMap<Integer,UriPattern> targetUriPatterns;
    static final Logger logger = Logger.getLogger(SQLListener.class);

    public synchronized static SQLUriMapper getExisting() throws BridgeDBException{
        if (mapper == null){
            BridgeDBRdfHandler.init();
            mapper =  new SQLUriMapper(false);
            Lens.init(mapper);
        }
        return mapper;
    }
    
    public synchronized static SQLUriMapper createNew() throws BridgeDBException{
        BridgeDBRdfHandler.init();
        mapper =  new SQLUriMapper(true);
        return mapper;
    }

    /**
     * Creates a new UriMapper including BridgeDB implementation based on a connection to the SQL Database.
     *
     * @param dropTables Flag to determine if any existing tables should be dropped and new empty tables created.
     * @param sqlAccess The connection to the actual database. This could be MySQL, Virtuoso ect.
     *       It could also be the live database, the loading database or the test database.
     * @param specific Code to hold the things that are different between different SQL implementaions.
     * @throws BridgeDBException
     */
     private SQLUriMapper(boolean dropTables) throws BridgeDBException{
        super(dropTables);
        clearUriPatterns();
        Collection<UriPattern> patterns = UriPattern.getUriPatterns();
        for (UriPattern pattern:patterns){
            this.registerUriPattern(pattern);
        }
        checkDataSources();
        subjectUriPatterns = new HashMap<Integer,UriPattern>();
        targetUriPatterns = new HashMap<Integer,UriPattern>();
        Lens.init(this);
    }   
    
    @Override
	protected void dropSQLTables() throws BridgeDBException
	{
        super.dropSQLTables();
 		dropTable(URI_TABLE_NAME);
 		dropTable(MIMETYPE_TABLE_NAME);
// 		dropTable(LENS_TABLE_NAME);
// 		dropTable(LENS_JUSTIFICATIONS_TABLE_NAME);
    }
 
    @Override
	protected void createSQLTables() throws BridgeDBException
	{
        super.createSQLTables();
		try 
		{
			Statement sh = createStatement();
            sh.execute("CREATE TABLE " + URI_TABLE_NAME
                    + "  (  " + DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ") NOT NULL,   "
                    + "     " + PREFIX_COLUMN_NAME + " VARCHAR(" + PREFIX_LENGTH + ") NOT NULL, "
                    + "     " + POSTFIX_COLUMN_NAME + " VARCHAR(" + POSTFIX_LENGTH + ") NOT NULL "
                    + "  ) ");
            sh.execute("CREATE TABLE " + MIMETYPE_TABLE_NAME
                    + "  (  " + PREFIX_COLUMN_NAME + " VARCHAR(" + PREFIX_LENGTH + ") NOT NULL, "
                    + "     " + POSTFIX_COLUMN_NAME + " VARCHAR(" + POSTFIX_LENGTH + ") NOT NULL, "
                    + "     mimeType VARCHAR(" + MIMETYPE_LENGTH + ") NOT NULL "
                    + "  ) ");
/*            sh.execute("CREATE TABLE " + LENS_TABLE_NAME + " ( " 
            		+ LENS_ID_COLUMN_NAME + " INT " + autoIncrement + " PRIMARY KEY, " 
                    + LENS_URI_COLUMN_NAME + " VARCHAR(" + LENS_URI_LENGTH + "), "
            		+ NAME_COLUMN_NAME + " VARCHAR(" + FULLNAME_LENGTH + ") NOT NULL, " 
            		+ CREATED_ON_COLUMN_NAME + " DATETIME, " 
            		+ CREATED_BY_COLUMN_NAME + " VARCHAR(" + CREATED_BY_LENGTH + ") "
            		+ ")");
            sh.execute("CREATE TABLE " + LENS_JUSTIFICATIONS_TABLE_NAME + " ( " 
                    + LENS_URI_COLUMN_NAME + " VARCHAR(" + LENS_URI_LENGTH + ") NOT NULL, "
            		+ JUSTIFICATION_COLUMN_NAME + " VARCHAR(" + PREDICATE_LENGTH + ") NOT NULL " 
            		+ ")");
*/
            sh.close();
		} catch (SQLException e)
		{
			throw new BridgeDBException ("Error creating the tables ", e);
		}
	}
    
    private void checkDataSources() throws BridgeDBException{
        checkDataSources(SOURCE_DATASOURCE_COLUMN_NAME);
        checkDataSources(TARGET_DATASOURCE_COLUMN_NAME);
    }
    
    private void checkDataSources(String columnName) throws BridgeDBException{
        Set<String> toCheckNames = getPatternDataSources(columnName);
        for (String toCheckName:toCheckNames){
            UriPattern pattern = UriPattern.existingByPattern(toCheckName);
            if (pattern != null){
                DataSource ds = pattern.getDataSource();
                String code;
                if (ds.getSystemCode() == null && ds.getSystemCode().isEmpty()){
                    code = "_" + ds.getFullName();
                } else {
                    code = ds.getSystemCode();
                }
                replaceSysCode (toCheckName, code);
            }
        }
    }

    @Override
    public synchronized Set<Xref> mapID(Xref sourceXref, String lensUri, DataSource... tgtDataSource) throws BridgeDBException {
        if (tgtDataSource == null || tgtDataSource.length == 0){
            return mapID(sourceXref, lensUri);
        }
        if (tgtDataSource.length == 1){
            return mapID(sourceXref, lensUri, tgtDataSource[0]);
        }
        HashSet<Xref> results = new HashSet<Xref>();
        for (DataSource dataSource: tgtDataSource){
            results.addAll(mapID(sourceXref, lensUri, dataSource));
        }
        return results;
    }
    
    @Override
    public synchronized Set<Xref> mapID(Xref sourceXref, String lensUri, DataSource tgtDataSource) throws BridgeDBException {
        if (badXref(sourceXref)) {
            logger.warn("mapId called with a badXref " + sourceXref);
            return new HashSet<Xref>();
        }
        if (tgtDataSource == null){
            logger.warn("mapId called with a null tgtDatasource and " + sourceXref);
            return new HashSet<Xref>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingFromAndWhere(query, sourceXref, lensUri, tgtDataSource);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<Xref> results = resultSetToXrefSet(rs);
        //Add mapping to self
        if (sourceXref.getDataSource().equals(tgtDataSource)){
             results.add(sourceXref);
        }
        return results;
    }
    
    @Override
    public synchronized Set<Xref> mapID(Xref sourceXref, String lensUri) throws BridgeDBException {
        if (badXref(sourceXref)) {
            logger.warn("mapId called with a badXref " + sourceXref);
            return new HashSet<Xref>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingFromAndWhere(query, sourceXref, lensUri, null);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<Xref> results = resultSetToXrefSet(rs);
        //Add mapping to self
        results.add(sourceXref);
        return results;
    }
    
    @Override
    public synchronized Set<String> mapUri (Xref sourceXref, String lensUri, UriPattern... tgtUriPatterns) 
            throws BridgeDBException {
        if (tgtUriPatterns == null || tgtUriPatterns.length == 0){
            return mapUri (sourceXref, lensUri);
        }
        Set<String> results = new HashSet<String>();
        for (UriPattern tgtUriPattern:tgtUriPatterns){
            results.addAll(mapUri (sourceXref, lensUri, tgtUriPattern));
        }
        return results;
    }
 
    @Override
    public synchronized Set<String> mapUri (Xref sourceXref, String lensUri, UriPattern tgtUriPattern) 
            throws BridgeDBException {
        if (tgtUriPattern == null){
            logger.warn("mapUri called with a null tgtDatasource and " + sourceXref);
            return new HashSet<String>();
        }
        DataSource tgtDataSource = tgtUriPattern.getDataSource();
        Set<Xref> targetXrefs = mapID(sourceXref, lensUri, tgtDataSource);
        HashSet<String> results = new HashSet<String>();
        for (Xref target:targetXrefs){
            results.add (tgtUriPattern.getUri(target.getId()));
        }
        return results;
    }
    
    @Override
    public synchronized Set<String> mapUri (Xref sourceXref, String lensUri) 
            throws BridgeDBException {
        Set<Xref> targetXrefs = mapID(sourceXref, lensUri);
        HashSet<String> results = new HashSet<String>();
        for (Xref target:targetXrefs){
            results.addAll (toUris(target));
        }
        return results;
    }
    
    @Override
    public synchronized Set<String> mapUri (String sourceUri, String lensUri, UriPattern... tgtUriPatterns) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        if (tgtUriPatterns == null || tgtUriPatterns.length == 0){
            return mapUri (sourceUri, lensUri);
        }
        Set<String> results = new HashSet<String>();
        for (UriPattern tgtUriPattern:tgtUriPatterns){
            results.addAll(mapUri (sourceUri, lensUri, tgtUriPattern));
        }
        return results;
    }
 
    @Override
    public synchronized Set<String> mapUri (String sourceUri, String lensUri, UriPattern tgtUriPattern) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        if (tgtUriPattern == null){
            logger.warn("mapUri called with a null tgtDatasource and " + sourceUri);
            return new HashSet<String>();
        }
        DataSource tgtDataSource = tgtUriPattern.getDataSource();
        Set<Xref> targetXrefs = mapID(sourceXref, lensUri, tgtDataSource);
        HashSet<String> results = new HashSet<String>();
        for (Xref target:targetXrefs){
            results.add (tgtUriPattern.getUri(target.getId()));
        }
        return results;
    }
    
    @Override
    public synchronized Set<String> mapUri (String sourceUri, String lensUri) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        Set<Xref> targetXrefs = mapID(sourceXref, lensUri);
        HashSet<String> results = new HashSet<String>();
        for (Xref target:targetXrefs){
            results.addAll (toUris(target));
        }
        return results;
    }
    
    public synchronized MappingsBySet mapBySet(String sourceUri, String lensUri, UriPattern... tgtUriPatterns) throws BridgeDBException {
        if (tgtUriPatterns == null || tgtUriPatterns.length == 0){
            return mapBySet (sourceUri, lensUri);
        }
        MappingsBySet mappingsBySet = new MappingsBySet(lensUri);
        for (UriPattern tgtUriPattern:tgtUriPatterns){
            mapBySet(sourceUri, mappingsBySet, lensUri, tgtUriPattern);
        }
        return mappingsBySet;
        
    }
    @Override
    public synchronized MappingsBySet mapBySet(String sourceUri, String lensUri, UriPattern tgtUriPattern) throws BridgeDBException {
        MappingsBySet mappingsBySet = new MappingsBySet(lensUri);
        mapBySet(sourceUri, mappingsBySet, lensUri, tgtUriPattern) ;    
        return mappingsBySet;
    }

    private void mapBySet(String sourceUri, MappingsBySet mappingsBySet, String lensUri, 
            UriPattern tgtUriPattern) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        
        DataSource tgtDataSource = tgtUriPattern.getDataSource();
        ResultSet rs = mapBySetOnly(sourceXref, sourceUri, lensUri, tgtDataSource);       
        resultSetAddToMappingsBySet(rs, sourceUri, mappingsBySet, tgtUriPattern);           
        if (sourceXref.getDataSource().equals(tgtDataSource)){
            mappingsBySet.addMapping(sourceUri, sourceUri); 
        }
    }

    @Override
    public synchronized MappingsBySet mapBySet(Set<String> sourceUris, String lensUri, UriPattern... tgtUriPatterns) 
           throws BridgeDBException{
        MappingsBySet mappingsBySet = new MappingsBySet(lensUri);
        for (String sourceUri:sourceUris) {
            if (tgtUriPatterns == null || tgtUriPatterns.length == 0){
                mapBySet(sourceUri, mappingsBySet, lensUri);
            } else {
                for (UriPattern tgtUriPattern:tgtUriPatterns) {
                    mapBySet(sourceUri, mappingsBySet, lensUri, tgtUriPattern);
                }
            }
        }
        return mappingsBySet;           
    }
       
    @Override
    public synchronized MappingsBySet mapBySet(String sourceUri, String lensUri) 
            throws BridgeDBException {
        MappingsBySet mappingsBySet = new MappingsBySet(lensUri);
        mapBySet(sourceUri, mappingsBySet, lensUri);
        return mappingsBySet;
    }
    
    public synchronized MappingsBySet mapBySet(String sourceUri, MappingsBySet mappingsBySet, String lensUri) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        ResultSet rs = mapBySetOnly(sourceXref, sourceUri, lensUri, null);
        resultSetAddToMappingsBySet(rs, sourceUri, mappingsBySet);
        mappingsBySet.addMapping(sourceUri, toUris(sourceXref));
        return mappingsBySet;
    }

    private ResultSet mapBySetOnly(Xref sourceXref, String sourceUri, String lensUri, DataSource tgtDataSource) 
            throws BridgeDBException {
        StringBuilder query =  startMappingsBySetQuery();
        appendMappingFromAndWhere(query, sourceXref, lensUri, tgtDataSource);
        Statement statement = this.createStatement();
        try {
            return statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }         
    }

    @Override
    public synchronized Set<Mapping> mapFull (Xref sourceXref, String lensUri) throws BridgeDBException{
        if (badXref(sourceXref)) {
            logger.warn("mapId called with a badXref " + sourceXref);
            return new HashSet<Mapping>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingInfo(query);
        appendMappingFromAndWhere(query, sourceXref, lensUri, null);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<Mapping> results = resultSetToMappingSet(sourceXref, rs);
        //Add mapping to self
        results.add(new Mapping(sourceXref));
        //Add targetUris
        for (Mapping mapping: results){
            mapping.addTargetUris(toUris(mapping.getTarget()));
        }
        return results;
    }
    
    @Override
    public synchronized Set<Mapping> mapFull(String sourceUri, String lensUri) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        Set<Mapping> results = mapFull(sourceXref,  lensUri);
        for (Mapping result:results){
            result.addSourceUri(sourceUri);
        }
        return results;
    }

    @Override
	public synchronized Set<Mapping> mapFull (Xref sourceXref, String lensUri, DataSource tgtDataSource) throws BridgeDBException{
        Set<Mapping> results = mapPart(sourceXref, lensUri, tgtDataSource);
        //Add targetUris
        for (Mapping mapping: results){
            mapping.addTargetUris(toUris(mapping.getTarget()));
        }
        return results;
    }
 
	private Set<Mapping> mapPart (Xref sourceXref, String lensUri, DataSource tgtDataSource) throws BridgeDBException{
        if (badXref(sourceXref)) {
            logger.warn("mapId called with a badXref " + sourceXref);
            return new HashSet<Mapping>();
        }
        if (tgtDataSource == null){
            logger.warn("map called with a null tgtDatasource and " + sourceXref);
            return new HashSet<Mapping>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingInfo(query);
        appendMappingFromAndWhere(query, sourceXref, lensUri, tgtDataSource);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<Mapping> results = resultSetToMappingSet(sourceXref, rs);
        //Add map to self if correct
        if (sourceXref.getDataSource().equals(tgtDataSource)){
            results.add(new Mapping(sourceXref));
        }
        return results;
    }

    @Override
    public synchronized Set<Mapping> mapFull (Xref ref, String lensUri, DataSource... tgtDataSources) 
            throws BridgeDBException{
        if (tgtDataSources == null || tgtDataSources.length == 0){
            return mapFull (ref, lensUri);
        } else {
            Set<Mapping> results = new HashSet<Mapping>();
            for (DataSource tgtDataSource: tgtDataSources){
                results.addAll(mapFull(ref, lensUri, tgtDataSource));
            }
            return results;
        }
    }

    @Override
	public synchronized Set<Mapping> mapFull (Xref sourceXref, String lensUri, UriPattern tgtUriPattern) throws BridgeDBException {
        if (tgtUriPattern == null){
            logger.warn("mapFull called with a null tgtDatasource and " + sourceXref);
            return new HashSet<Mapping>();
        }
        DataSource tgtDataSource = tgtUriPattern.getDataSource();
        Set<Mapping> results = mapPart(sourceXref, lensUri, tgtDataSource);
        for (Mapping result:results){
            result.addTargetUri(tgtUriPattern.getUri(result.getTarget().getId()));
        }
        return results;
    }

    @Override
    public synchronized Set<Mapping> mapFull (Xref sourceXref, String lensUri, UriPattern... tgtUriPatterns) 
            throws BridgeDBException{
        if (tgtUriPatterns == null || tgtUriPatterns.length == 0){
            return mapFull (sourceXref, lensUri);
        } else {
            Set<Mapping> results = new HashSet<Mapping>();
            for (UriPattern tgtUriPattern: tgtUriPatterns){
                results.addAll(mapFull(sourceXref, lensUri, tgtUriPattern));
            }
            return results;
        }
    }

    @Override
    public synchronized Set<Mapping> mapFull(String sourceUri, String lensUri, UriPattern... tgtUriPatterns) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        Set<Mapping> results = mapFull(sourceXref,  lensUri, tgtUriPatterns);
        for (Mapping result:results){
            result.addSourceUri(sourceUri);
        }
        return results;
    }

    @Override
    public Set<Mapping> mapFull(String sourceUri, String lensUri, UriPattern tgtUriPattern) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        Set<Mapping> results = mapFull(sourceXref,  lensUri, tgtUriPattern);
        for (Mapping result:results){
            result.addSourceUri(sourceUri);
        }
        return results;
    }

    @Override
    public synchronized Set<Mapping> mapFull(String sourceUri, String lensUri, DataSource... tgtDataSources) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        Set<Mapping> results = mapFull(sourceXref,  lensUri, tgtDataSources);
        for (Mapping result:results){
            result.addSourceUri(sourceUri);
        }
        return results;
    }

    @Override
    public synchronized Set<Mapping> mapFull(String sourceUri, String lensUri, DataSource tgtDataSource) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        Xref sourceXref = toXref(sourceUri);
        Set<Mapping> results = mapFull(sourceXref,  lensUri, tgtDataSource);
        for (Mapping result:results){
            result.addSourceUri(sourceUri);
        }
        return results;
    }

    private StringBuilder startMappingQuery(){
        StringBuilder query = new StringBuilder("SELECT ");
            query.append(TARGET_ID_COLUMN_NAME);
                query.append(", ");
            query.append(TARGET_DATASOURCE_COLUMN_NAME);
        return query;
    }
    
   private StringBuilder startMappingsBySetQuery(){
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(TARGET_ID_COLUMN_NAME);
        query.append(", ");
        query.append(TARGET_DATASOURCE_COLUMN_NAME);
        query.append(", ");
        query.append(MAPPING_SET_ID_COLUMN_NAME);
        query.append(", ");
        query.append(PREDICATE_COLUMN_NAME);
        query.append(", ");
        query.append(JUSTIFICATION_COLUMN_NAME);
        query.append(", ");
        query.append(MAPPING_NAME_COLUMN_NAME);
        return query;
   }

    private void appendMappingInfo(StringBuilder query){
        query.append(", ");
        query.append(MAPPING_SET_ID_COLUMN_NAME);
        query.append(", ");
        query.append(PREDICATE_COLUMN_NAME);
    }
    
    private void appendSourceInfo(StringBuilder query){
        query.append(", ");
        query.append(SOURCE_ID_COLUMN_NAME);
        query.append(", ");
        query.append(SOURCE_DATASOURCE_COLUMN_NAME);
    }
    
    private void appendMappingFromAndWhere(StringBuilder query, Xref ref, String lensUri, DataSource tgtDataSource) 
            throws BridgeDBException {
        appendMappingFromJoinMapping(query);
        appendSourceXref(query, ref);
        if (tgtDataSource != null){
            query.append(" AND ");
                query.append(TARGET_DATASOURCE_COLUMN_NAME);
                query.append(" = '");
                query.append(getDataSourceKey(tgtDataSource));
                query.append("' ");   
        }
        appendLensClause(query, lensUri, true);
    }

    private void appendMappingFromJoinMapping(StringBuilder query){ 
        appendMappingFrom(query);
        appendMappingJoinMapping(query);
    }
    
    private void appendMappingFrom(StringBuilder query){ 
        query.append(" FROM ");
        query.append(MAPPING_TABLE_NAME);
        query.append(", ");
        query.append(MAPPING_SET_TABLE_NAME);
    }

    /*public static void appendMappingInfoFromAndWhere(StringBuilder query){
        query.append(" FROM ");
        query.append(MAPPING_SET_TABLE_NAME);
        query.append(", ");
        query.append(MAPPING_STATS_TABLE_NAME);
        query.append(" WHERE ");
        query.append(ID_COLUMN_NAME);
        query.append(" = ");
        query.append(MAPPING_SET_ID_COLUMN_NAME);
    }*/
    
    /**
     * Adds the WHERE clause conditions for ensuring that the returned mappings
     * are from active linksets.
     * 
     * @param query Query with WHERE clause started
     * @param lensUri Uri of the lens to use
     * @throws BridgeDbSqlException if the lens does not exist
     */
    private void appendLensClause(StringBuilder query, String lensId, boolean whereAdded) throws BridgeDBException {
        if (lensId == null){
            lensId = Lens.getDefaultLens();
        }
        if (!lensId.equals(Lens.getAllLens())) {
            List<String> justifications = Lens.getJustificationsbyId(lensId);
            if (justifications.isEmpty()){
                throw new BridgeDBException ("No  justifications found for Lens " + lensId);
            }
            if (whereAdded){
                query.append(" AND ");  
            } else {
                query.append(" WHERE ");                      
            }
            query.append(JUSTIFICATION_COLUMN_NAME);
            query.append(" IN (");
            for (int i = 0; i < justifications.size() - 1; i++){
                query.append("'").append(justifications.get(i)).append("', ");
            }
            query.append("'").append(justifications.get(justifications.size()-1)).append("')");
        }
	}

    @Override
    public synchronized boolean uriExists(String uri) throws BridgeDBException {
        uri = scrubUri(uri);
        Xref xref = toXref(uri);
        if (xref == null){
            return false;
        }
        return this.xrefExists(xref);
    }

    @Override
    public synchronized Set<String> uriSearch(String text, int limit) throws BridgeDBException {
        Set<Xref> xrefs = freeSearch(text, limit);
        Set<String> results = new HashSet<String>();
        for (Xref xref:xrefs){
            results.addAll(toUris(xref));
            if (results.size() >= limit){
                break;
            }
        }
        if (results.size() > limit){
            int count = 0;
            for (Iterator<String> i = results.iterator(); i.hasNext();) {
                String element = i.next();
                count++;
                if (count > limit) {
                    i.remove();
                }
            }
        }
        return results;
    }
    
    @Override
    public synchronized Xref toXref(String uri) throws BridgeDBException {
        if (uri == null || uri.isEmpty()){
            return null;
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(DATASOURCE_COLUMN_NAME);
        query.append(", ");
        query.append(PREFIX_COLUMN_NAME);
        query.append(", ");
        query.append(POSTFIX_COLUMN_NAME);
        query.append(" FROM ");
        query.append(URI_TABLE_NAME);
        query.append(" WHERE '");
        query.append(insertEscpaeCharacters(uri));
        query.append("' LIKE CONCAT(");
        query.append(PREFIX_COLUMN_NAME);
        query.append(",'%',");
        query.append(POSTFIX_COLUMN_NAME);
        query.append(")");
        
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        try {
            if (rs.next()){
                String sysCode = rs.getString(DATASOURCE_COLUMN_NAME);
                String prefix = rs.getString(PREFIX_COLUMN_NAME);
                String postfix = rs.getString(POSTFIX_COLUMN_NAME);
                while(rs.next()){
                    String newPrefix = rs.getString(PREFIX_COLUMN_NAME);
                    String newPostfix = rs.getString(POSTFIX_COLUMN_NAME);
                    //If there is more than one result take the most specific.
                    if (newPrefix.length() > prefix.length() || newPostfix.length() > postfix.length()){
                        prefix = newPrefix;
                        sysCode = rs.getString(DATASOURCE_COLUMN_NAME);
                        postfix = newPostfix;
                    }
                }
                DataSource dataSource = findDataSource(sysCode);
                String id = uri.substring(prefix.length(), uri.length()-postfix.length());
                Xref result =  new Xref(id, dataSource);
                if (logger.isDebugEnabled()){
                    logger.debug(uri + " toXref " + result);
                }
                return result;
            }
            return null;
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to get uriSpace. " + query, ex);
        }    
    }

    @Override
    public synchronized UriPattern toUriPattern(String uri) throws BridgeDBException {
        if (uri == null || uri.isEmpty()){
            return null;
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(PREFIX_COLUMN_NAME);
        query.append(", ");
        query.append(POSTFIX_COLUMN_NAME);
        query.append(" FROM ");
        query.append(URI_TABLE_NAME);
        query.append(" WHERE '");
        query.append(insertEscpaeCharacters(uri));
        query.append("' LIKE CONCAT(");
        query.append(PREFIX_COLUMN_NAME);
        query.append(",'%',");
        query.append(POSTFIX_COLUMN_NAME);
        query.append(")");
        
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        try {
            if (rs.next()){
                String prefix = rs.getString(PREFIX_COLUMN_NAME);
                String postfix = rs.getString(POSTFIX_COLUMN_NAME);
                while(rs.next()){
                    String newPrefix = rs.getString(PREFIX_COLUMN_NAME);
                    String newPostfix = rs.getString(POSTFIX_COLUMN_NAME);
                    //If there is more than one result take the most specific.
                    if (newPrefix.length() > prefix.length() || newPostfix.length() > postfix.length()){
                        prefix = newPrefix;
                        postfix = newPostfix;
                    }
                }
                UriPattern result = UriPattern.byPrefixAndPostfix(prefix, postfix);
                if (logger.isDebugEnabled()){
                    logger.debug(uri + " toXref " + result);
                }
                return result;
            }
            return null;
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to get uriSpace. " + query, ex);
        }    
    }

    //@Override too slow
    public synchronized List<Mapping> getSampleMapping() throws BridgeDBException {
        StringBuilder query = new StringBuilder("SELECT ");
        this.appendTopConditions(query, 0, 5);
        query.append(TARGET_ID_COLUMN_NAME);
        query.append(", ");
        query.append(TARGET_DATASOURCE_COLUMN_NAME);
        appendMappingInfo(query);
        appendSourceInfo(query);
        appendMappingFrom(query);
        appendMappingJoinMapping(query);
        this.appendLimitConditions(query, 0, 5);
        Statement statement = this.createStatement();
        ResultSet rs;
        //if (true) throw new BridgeDBException(query.toString());
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        } 
        Set<Mapping> results = resultSetToMappingSet(null, rs);
        for (Mapping result:results){
            addSourceURIs(result);
            addTargetURIs(result);      
        }
        ArrayList list =  new ArrayList<Mapping>(results);
        return list;
    }

    @Override
    public synchronized OverallStatistics getOverallStatistics(String lensId) throws BridgeDBException {
        int numberOfLenses;
        if (Lens.getAllLens().equals(lensId)){
            numberOfLenses = Lens.getNumberOfLenses();
        } else {
            numberOfLenses = 1;
        }
        StringBuilder query = new StringBuilder("SELECT count(*) as numberOfMappingSets, ");
        query.append("count(distinct(");
        query.append(SOURCE_DATASOURCE_COLUMN_NAME);
        query.append(")) as numberOfSourceDataSources,");
        query.append(" count(distinct(");
        query.append(PREDICATE_COLUMN_NAME);
        query.append(")) as numberOfPredicates,");
        query.append(" count(distinct(");
        query.append(TARGET_DATASOURCE_COLUMN_NAME);
        query.append(")) as numberOfTargetDataSources,");
        query.append(" sum(");
        query.append(MAPPING_LINK_COUNT_COLUMN_NAME);
        query.append(") as numberOfMappings ");
        query.append(" FROM ");
        query.append(MAPPING_STATS_TABLE_NAME);
        this.appendLensClause(query, lensId, false);
        Statement statement = this.createStatement();
        try {
            ResultSet rs = statement.executeQuery(query.toString());
			if (rs.next()){
                int numberOfMappingSets = rs.getInt("numberOfMappingSets");
                int numberOfSourceDataSources = rs.getInt("numberOfSourceDataSources");
                int numberOfPredicates= rs.getInt("numberOfPredicates");
                int numberOfTargetDataSources = rs.getInt("numberOfTargetDataSources");
                int numberOfMappings = rs.getInt("numberOfMappings");
                return new OverallStatistics(numberOfMappings, numberOfMappingSets, 
                		numberOfSourceDataSources, numberOfPredicates, 
                		numberOfTargetDataSources, numberOfLenses);
            } else {
                System.err.println(query.toString());
                throw new BridgeDBException("no Results for query. " + query.toString());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query.toString(), ex);
        }
    }

     /*private int getMappingsCount() throws BridgeDBException{
        String linkQuery = "SELECT count(*) as numberOfMappings "
                + "FROM " + MAPPING_TABLE_NAME;
        Statement statement = this.createStatement();
        try {
            ResultSet rs = statement.executeQuery(linkQuery);
            if (rs.next()){
                return rs.getInt("numberOfMappings");
            } else {
                System.err.println(linkQuery);
                throw new BridgeDBException("No Results for query. " + linkQuery);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + linkQuery, ex);
        }      
    }*/

    @Override
    public synchronized MappingSetInfo getMappingSetInfo(int mappingSetId) throws BridgeDBException {
        StringBuilder query = new StringBuilder("SELECT *");
        query.append(" FROM ");
        query.append(MAPPING_STATS_TABLE_NAME);
        query.append(" WHERE ");
        query.append(MAPPING_SET_ID_COLUMN_NAME);
        query.append(" = ");
        query.append(mappingSetId);
        Statement statement = this.createStatement();
        try {
            ResultSet rs = statement.executeQuery(query.toString());
            List<MappingSetInfo> results = resultSetToMappingSetInfos(rs);
            if (results.isEmpty()){
                return null;
            }
            if (results.size() > 1){
                throw new BridgeDBException (results.size() + " mappingSets found with id " + mappingSetId);
            }
            return results.get(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
    }

    @Override
    public synchronized List<MappingSetInfo> getMappingSetInfos(String sourceSysCode, String targetSysCode,String lensUri) throws BridgeDBException {
        StringBuilder query = new StringBuilder("select *");
        query.append(" FROM ");
        query.append(MAPPING_STATS_TABLE_NAME);
        boolean whereSet = appendSystemCodes(query, sourceSysCode, targetSysCode);
        appendLensClause(query, lensUri, whereSet);         
        Statement statement = this.createStatement();
        try {
            ResultSet rs = statement.executeQuery(query.toString());
            return resultSetToMappingSetInfos(rs);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
    }

    @Override
    public synchronized Set<String> getUriPatterns(String dataSource) throws BridgeDBException {
        String query = ("SELECT " + PREFIX_COLUMN_NAME + ", " + POSTFIX_COLUMN_NAME + " FROM " + URI_TABLE_NAME
                + " WHERE " + DATASOURCE_COLUMN_NAME + " = '" + dataSource + "'");
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
        return resultSetToUriPattern(rs);
    }
    
    @Override
    public synchronized int getSqlCompatVersion() throws BridgeDBException {
        String query = ("select " + SCHEMA_VERSION_COLUMN_NAME + " from " + INFO_TABLE_NAME);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
            //should always be there unless something has gone majorly wrong.
            rs.next();
            return rs.getInt("schemaversion");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }       
    }

    // **** UriListener Methods
    
    @Override
    public synchronized void registerUriPattern(DataSource source, String uriPattern) throws BridgeDBException {
        //checkDataSourceInDatabase(source);
        int pos = uriPattern.indexOf("$id");
        if (pos == -1) {
            throw new BridgeDBException ("uriPattern " + uriPattern + " does not contain \"$id\"");
        }
        String prefix = uriPattern.substring(0, pos);
		String postfix = uriPattern.substring(pos + 3);
        this.registerUriPattern(source, prefix, postfix);
    }
    
    private void registerUriPattern (UriPattern uriPattern) throws BridgeDBException{
        DataSource dataSource = uriPattern.getDataSource();
        String prefix = uriPattern.getPrefix();
        String postfix = uriPattern.getPostfix();
        registerUriPattern(dataSource, prefix, postfix);
    }
    
    @Override
    public synchronized void registerUriPattern(DataSource dataSource, String prefix, String postfix) throws BridgeDBException {
        //checkDataSourceInDatabase(dataSource);
        if (postfix == null){
            postfix = "";
        }
        if (prefix.length() > PREFIX_LENGTH){
            throw new BridgeDBException("Prefix Length ( " + prefix.length() + ") is too long for " + prefix);
        }
        if (postfix.length() > POSTFIX_LENGTH){
            throw new BridgeDBException("Postfix Length ( " + prefix.length() + ") is too long for " + prefix);
        }

        prefix = insertEscpaeCharacters(prefix);
        postfix = insertEscpaeCharacters(postfix);
        String dataSourceKey = getDataSourceKey(prefix, postfix);
        if (dataSourceKey != null){
            if (getDataSourceKey(dataSource).equals(dataSourceKey)) return; //Already known so fine.
            throw new BridgeDBException ("UriPattern " + prefix + "$id" + postfix + " already mapped to " + dataSourceKey 
                    + " Which does not match " + getDataSourceKey(dataSource));
        }
        String query = "INSERT INTO " + URI_TABLE_NAME + " (" 
                + DATASOURCE_COLUMN_NAME + ", " 
                + PREFIX_COLUMN_NAME + ", " 
                + POSTFIX_COLUMN_NAME + ") VALUES "
                + " ('" + getDataSourceKey(dataSource) + "', "
                + "  '" + prefix + "',"
                + "  '" + postfix + "')";
        Statement statement = createStatement();
        try {
            int changed = statement.executeUpdate(query);
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error inserting prefix " + prefix + " and postfix " + postfix , ex, query);
        }
    }

    @Override
    public synchronized int registerMappingSet(UriPattern sourceUriPattern, String predicate, String justification, 
            UriPattern targetUriPattern, String mappingSource, boolean symetric, Set<String> viaLabels, 
            Set<Integer> chainedLinkSets) throws BridgeDBException {
        checkUriPattern(sourceUriPattern);
        checkUriPattern(targetUriPattern);
        DataSource source = sourceUriPattern.getDataSource();
        DataSource target = targetUriPattern.getDataSource();      
        int mappingSetId = registerMappingSet(source, predicate, justification, target, mappingSource, symetric, 
                viaLabels, chainedLinkSets);
        subjectUriPatterns.put(mappingSetId, sourceUriPattern);
        targetUriPatterns.put(mappingSetId, targetUriPattern);
        return mappingSetId;
    }

    private void checkUriPattern(UriPattern pattern) throws BridgeDBException{
        String postfix = pattern.getPostfix();
        if (postfix == null){
            postfix = "";
        }
    	String query = "SELECT " + DATASOURCE_COLUMN_NAME  
    			+ " FROM " + URI_TABLE_NAME
                + " WHERE " + PREFIX_COLUMN_NAME + " ='" + pattern.getPrefix()
                + "' AND " + POSTFIX_COLUMN_NAME + " ='" + postfix + "'";
    	Statement statement = this.createStatement();
    	try {
			ResultSet rs = statement.executeQuery(query);
			if (rs.next()) {
				String storedKey = rs.getString(DATASOURCE_COLUMN_NAME);
                String newKey = getDataSourceKey(pattern.getDataSource());
                if (!storedKey.equals(newKey)){
                    logger.error("WARNING " + pattern + " has a different Datasource to what was registered.");
                    logger.error("  pattern has " + pattern.getDataSource() + " with key " + newKey);
                    logger.error("  sql has " + keyToDataSource(storedKey) + " obtained from " + storedKey);
                }
			} else {
               throw new BridgeDBException("Unregistered pattern. " + pattern);
            }
		} catch (SQLException e) {
			throw new BridgeDBException("Unable to check pattern. " + query, e);
		}        
    }
    
    @Override
    public synchronized void insertUriMapping(String sourceUri, String targetUri, int mappingSetId, boolean symetric) throws BridgeDBException {
        UriPattern uriPattern = subjectUriPatterns.get(mappingSetId);
        if (uriPattern == null){
            throw new BridgeDBException("No SourceURIPattern regstered for mappingSetId " + mappingSetId);
        }
        int end =  sourceUri.length()-uriPattern.getPostfix().length();
        if (!sourceUri.startsWith(uriPattern.getPrefix())){
            throw new BridgeDBException("SourceUri: " + sourceUri + " does not mathc the registered pattern "+uriPattern);
        }
        if (!sourceUri.endsWith(uriPattern.getPostfix())){
            throw new BridgeDBException("SourceUri: " + sourceUri + " does not mathc the registered pattern "+uriPattern);
        }
        String sourceId = sourceUri.substring(uriPattern.getPrefix().length(), end);
        uriPattern = targetUriPatterns.get(mappingSetId);
        if (uriPattern == null){
            throw new BridgeDBException("No TargetURIPattern regstered for mappingSetId " + mappingSetId);
        }
        if (!targetUri.startsWith(uriPattern.getPrefix())){
            throw new BridgeDBException("TargetUri: " + targetUri + " does not mathc the registered pattern "+uriPattern);
        }
        if (!targetUri.endsWith(uriPattern.getPostfix())){
            throw new BridgeDBException("TargetUri: " + targetUri + " does not mathc the registered pattern "+uriPattern);
        }
        end = targetUri.length()-uriPattern.getPostfix().length();
        String targetId = targetUri.substring(uriPattern.getPrefix().length(), end);
        this.insertLink(sourceId, targetId, mappingSetId, symetric);
    }

    @Override
    public synchronized void closeInput() throws BridgeDBException {
        super.closeInput();
        subjectUriPatterns.clear();
        targetUriPatterns.clear();
    }
    /**
     * Method to split a Uri into an URISpace and an ID.
     *
     * Based on OPENRDF version with ":" added as and extra splitter.
     *
     * Ideally this would be replaced by a method from Identifiers.org
     *    based on their knoweldge or ULIs
     * @param uri Uri to split
     * @return The URISpace of the Uri
     */
    private final static String splitUriSpace(String uri){
        String prefix = null;
        uri = uri.trim();
        if (uri.contains("#")){
            prefix = uri.substring(0, uri.lastIndexOf("#")+1);
        } else if (uri.contains("=")){
            prefix = uri.substring(0, uri.lastIndexOf("=")+1);
        } else if (uri.contains("/")){
            prefix = uri.substring(0, uri.lastIndexOf("/")+1);
        } else if (uri.contains(":")){
            prefix = uri.substring(0, uri.lastIndexOf(":")+1);
        }
        //ystem.out.println(lookupPrefix);
        if (prefix == null){
            throw new IllegalArgumentException("Uri should have a '#', '/, or a ':' in it.");
        }
        if (prefix.isEmpty()){
            throw new IllegalArgumentException("Uri should not start with a '#', '/, or a ':'.");            
        }
        return prefix;
    }

/*    private Set<String> getViaCodes(int id) throws BridgeDBException {

    /**
     * Method to split a Uri into an URISpace and an ID.
     *
     * Based on OPENRDF version with ":" added as and extra splitter.
     *
     * Ideally this would be replaced by a method from Identifiers.org
     *    based on their knowledge or ULI/URLs
     * @param uri Uri to split
     * @return The URISpace of the Uri
     * /
    public final static String splitId(String uri){
        uri = uri.trim();
        if (uri.contains("#")){
            return uri.substring(uri.lastIndexOf("#")+1, uri.length());
        } else if (uri.contains("=")){
            return uri.substring(uri.lastIndexOf("=")+1, uri.length());
        } else if (uri.contains("/")){
            return uri.substring(uri.lastIndexOf("/")+1, uri.length());
        } else if (uri.contains(":")){
            return uri.substring(uri.lastIndexOf(":")+1, uri.length());
        }
        throw new IllegalArgumentException("Uri should have a '#', '/, or a ':' in it.");
    }
*/
    private Set<DataSetInfo> getViaCodes(int id) throws BridgeDBException {
        String query = ("SELECT " + VIA_DATASOURCE_COLUMN_NAME
                + " FROM " + VIA_TABLE_NAME 
                + " WHERE " + MAPPING_SET_ID_COLUMN_NAME + " = \"" + id + "\"");
    	Statement statement = this.createStatement();
        HashSet<DataSetInfo> results = new HashSet<DataSetInfo>();
		try {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()){
                String sysCode = rs.getString(VIA_DATASOURCE_COLUMN_NAME);
                results.add(findDataSetInfo(sysCode));
            }
            return results;
		} catch (SQLException e) {
			throw new BridgeDBException("Unable to retrieve lenses.", e);
		}
    }
    
    private DataSetInfo findDataSetInfo(String sysCode){
        DataSource ds = this.findDataSource(sysCode);
        return new DataSetInfo(sysCode, ds.getFullName());
    }
    
    private Set<Integer> getChainIds(int id) throws BridgeDBException {
        String query = ("SELECT " + CHAIN_ID_COLUMN_NAME
                + " FROM " + CHAIN_TABLE_NAME 
                + " WHERE " + MAPPING_SET_ID_COLUMN_NAME + " = \"" + id + "\"");
    	Statement statement = this.createStatement();
        HashSet<Integer> results = new HashSet<Integer>();
		try {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()){
                results.add(rs.getInt(CHAIN_ID_COLUMN_NAME));
            }
            return results;
		} catch (SQLException e) {
			throw new BridgeDBException("Unable to retrieve lenses.", e);
		}
    }

    /**
     * Generates a set of Uri from a ResultSet.
     *
     * This implementation just concats the URISpace and Id
     *
     * Ideally this would be replaced by a method from Identifiers.org
     *    based on their knoweldge or ULI/URLs
     * This may require the method to be exstended with the Target NameSpaces.
     *
     * @param rs Result Set holding the information
     * @return Uris generated
     * @throws BridgeDBException
     */
    private Set<String> resultSetToUrisSet(ResultSet rs) throws BridgeDBException {
        HashSet<String> results = new HashSet<String>();
        try {
            while (rs.next()){
                String id = rs.getString("id");
                String uriSpace = rs.getString(PREFIX_COLUMN_NAME);
                String uri = uriSpace + id;
                results.add(uri);
            }
            return results;
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }
    }

   /**
     * Generates a set of UriSpaces a ResultSet.
     *
     * This implementation just extracts the URISpace
     *
     * @param rs Result Set holding the information
     * @return UriSpaces generated
     * @throws BridgeDBException
     */
     private Set<String> resultSetToUriPattern(ResultSet rs) throws BridgeDBException {
        try {
            HashSet<String> uriPatterns = new HashSet<String>();
            while (rs.next()){
                String prefix = rs.getString(PREFIX_COLUMN_NAME);
                String postfix = rs.getString(POSTFIX_COLUMN_NAME);
                uriPatterns.add(prefix + "$id" + postfix);
            }
            return  uriPatterns;
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }
    }

    private Set<Mapping> resultSetToMappingSet(Xref sourceXref, ResultSet rs) throws BridgeDBException {
        HashSet<Mapping> results = new HashSet<Mapping>();
        try {
            while (rs.next()){
                String targetId = rs.getString(TARGET_ID_COLUMN_NAME);
                String targetKey = rs.getString(TARGET_DATASOURCE_COLUMN_NAME);
                DataSource targetDatasource = keyToDataSource(targetKey);
                Xref target = new Xref(targetId, targetDatasource);
                Integer mappingSetId = rs.getInt(MAPPING_SET_ID_COLUMN_NAME);
                String predicate = rs.getString(PREDICATE_COLUMN_NAME);
                Xref source;
                if (sourceXref == null){
                    String sourceId = rs.getString(SOURCE_ID_COLUMN_NAME);
                    String key = rs.getString(SOURCE_DATASOURCE_COLUMN_NAME);
                    DataSource sourceDataSource = keyToDataSource(key);
                    source = new Xref(sourceId, sourceDataSource);
                } else {
                    source = sourceXref;
                }
                Mapping uriMapping = new Mapping (source, predicate, target, mappingSetId);       
                results.add(uriMapping);
            }
            return results;
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }
    }

   /**
     * Generates the meta info from the result of a query
     * @param rs
     * @return
     * @throws BridgeDBException
     */
    public synchronized List<MappingSetInfo> resultSetToMappingSetInfos(ResultSet rs ) throws BridgeDBException{
        ArrayList<MappingSetInfo> results = new ArrayList<MappingSetInfo>();
        try {
            while (rs.next()){
                int id = rs.getInt(MAPPING_SET_ID_COLUMN_NAME);
                Set<DataSetInfo> viaSysCodes = getViaCodes(id);
                Set<Integer> chainIds = getChainIds(id);
                DataSetInfo sourceInfo = findDataSetInfo(rs.getString(SOURCE_DATASOURCE_COLUMN_NAME));
                DataSetInfo targetInfo = findDataSetInfo(rs.getString(TARGET_DATASOURCE_COLUMN_NAME));
                

                results.add(new MappingSetInfo(id, 
                        sourceInfo, 
                        rs.getString(PREDICATE_COLUMN_NAME), 
                        targetInfo, 
                        rs.getString(JUSTIFICATION_COLUMN_NAME), 
                        rs.getString(MAPPING_NAME_COLUMN_NAME), 
                        rs.getString(MAPPING_URI_COLUMN_NAME), 
                        rs.getInt(SYMMETRIC_COLUMN_NAME), 
                        viaSysCodes, 
                        chainIds, 
                        rs.getInt(MAPPING_LINK_COUNT_COLUMN_NAME),
                        rs.getInt(MAPPING_SOURCE_COUNT_COLUMN_NAME), 
                        rs.getInt(MAPPING_TARGET_COUNT_COLUMN_NAME), 
                        rs.getInt(MAPPING_MEDIUM_FREQUENCY_COLUMN_NAME),
                        rs.getInt(MAPPING_75_PERCENT_FREQUENCY_COLUMN_NAME), 
                        rs.getInt(MAPPING_90_PERCENT_FREQUENCY_COLUMN_NAME), 
                        rs.getInt(MAPPING_99_PERCENT_FREQUENCY_COLUMN_NAME), 
                        rs.getInt(MAPPING_MAX_FREQUENCY_COLUMN_NAME)));
            }
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
        }
        return results;
    }

    private void resultSetAddToMappingsBySet(ResultSet rs, String sourceUri, MappingsBySet mappingsBySet) 
            throws BridgeDBException {
        try {
            while (rs.next()){
                String targetId = rs.getString(TARGET_ID_COLUMN_NAME);
                String targetKey = rs.getString(TARGET_DATASOURCE_COLUMN_NAME);
                DataSource targetDatasource = keyToDataSource(targetKey);
                Xref target = new Xref(targetId, targetDatasource);
                Set<String> targetUris = toUris(target);
                Integer mappingSetId = rs.getInt(MAPPING_SET_ID_COLUMN_NAME);
                String predicate = rs.getString(PREDICATE_COLUMN_NAME);
                String justification = rs.getString(JUSTIFICATION_COLUMN_NAME);
                String mappingSource = rs.getString(MAPPING_NAME_COLUMN_NAME);
                mappingsBySet.addMapping(mappingSetId, predicate, justification, mappingSource, sourceUri, targetUris);
            }
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }              
    }

    private void resultSetAddToMappingsBySet(ResultSet rs, String sourceUri, MappingsBySet mappingsBySet, 
            UriPattern tgtUriPattern) throws BridgeDBException {
        try {
            while (rs.next()){
                String targetId = rs.getString(TARGET_ID_COLUMN_NAME);
                String targetUri = tgtUriPattern.getUri(targetId);
                Integer mappingSetId = rs.getInt(MAPPING_SET_ID_COLUMN_NAME);
                String predicate = rs.getString(PREDICATE_COLUMN_NAME);
                String justification = rs.getString(JUSTIFICATION_COLUMN_NAME);
                String mappingSource = rs.getString(MAPPING_NAME_COLUMN_NAME);
                mappingsBySet.addMapping(mappingSetId, predicate, justification, mappingSource, sourceUri, targetUri);
            }
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }              
    }

    /**
     * Finds the SysCode of the DataSource which includes this prefix and postfix
     *
     * Should be replaced by a more complex method from identifiers.org
     *
     * @param prefix to find DataSource for
     * @param postfix to find DataSource for
     * @return sysCode of an existig DataSource or null
     * @throws BridgeDBException
     */
    private String getDataSourceKey(String prefix, String postfix) throws BridgeDBException {
        if (postfix == null){
            postfix = "";
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(DATASOURCE_COLUMN_NAME);
        query.append(" FROM ");
        query.append(URI_TABLE_NAME);
        query.append(" WHERE ");
        query.append(PREFIX_COLUMN_NAME);
        query.append(" = '");
        query.append(prefix);
        query.append("' ");
        query.append(" AND ");
        query.append(POSTFIX_COLUMN_NAME);
        query.append(" = '");
        query.append(postfix);
        query.append("' ");
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        try {
            if (rs.next()){
                return rs.getString(DATASOURCE_COLUMN_NAME);
            }
            return null;
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to get SysCode. " + query, ex);
        }    
    }

    /**
     * Returns the DataSource associated with a URISpace.
     *
     * Throws an exception if the URISpace is unknown.
     *
     * @param uriSpace A Known URISpace
     * @return A DataSource. Never null, instead an Exception is thrown
     * @throws BridgeDBException For example if the uriSpace is not known.
     */
    private DataSource getDataSource(String uriSpace) throws BridgeDBException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(DATASOURCE_COLUMN_NAME);
        query.append(" FROM ");
        query.append(URI_TABLE_NAME);
        query.append(" WHERE ");
        query.append(PREFIX_COLUMN_NAME);
        query.append(" = '");
            query.append(uriSpace);
            query.append("' ");
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        HashSet<String> results = new HashSet<String>();
        try {
            if (rs.next()){
                String sysCode = rs.getString("dataSource");
                return findDataSource(sysCode);
            }
            DataSource.Builder builder = DataSource.register(uriSpace, uriSpace).urlPattern(uriSpace+"$id");
            return builder.asDataSource();
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }
    }

    private boolean appendSystemCodes(StringBuilder query, String sourceSysCode, String targetSysCode) {
        boolean whereSet = false;
        if (sourceSysCode != null && !sourceSysCode.isEmpty()){
            query.append(" WHERE ");
            whereSet = true;
            query.append(SOURCE_DATASOURCE_COLUMN_NAME);
            query.append(" = \"" );
            query.append(sourceSysCode);
            query.append("\" ");
        }
        if (targetSysCode != null && !targetSysCode.isEmpty()){
            if (whereSet){
                query.append(" AND " );            
            } else {
                query.append(" WHERE " );                            
            }
            query.append(TARGET_DATASOURCE_COLUMN_NAME);
            query.append(" = \"" );
            query.append(targetSysCode);
            query.append("\" ");
            return true;
        }
        return whereSet;
    }

    private Set<String> toUris(Xref xref) throws BridgeDBException {
        DataSource dataSource = xref.getDataSource();
        StringBuilder query = new StringBuilder();
        query.append("SELECT " + PREFIX_COLUMN_NAME + ", " + POSTFIX_COLUMN_NAME);
        query.append(" FROM ");
        query.append(URI_TABLE_NAME);
        query.append(" WHERE ");
        query.append(DATASOURCE_COLUMN_NAME);
        query.append(" = '");
        query.append(getDataSourceKey(dataSource));
        query.append("' ");
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        HashSet<String> results = new HashSet<String>();
        try {
            while (rs.next()){
                String prefix = rs.getString(PREFIX_COLUMN_NAME);
                String postfix = rs.getString(POSTFIX_COLUMN_NAME);
                String uri = prefix + xref.getId() + postfix;
                results.add(uri);
            }
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }
       return results;
   }

    private void addSourceURIs(Mapping mapping) throws BridgeDBException {
        Set<String> URIs = toUris(mapping.getSource());
        mapping.addSourceUris(URIs);
    }

    private void addTargetURIs(Mapping mapping) throws BridgeDBException {
        Set<String> URIs = toUris(mapping.getTarget());
        mapping.addTargetUris(URIs);
    }

    private void clearUriPatterns() throws BridgeDBException {
        String update = "DELETE FROM " + URI_TABLE_NAME;
        try {
        	Statement statement = createStatement();
            statement.executeUpdate(update);
        } catch (BridgeDBException ex){
             throw ex;
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error clearing uri patterns " + update, ex);
        }
    }

    private Set<String> getPatternDataSources(String column) throws BridgeDBException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(column);
        query.append(" FROM ");
        query.append(MAPPING_SET_TABLE_NAME);
        query.append(" WHERE ");
        query.append(column);
        query.append(" LIKE \"%$id%\"");
        
        Statement statement = this.createStatement();
        Set<String> results = new HashSet<String>();
        try {
            ResultSet rs = statement.executeQuery(query.toString());
            while (rs.next()){
                results.add(rs.getString(column));
            }
            return results;        
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
    }

    private void replaceSysCode(String oldCode, String newCode) throws BridgeDBException {
        replaceSysCode (SOURCE_DATASOURCE_COLUMN_NAME, oldCode, newCode);
        replaceSysCode (TARGET_DATASOURCE_COLUMN_NAME, oldCode, newCode);
    }

    private void replaceSysCode(String columnName, String oldCode, String newCode) throws BridgeDBException {
        String update = "UPDATE " + MAPPING_SET_TABLE_NAME + " SET " + columnName + " =\"" + newCode + "\" WHERE " 
                + columnName + " = \"" + oldCode + "\""; 
        Statement statement = this.createStatement();
        try {
            statement.executeUpdate(update);
        } catch (SQLException ex) {
            throw new BridgeDBException("Error updating " + update, ex);
        }
    }

   public static void main(String[] args) throws BridgeDBException, RDFHandlerException, IOException  {
        ConfigReader.logToConsole();
        BridgeDBRdfHandler.init();
        SQLUriMapper test = new SQLUriMapper(false);
   }
   
   public final static String scrubUri(String original){
       if (original == null){
           return null;
       }
       String result = original.trim();
       if (original.startsWith("<")){
           original = original.substring(1);
       }
       if (original.endsWith(">")){
           original = original.substring(0, original.length()-1);
       }
       return result;
   }

    public synchronized Set<String> getJustifications() throws BridgeDBException {
        HashSet<String> justifications = new HashSet<String>();
        String lensQuery = "SELECT DISTINCT " + JUSTIFICATION_COLUMN_NAME
            + " FROM " + MAPPING_SET_TABLE_NAME;
        try {
            Statement statement = this.createStatement();    		
            ResultSet rs = statement.executeQuery(lensQuery);
          	while (rs.next()) {
                justifications.add(rs.getString(JUSTIFICATION_COLUMN_NAME));
            }
        } catch (SQLException ex) {
            throw new BridgeDBException("Error retrieving justifications ", ex);
        }
        return justifications;
    }

    private int getMaxCounted() throws BridgeDBException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT MAX(");
        query.append(MAPPING_SET_ID_COLUMN_NAME);
        query.append(") as maxCount FROM ");
        query.append(MAPPING_STATS_TABLE_NAME);
        query.append(" WHERE NOT(");
        query.append(MAPPING_LINK_COUNT_COLUMN_NAME);
        query.append(" is NULL)");
        
        Statement statement = this.createStatement();
        try {
            ResultSet rs = statement.executeQuery(query.toString());
            if (rs.next()){
                return (rs.getInt("maxCount"));
            }
            return 0;
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
    }

    @Override
    public void recover() throws BridgeDBException {
        int max = getMaxCounted();
        deleteUncounted(MAPPING_STATS_TABLE_NAME, MAPPING_SET_ID_COLUMN_NAME, max);
        deleteUncounted(MAPPING_TABLE_NAME, MAPPING_SET_ID_COLUMN_NAME, max);
        deleteUncounted(MAPPING_SET_TABLE_NAME, ID_COLUMN_NAME, max);
        deleteUncounted(CHAIN_TABLE_NAME, MAPPING_SET_ID_COLUMN_NAME, max);
        deleteUncounted(VIA_TABLE_NAME, MAPPING_SET_ID_COLUMN_NAME, max);
        resetAutoIncrement(max);
   }

    private void deleteUncounted(String tableName, String idColumnName, int max) throws BridgeDBException {
        StringBuilder update = new StringBuilder();
        update.append("DELETE FROM ");
        update.append(tableName);
        update.append(" WHERE ");
        update.append(idColumnName);
        update.append(" > ");
        update.append(max);
        
        Statement statement = this.createStatement();
        try {
            statement.executeUpdate(update.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run update. " + update, ex);
        }    
    }

    private void resetAutoIncrement(int max) throws BridgeDBException {
        StringBuilder update = new StringBuilder();
        update.append("ALTER TABLE ");
        update.append(MAPPING_SET_TABLE_NAME);
        update.append(" AUTO_INCREMENT = ");
        update.append(max + 1);
        
        Statement statement = this.createStatement();
        try {
            statement.executeUpdate(update.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run update. " + update, ex);
        }    
    }
   
}
 
