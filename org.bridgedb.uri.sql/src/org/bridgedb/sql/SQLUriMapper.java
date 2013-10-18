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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.pairs.CodeMapper;
import org.bridgedb.pairs.IdSysCodePair;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.identifiers.org.IdentifersOrgReader;
import org.bridgedb.rdf.pairs.RdfBasedCodeMapper;
import org.bridgedb.statistics.DataSetInfo;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.GraphResolver;
import org.bridgedb.uri.Lens;
import org.bridgedb.uri.Mapping;
import org.bridgedb.uri.MappingsBySet;
import org.bridgedb.uri.MappingsBySysCodeId;
import org.bridgedb.uri.RegexUriPattern;
import org.bridgedb.uri.UriListener;
import org.bridgedb.uri.UriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.openrdf.model.Resource;

/**
 * Implements the UriMapper and UriListener interfaces using SQL.
 *
 * Takes into accounts the specific factors for the SQL version being used.
 *
 * @author Christian
 */
public class SQLUriMapper extends SQLIdMapper implements UriMapper, UriListener {

    private static final int CREATED_BY_LENGTH = 150;
    private static final int JUSTIFICATION_LENGTH = 150;
    private static final int MIMETYPE_LENGTH = 50;
    private static final int POSTFIX_LENGTH = 100;
    private static final int PREDICATE_LENGTH = 100;
    private static final int PREFIX_LENGTH = 400;
    private static final int REGEX_LENGTH = 400;

    public static final String CHAIN_TABLE_NAME = "chain";
    private static final String VIA_TABLE_NAME = "via";
    private static final String MIMETYPE_TABLE_NAME = "mimeType";
    private static final String URI_TABLE_NAME = "uri";

    public static final String CHAIN_ID_COLUMN_NAME = "chainId";
    private static final String CREATED_BY_COLUMN_NAME = "createdBy";
    private static final String CREATED_ON_COLUMN_NAME = "createdOn";
    private static final String DATASOURCE_COLUMN_NAME = "dataSource";
    public static final String JUSTIFICATION_COLUMN_NAME = "justification";
    private static final String PREDICATE_COLUMN_NAME = "predicate";
    private static final String PREFIX_COLUMN_NAME = "prefix";
 //   private static final String LENS_ID_COLUMN_NAME = "lensId";
//    private static final String LENS_URI_COLUMN_NAME = "lensUri";
    private static final String POSTFIX_COLUMN_NAME = "postfix";
    static final String MAPPING_75_PERCENT_FREQUENCY_COLUMN_NAME = "mapping75Frequency";
    static final String MAPPING_90_PERCENT_FREQUENCY_COLUMN_NAME = "mapping90Frequency";
    static final String MAPPING_99_PERCENT_FREQUENCY_COLUMN_NAME = "mapping99Frequency";
    static final String MAPPING_LINK_COUNT_COLUMN_NAME = "mappingLinkCount";
    static final String MAPPING_MAX_FREQUENCY_COLUMN_NAME = "mappingMaxFrequency";
    static final String MAPPING_MEDIUM_FREQUENCY_COLUMN_NAME = "mappingMediumFrequency";
    static final String MAPPING_RESOURCE_COLUMN_NAME = "resource";
    static final String MAPPING_SOURCE_COLUMN_NAME = "source";
    static final String MAPPING_SOURCE_COUNT_COLUMN_NAME = "mappingSourceCount";
    static final String MAPPING_TARGET_COUNT_COLUMN_NAME = "mappingTargetCount";
    private static final String MIMETYPE_COLUMN_NAME = "mimetype";
    private static final String NAME_COLUMN_NAME = "name";
    private static final String REGEX_COLUMN_NAME = "regex";
        
    static final String VIA_DATASOURCE_COLUMN_NAME = "viaDataSource";
    
    private static SQLUriMapper mapper = null;
    private HashMap<Integer,RegexUriPattern> subjectUriPatterns;
    private HashMap<Integer,RegexUriPattern> targetUriPatterns;
    private boolean processingRawLinkset = true;
            
    static final Logger logger = Logger.getLogger(SQLListener.class);

    public static SQLUriMapper getExisting() throws BridgeDBException{
        if (mapper == null){
            CodeMapper codeMapper = new RdfBasedCodeMapper();
            mapper =  new SQLUriMapper(false, codeMapper);
        }
        return mapper;
    }
    
    public static SQLUriMapper createNew() throws BridgeDBException{
        CodeMapper codeMapper = new RdfBasedCodeMapper();
        mapper =  new SQLUriMapper(true, codeMapper);
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
     private SQLUriMapper(boolean dropTables, CodeMapper codeMapper) throws BridgeDBException{
        super(dropTables, codeMapper);
        BioDataSource.init();
        BridgeDBRdfHandler.init();
        UriPattern.registerUriPatterns();
        try{
            IdentifersOrgReader.init();
        } catch (BridgeDBException ex){
            if (ConfigReader.inTestMode()){
                System.err.println("Ignoring error " + ex);
            } else {
                throw ex;
            }
        }
        clearUriPatterns();
        Collection<RegexUriPattern> patterns = RegexUriPattern.getUriPatterns();
        for (RegexUriPattern pattern:patterns){
            this.registerUriPattern(pattern);
        }
        subjectUriPatterns = new HashMap<Integer,RegexUriPattern>();
        targetUriPatterns = new HashMap<Integer,RegexUriPattern>();
        Lens.init(this);
    }   
    
    @Override
	protected void dropSQLTables() throws BridgeDBException
	{
        super.dropSQLTables();
 		dropTable(URI_TABLE_NAME);
  		dropTable(MIMETYPE_TABLE_NAME);
        dropTable(VIA_TABLE_NAME);
        dropTable(CHAIN_TABLE_NAME);
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
                    + "     " + REGEX_COLUMN_NAME + " VARCHAR(" + REGEX_LENGTH + "), "
                    + "     " + POSTFIX_COLUMN_NAME + " VARCHAR(" + POSTFIX_LENGTH + ") NOT NULL "
                    + "  ) ");
            sh.execute("CREATE TABLE " + MIMETYPE_TABLE_NAME
                    + "  (  " + PREFIX_COLUMN_NAME + " VARCHAR(" + PREFIX_LENGTH + ") NOT NULL, "
                    + "     " + POSTFIX_COLUMN_NAME + " VARCHAR(" + POSTFIX_LENGTH + ") NOT NULL, "
                    + "     mimeType VARCHAR(" + MIMETYPE_LENGTH + ") NOT NULL "
                    + "  ) ");
         	sh.execute("CREATE TABLE " + VIA_TABLE_NAME 
                    + " (" + MAPPING_SET_ID_COLUMN_NAME + " INT NOT NULL, "
                    + "     " + VIA_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ")  NOT NULL "
					+ " ) "); 
         	sh.execute("CREATE TABLE " + CHAIN_TABLE_NAME 
                    + " (" + MAPPING_SET_ID_COLUMN_NAME + " INT NOT NULL, "
                    + "     " + CHAIN_ID_COLUMN_NAME + " INT NOT NULL"
					+ " ) "); 
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
    
	protected void createMappingSetTable() throws BridgeDBException
	{
        //"IF NOT EXISTS " is not supported
        String query = "";
		try 
		{
			Statement sh = createStatement();
        	query = "CREATE TABLE " + MAPPING_SET_TABLE_NAME 
                    + " (" + ID_COLUMN_NAME                   + " INT " + autoIncrement + " PRIMARY KEY, " 
                        + SOURCE_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ") NOT NULL, "
                        + PREDICATE_COLUMN_NAME         + " VARCHAR(" + PREDICATE_LENGTH + "), "
                        + JUSTIFICATION_COLUMN_NAME     + " VARCHAR(" + JUSTIFICATION_LENGTH + "), "
                        + TARGET_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + "), "
                        + MAPPING_RESOURCE_COLUMN_NAME  + " VARCHAR(" + MAPPING_URI_LENGTH + "), "
                        + MAPPING_SOURCE_COLUMN_NAME  + " VARCHAR(" + MAPPING_URI_LENGTH + "), "
                        + SYMMETRIC_COLUMN_NAME + " INT, "
                        + MAPPING_LINK_COUNT_COLUMN_NAME     + " INT, "
                        + MAPPING_SOURCE_COUNT_COLUMN_NAME     + " INT, "
                        + MAPPING_TARGET_COUNT_COLUMN_NAME     + " INT, "
                        + MAPPING_MEDIUM_FREQUENCY_COLUMN_NAME     + " INT, "
                        + MAPPING_75_PERCENT_FREQUENCY_COLUMN_NAME     + " INT, "
                        + MAPPING_90_PERCENT_FREQUENCY_COLUMN_NAME     + " INT, "
                        + MAPPING_99_PERCENT_FREQUENCY_COLUMN_NAME     + " INT, "
                        + MAPPING_MAX_FREQUENCY_COLUMN_NAME     + " INT"
					+ " ) "; 
            sh.execute(query);
            sh.close();
		} catch (SQLException e){
 			throw new BridgeDBException ("Error creating the MappingSet table using " + query, e);
		}
	}
    
    private Set<IdSysCodePair> mapID(IdSysCodePair sourcePair, String lensUri, String tgtSysCode) throws BridgeDBException {
        if (sourcePair == null || tgtSysCode == null){
            return new HashSet<IdSysCodePair>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingFromAndWhere(query, sourcePair, lensUri, tgtSysCode);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<IdSysCodePair> results = resultSetToIdSysCodePairSet(rs);
         //Add mapping to self
        if (sourcePair.getSysCode().equals(tgtSysCode)){
             results.add(sourcePair);
        }
        return results;
    }

    private Set<IdSysCodePair> mapID(IdSysCodePair sourceRef, String lensUri) throws BridgeDBException {
        if (sourceRef == null) {
            logger.warn("mapId called with a badXref " + sourceRef);
            return new HashSet<IdSysCodePair>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingFromAndWhere(query, sourceRef, lensUri, null);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<IdSysCodePair> results = resultSetToIdSysCodePairSet(rs);
         //Add mapping to self
        results.add(sourceRef);
        return results;
    }
    
    private Set<IdSysCodePair> mapID(IdSysCodePair sourceRef, String lensUri, DataSource... tgtDataSource) throws BridgeDBException {
        if (tgtDataSource == null || tgtDataSource.length == 0){
            return mapID(sourceRef, lensUri);
        }
        if (tgtDataSource.length == 1){
            return mapID(sourceRef, lensUri, toCode(tgtDataSource[0]));
        }
        HashSet<IdSysCodePair> results = new HashSet<IdSysCodePair>();
        for (DataSource dataSource: tgtDataSource){
            if (dataSource != null){
                results.addAll(mapID(sourceRef, lensUri, dataSource.getSystemCode()));
            }
        }
        return results;
    }
    
    @Override
    public Set<Xref> mapID(Xref sourceXref, String lensUri, DataSource... tgtDataSources) throws BridgeDBException {
        IdSysCodePair ref = toIdSysCodePair(sourceXref);
        Set<IdSysCodePair> pairs = mapID(ref, lensUri, tgtDataSources);
        Set<Xref> results = toXrefs(pairs);
        return results;
    }
    
    private Set<String> mapUri (IdSysCodePair sourceRef, String lensUri) 
            throws BridgeDBException {
        Set<IdSysCodePair> targetRefs = mapID(sourceRef, lensUri);
        HashSet<String> results = new HashSet<String>();
        for (IdSysCodePair target:targetRefs){
            results.addAll (toUris(target));
        }
        return results;
    }

    private Set<String> mapUri (IdSysCodePair sourceRef, String lensUri, RegexUriPattern tgtUriPattern) 
            throws BridgeDBException {
        if (tgtUriPattern == null){
            logger.warn("mapUri called with a null tgtDatasource");
            return new HashSet<String>();
        }
        String tgtSysCode = tgtUriPattern.getSysCode();
        Set<IdSysCodePair> targetRefs = mapID(sourceRef, lensUri, tgtSysCode);
        HashSet<String> results = new HashSet<String>();
        for (IdSysCodePair target:targetRefs){
            results.add (tgtUriPattern.getUri(target.getId()));
        }
        return results;
    }
    
    private Set<String> mapUri (IdSysCodePair sourceRef, String lensUri, String graph, RegexUriPattern[] tgtUriPatterns) 
            throws BridgeDBException {
        Set<RegexUriPattern> targetUriPatterns = mergeGraphAndTargets(graph, tgtUriPatterns);
        if (targetUriPatterns == null || targetUriPatterns.isEmpty()){
            return mapUri (sourceRef, lensUri);
        }
        Set<String> results = new HashSet<String>();
        for (RegexUriPattern tgtUriPattern:targetUriPatterns){
            results.addAll(mapUri(sourceRef, lensUri, tgtUriPattern));
        }
        return results;
    }
 
    @Override
    public Set<String> mapUri (Xref sourceXref, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
            throws BridgeDBException {
        IdSysCodePair sourceRef = toIdSysCodePair(sourceXref);
        return mapUri(sourceRef, lensUri, graph, tgtUriPatterns);
    }
        
    @Override
    public Set<String> mapUri (String sourceUri, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        IdSysCodePair sourceRef = toIdSysCodePair(sourceUri);
        return mapUri(sourceRef, lensUri, graph, tgtUriPatterns);
    }
 
    @Override
    public MappingsBySysCodeId mapUriBySysCodeId (Collection<String> sourceUris, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
            throws BridgeDBException {
        if (sourceUris.size() == 1){
            return mapUriBySysCodeId(sourceUris.iterator().next(), lensUri, graph, tgtUriPatterns);
        } 
        if (sourceUris.isEmpty()){
            return new MappingsBySysCodeId();
        }
        Iterator<String> iterator = sourceUris.iterator();
        MappingsBySysCodeId result = mapUriBySysCodeId(iterator.next(), lensUri, graph, tgtUriPatterns);
        while (iterator.hasNext()){
            result.merge(mapUriBySysCodeId(iterator.next(), lensUri, graph, tgtUriPatterns));
        }
        return result;
    }
    
    @Override
    public MappingsBySysCodeId mapUriBySysCodeId (String sourceUri, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        IdSysCodePair sourceRef = toIdSysCodePair(sourceUri);
        return mapUriBySysCodeId(sourceRef, lensUri, graph, tgtUriPatterns);
    }

    private MappingsBySysCodeId mapUriBySysCodeId (IdSysCodePair sourceRef, String lensUri, String graph, RegexUriPattern[] tgtUriPatterns) 
            throws BridgeDBException {
        Set<RegexUriPattern> targetUriPatterns = mergeGraphAndTargets(graph, tgtUriPatterns);
        if (targetUriPatterns == null || targetUriPatterns.isEmpty()){
            return mapUriBySysCodeId (sourceRef, lensUri);
        }
        MappingsBySysCodeId results = new MappingsBySysCodeId();
        for (RegexUriPattern tgtUriPattern:targetUriPatterns){
            mapUriBySysCodeId(results, sourceRef, lensUri, tgtUriPattern);
        }
        return results;
    }

    private MappingsBySysCodeId mapUriBySysCodeId(IdSysCodePair sourceRef, String lensUri) 
            throws BridgeDBException {
        MappingsBySysCodeId results = new MappingsBySysCodeId();
        Set<IdSysCodePair> targetRefs = mapID(sourceRef, lensUri);
        for (IdSysCodePair target:targetRefs){
            results.addMappings(target, toUris(target)); 
        }
        return results;
    }
    
    private void mapUriBySysCodeId (MappingsBySysCodeId results, IdSysCodePair sourceRef, String lensUri, RegexUriPattern tgtUriPattern) 
            throws BridgeDBException {
        if (tgtUriPattern == null){
            logger.warn("mapUri called with a null tgtDatasource");
            return;
        }
        String tgtSysCode = tgtUriPattern.getSysCode();
        Set<IdSysCodePair> targetRefs = mapID(sourceRef, lensUri, tgtSysCode);
        for (IdSysCodePair target:targetRefs){
            results.addMapping(target, tgtUriPattern.getUri(target.getId()));
        }
    }

    @Override
    public MappingsBySet mapBySet(String sourceUri, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) throws BridgeDBException {
        Set<RegexUriPattern> targetUriPatterns = mergeGraphAndTargets(graph, tgtUriPatterns);
        MappingsBySet mappingsBySet = new MappingsBySet(lensUri);
        if (targetUriPatterns == null || targetUriPatterns.isEmpty()){
           return mapBySet (sourceUri, mappingsBySet, lensUri);
        }
        for (RegexUriPattern tgtUriPattern:targetUriPatterns){
            mapBySet(sourceUri, mappingsBySet, lensUri, tgtUriPattern);
        }
        return mappingsBySet;
    }

    private void mapBySet(String sourceUri, MappingsBySet mappingsBySet, String lensUri, RegexUriPattern tgtUriPattern) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        IdSysCodePair sourceRef = toIdSysCodePair(sourceUri);
        String tgtSysCode = tgtUriPattern.getSysCode();
        ResultSet rs = mapBySetOnly(sourceRef, sourceUri, lensUri, tgtSysCode);       
        resultSetAddToMappingsBySet(rs, sourceUri, mappingsBySet, tgtUriPattern);           
        if (sourceRef.getSysCode().equals(tgtSysCode)){
            mappingsBySet.addMapping(sourceUri, tgtUriPattern.getUri(sourceRef.getId())); 
        }
    }

    @Override
    public MappingsBySet mapBySet(Set<String> sourceUris, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
           throws BridgeDBException{
        Set<RegexUriPattern> targetUriPatterns = mergeGraphAndTargets(graph, tgtUriPatterns);
        MappingsBySet mappingsBySet = new MappingsBySet(lensUri);
        for (String sourceUri:sourceUris) {
            if (targetUriPatterns == null || targetUriPatterns.isEmpty()){
                mapBySet(sourceUri, mappingsBySet, lensUri);
            } else {
                for (RegexUriPattern tgtUriPattern:targetUriPatterns) {
                    mapBySet(sourceUri, mappingsBySet, lensUri, tgtUriPattern);
                }
            }
        }
        return mappingsBySet;           
    }
       
    public MappingsBySet mapBySet(String sourceUri, MappingsBySet mappingsBySet, String lensUri) 
            throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        IdSysCodePair sourceRef = toIdSysCodePair(sourceUri);
        ResultSet rs = mapBySetOnly(sourceRef, sourceUri, lensUri, null);
        resultSetAddToMappingsBySet(rs, sourceUri, mappingsBySet);
        mappingsBySet.addMapping(sourceUri, toUris(sourceRef));
        return mappingsBySet;
    }

    private ResultSet mapBySetOnly(IdSysCodePair sourceRef, String sourceUri, String lensUri, String tgtSysCode) 
            throws BridgeDBException {
        StringBuilder query =  startMappingsBySetQuery();
        appendMappingFromAndWhere(query, sourceRef, lensUri, tgtSysCode);
        Statement statement = this.createStatement();
        try {
            return statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }         
    }

    private Set<Mapping> mapFull (IdSysCodePair sourceRef, String lensUri) throws BridgeDBException{
        if (sourceRef == null){
            return new HashSet<Mapping>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingInfo(query);
        appendMappingFromAndWhere(query, sourceRef, lensUri, null);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<Mapping> results = resultSetToMappingSet(sourceRef, rs);
        //Add mapping to self
        results.add(new Mapping(codeMapper1.toXref(sourceRef)));
        //Add targetUris
        for (Mapping mapping: results){
            mapping.addTargetUris(toUris(mapping.getTarget()));
        }
        return results;
    }

	private Set<Mapping> mapFull (IdSysCodePair sourceRef, String lensUri, String tgtSysCode) throws BridgeDBException{
        if (sourceRef == null) {
            return new HashSet<Mapping>();
        }
        if (tgtSysCode == null){
           return new HashSet<Mapping>();
        }
        StringBuilder query = startMappingQuery();
        appendMappingInfo(query);
        appendMappingFromAndWhere(query, sourceRef, lensUri, tgtSysCode);
        Statement statement = this.createStatement();
        ResultSet rs;
        try {
            rs = statement.executeQuery(query.toString());
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        Set<Mapping> results = resultSetToMappingSet(sourceRef, rs);
        //Add map to self if correct
        if (sourceRef.getSysCode().equals(tgtSysCode)){
            results.add(new Mapping(codeMapper1.toXref(sourceRef)));
        }
        return results;
    }

	private Set<Mapping> mapFull (IdSysCodePair sourceRef, String lensUri, DataSource tgtDataSource) throws BridgeDBException{
        String tgtSysCode = toCode(tgtDataSource);
        Set<Mapping> results = mapFull(sourceRef, lensUri, tgtSysCode);
        //Add targetUris
        for (Mapping mapping: results){
            mapping.addTargetUris(toUris(mapping.getTarget()));
        }
        return results;
    }
    
    private Set<Mapping> mapFull (IdSysCodePair ref, String lensUri, DataSource... tgtDataSources) 
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

	private Set<Mapping> mapFull (IdSysCodePair sourceRef, String lensUri, RegexUriPattern tgtUriPattern) throws BridgeDBException {
        if (tgtUriPattern == null){
           return new HashSet<Mapping>();
        }
        String tgtSysCode = tgtUriPattern.getSysCode();
        Set<Mapping> results = mapFull(sourceRef, lensUri, tgtSysCode);
        for (Mapping result:results){
            result.addTargetUri(tgtUriPattern.getUri(result.getTarget().getId()));
        }
        return results;
    }

    private Set<Mapping> mapFull (IdSysCodePair sourceRef, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
            throws BridgeDBException{
        Set<RegexUriPattern> targetUriPatterns = mergeGraphAndTargets(graph, tgtUriPatterns);
        if (targetUriPatterns == null || targetUriPatterns.isEmpty()){
            return mapFull (sourceRef, lensUri);
        } else {
            Set<Mapping> results = new HashSet<Mapping>();
            for (RegexUriPattern tgtUriPattern: targetUriPatterns){
                results.addAll(mapFull(sourceRef, lensUri, tgtUriPattern));
            }
            return results;
        }
    }

    @Override
    public Set<Mapping> mapFull (Xref sourceXref, String lensUri, DataSource... tgtDataSources) 
            throws BridgeDBException{
        IdSysCodePair sourceRef = toIdSysCodePair(sourceXref);
        return mapFull(sourceRef, lensUri, tgtDataSources);
    }

    @Override
    public Set<Mapping> mapFull (Xref sourceXref, String lensUri, String graph, RegexUriPattern... tgtUriPatterns) 
            throws BridgeDBException{
        IdSysCodePair sourceRef = toIdSysCodePair(sourceXref);
        return mapFull(sourceRef, lensUri, graph, tgtUriPatterns);
    }

    @Override
    public Set<Mapping> mapFull(String sourceUri, String lensUri, String graph, 
            RegexUriPattern... tgtUriPatterns) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        IdSysCodePair sourceRef = toIdSysCodePair(sourceUri);
        Set<Mapping> results = mapFull(sourceRef, lensUri, graph, tgtUriPatterns);
        for (Mapping result:results){
            result.addSourceUri(sourceUri);
        }
        return results;
    }

    @Override
    public Set<Mapping> mapFull(String sourceUri, String lensUri, DataSource... tgtDataSources) throws BridgeDBException {
        sourceUri = scrubUri(sourceUri);
        IdSysCodePair sourceRef = toIdSysCodePair(sourceUri);
        Set<Mapping> results = mapFull(sourceRef,  lensUri, tgtDataSources);
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
        query.append(MAPPING_SOURCE_COLUMN_NAME);
        query.append(", ");
        query.append(MAPPING_RESOURCE_COLUMN_NAME);
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
    
    private void appendMappingFromAndWhere(StringBuilder query, IdSysCodePair ref, String lensUri, String tgtSysCode) 
            throws BridgeDBException {
        appendMappingFromJoinMapping(query);
        appendSourceIdSysCodePair(query, ref);
        if (tgtSysCode != null){
            query.append(" AND ");
                query.append(TARGET_DATASOURCE_COLUMN_NAME);
                query.append(" = '");
                query.append(tgtSysCode);
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
    public boolean uriExists(String uri) throws BridgeDBException {
        uri = scrubUri(uri);
        Xref xref;
        try {
            xref = toXref(uri);
            if (xref == null){
                return false;
            }
       } catch (BridgeDBException ex){
            return false;
       }
       return this.xrefExists(xref);
    }

    @Override
    public Set<String> uriSearch(String text, int limit) throws BridgeDBException {
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
    public Xref toXref(String uri) throws BridgeDBException {
        IdSysCodePair pair = toIdSysCodePair(uri);
        if (pair == null){
            return null;
        }
        return codeMapper1.toXref(pair);
    }

    @Override
    public IdSysCodePair toIdSysCodePair(String uri) throws BridgeDBException {
        if (uri == null || uri.isEmpty()){
            return null;
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ");
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
            String prefix = null;
            String postfix = null;
            String regex = null;
            String id = null;
            while(rs.next()){
                String sysCode = rs.getString(DATASOURCE_COLUMN_NAME);
                prefix = rs.getString(PREFIX_COLUMN_NAME);
                postfix = rs.getString(POSTFIX_COLUMN_NAME);
                regex = rs.getString (REGEX_COLUMN_NAME);
                id = uri.substring(prefix.length(), uri.length()-postfix.length());
                if (regex == null){
                    return new IdSysCodePair(id, sysCode);                    
                }
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(id);
                if (matcher.matches()){
                    return new IdSysCodePair(id, sysCode);
                } 
            }
            if (prefix == null){
                throw new BridgeDBException("Unknown uri " + uri);
            } else if (postfix == null || postfix.isEmpty()){
                throw new BridgeDBException("Unknown uri " + uri + ". " + id + " does not match the regex pattern " + regex 
                        + " with prefix: " + prefix);                
            } else {
                throw new BridgeDBException("Unknown uri " + uri + ". " + id + " does not match the regex pattern " + regex 
                        + " with prefix: " + prefix + " and postfix " + postfix);                
            }
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to get IdSysCodePair. " + query, ex);
        }    
    }

    @Override
    public RegexUriPattern toUriPattern(String uri) throws BridgeDBException {
        if (uri == null || uri.isEmpty()){
            return null;
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ");
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
            while(rs.next()){
                String prefix = rs.getString(PREFIX_COLUMN_NAME);
                String postfix = rs.getString(POSTFIX_COLUMN_NAME);
                String regex = rs.getString(REGEX_COLUMN_NAME);
                String sysCode = rs.getString(DATASOURCE_COLUMN_NAME);
                if (regex == null){
                    return RegexUriPattern.factory(prefix, postfix, sysCode);
                }
                Pattern regexPattern = Pattern.compile(regex);
                String id = uri.substring(prefix.length(), uri.lastIndexOf(postfix));
                Matcher matcher = regexPattern.matcher(id);
                if (matcher.matches()){
                    return RegexUriPattern.factory(prefix, postfix, sysCode, regexPattern);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to get uriSpace. " + query, ex);
        }    
    }

    //@Override too slow
    public List<Mapping> getSampleMapping() throws BridgeDBException {
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
    public OverallStatistics getOverallStatistics(String lensId) throws BridgeDBException {
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
        query.append(MAPPING_SET_TABLE_NAME);
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
    public MappingSetInfo getMappingSetInfo(int mappingSetId) throws BridgeDBException {
        StringBuilder query = new StringBuilder("SELECT *");
        query.append(" FROM ");
        query.append(MAPPING_SET_TABLE_NAME);
        query.append(" WHERE ");
        query.append(ID_COLUMN_NAME);
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
    public List<MappingSetInfo> getMappingSetInfos(String sourceSysCode, String targetSysCode,String lensUri) throws BridgeDBException {
        StringBuilder query = new StringBuilder("select *");
        query.append(" FROM ");
        query.append(MAPPING_SET_TABLE_NAME);
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
    public Set<String> getUriPatterns(String dataSource) throws BridgeDBException {
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
    public int getSqlCompatVersion() throws BridgeDBException {
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
        
    private void registerUriPattern (RegexUriPattern uriPattern) throws BridgeDBException{
        String code = uriPattern.getSysCode();
        String prefix = uriPattern.getPrefix();
        if (prefix.length() > PREFIX_LENGTH){
            throw new BridgeDBException("Prefix Length ( " + prefix.length() + ") is too long for " + prefix);
        }
        DataSource dataSource = DataSource.getExistingBySystemCode(code);
        Pattern regex = uriPattern.getRegex();
        String postfix = uriPattern.getPostfix();
        if (postfix.length() > POSTFIX_LENGTH){
            throw new BridgeDBException("Postfix Length ( " + prefix.length() + ") is too long for " + prefix);
        }

        prefix = insertEscpaeCharacters(prefix);
        postfix = insertEscpaeCharacters(postfix);
        checkExistingUriPatterns(uriPattern);
        registerUriPattern(prefix, postfix, code, regex);
    }

    private void registerUriPattern (String prefix, String postfix, String code, Pattern regex) throws BridgeDBException{
        StringBuilder query = new StringBuilder("INSERT INTO ").append(URI_TABLE_NAME).append(" (") 
                .append(PREFIX_COLUMN_NAME).append(", ")
                .append(POSTFIX_COLUMN_NAME).append(", ")
                .append(DATASOURCE_COLUMN_NAME).append(", ") 
                .append(REGEX_COLUMN_NAME).append(") VALUES ")
                .append(" (?, ?, ?, ?)");
        PreparedStatement statement = this.createPreparedStatement(query.toString());
        String regexSt = null;
        if (regex != null){
            regexSt = regex.toString();
            if (regexSt.length() > REGEX_LENGTH){
                throw new BridgeDBException("pattern length ( " + regexSt.length() + ") is too long for " + regex);
            }
        }
       try {
            statement.setString(1, prefix);
            statement.setString(2, postfix);
            statement.setString(3, code);
            statement.setString(4, regexSt);
            int changed = statement.executeUpdate();
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error inserting prefix " + prefix + " and postfix " + postfix , ex, statement.toString());
        }
    }

    @Override
    public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String justification, 
            RegexUriPattern targetUriPattern, Resource mappingResource, Resource mappingSource, boolean symetric, Set<String> viaLabels, 
            Set<Integer> chainedLinkSets) throws BridgeDBException {
        checkUriPattern(sourceUriPattern);
        checkUriPattern(targetUriPattern);
        DataSource source = DataSource.getExistingBySystemCode(sourceUriPattern.getSysCode());
        DataSource target = DataSource.getExistingBySystemCode(targetUriPattern.getSysCode());        
        int mappingSetId = registerMappingSet(source, target, predicate, justification, mappingResource, mappingSource, 0);
        registerVia(mappingSetId, viaLabels);
        registerChain(mappingSetId, chainedLinkSets);
        if (symetric){
            int symetricId = registerMappingSet(target, source, predicate, justification, mappingResource, mappingSource, mappingSetId);
            registerVia(symetricId, viaLabels);
            registerChain(symetricId, chainedLinkSets);
        }
        subjectUriPatterns.put(mappingSetId, sourceUriPattern);
        targetUriPatterns.put(mappingSetId, targetUriPattern);
        return mappingSetId;
    }

    /**
     * One way registration of Mapping Set.
     * @param justification 
     * 
     */
    private int registerMappingSet(DataSource source, DataSource target, String predicate, 
            String justification, Resource mappingResource, Resource mappingSource, int symmetric) throws BridgeDBException {
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
        query.append(", ");
        query.append(MAPPING_RESOURCE_COLUMN_NAME); 
        query.append(", ");
        query.append(MAPPING_SOURCE_COLUMN_NAME); 
        query.append(", ");
        query.append(SYMMETRIC_COLUMN_NAME);
        query.append(") VALUES ('");
        query.append(getDataSourceKey(source));
        query.append("', '");
        query.append(predicate);
        query.append("', '");
        query.append(justification);
        query.append("', '");
        query.append(getDataSourceKey(target));
        query.append("', '");
        query.append(mappingResource);
        query.append("', '");
        query.append(mappingSource);
        query.append("', ");
        query.append(symmetric);
        query.append(")");
        int autoinc = registerMappingSet(query.toString());
        logger.info("Registered new Mapping " + autoinc + " from " + getDataSourceKey(source) + " to " + getDataSourceKey(target));
        return autoinc;
    }

    private void checkUriPattern(RegexUriPattern pattern) throws BridgeDBException{
        String postfix = pattern.getPostfix();
        if (postfix == null){
            postfix = "";
        }
        String query;
        if (pattern.getRegex() == null){
            query = "SELECT " + DATASOURCE_COLUMN_NAME  
                	+ " FROM " + URI_TABLE_NAME
                    + " WHERE " + PREFIX_COLUMN_NAME 
                    + " = ? AND " + POSTFIX_COLUMN_NAME + " = ? "
                    + " AND " + REGEX_COLUMN_NAME + " is NULL"; 
        } else {
            query = "SELECT " + DATASOURCE_COLUMN_NAME  
                	+ " FROM " + URI_TABLE_NAME
                    + " WHERE " + PREFIX_COLUMN_NAME 
                    + " = ? AND " + POSTFIX_COLUMN_NAME 
                    + " = ? AND " + REGEX_COLUMN_NAME + " = ?";            
        }
    	PreparedStatement statement = this.createPreparedStatement(query);
    	try {
            statement.setString(1, pattern.getPrefix());
            statement.setString(2, postfix);
            if (pattern.getRegex() != null){
                statement.setString(3, pattern.getRegex().pattern());
            }
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				String storedCode = rs.getString(DATASOURCE_COLUMN_NAME);
                String newCode = pattern.getSysCode();
                if (!storedCode.equals(newCode)){
                    throw new BridgeDBException(pattern + " has a different Code to what was registered. Expected "
                        + pattern.getSysCode() + " but found " + storedCode);
                }
			} else {
               throw new BridgeDBException("Unregistered pattern. " + pattern);
            }
		} catch (SQLException e) {
			throw new BridgeDBException("Unable to check pattern. " + query, e);
		}        
    }
    
    private void registerChain(int mappingSetId, Set<Integer> chainedLinkSets) throws BridgeDBException {
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

    private void registerVia(int mappingSetId, Set<String> viaLabels) throws BridgeDBException {
        if (viaLabels == null || viaLabels.isEmpty() ){
            processingRawLinkset = true;
            return;
        } else {
            processingRawLinkset = false;
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

    //TODO check regex
    @Override
    public void insertUriMapping(String sourceUri, String targetUri, int mappingSetId, boolean symetric) throws BridgeDBException {
        boolean ok = true;
        RegexUriPattern sourceUriPattern = subjectUriPatterns.get(mappingSetId);
        if (sourceUriPattern == null){
            throw new BridgeDBException("No SourceURIPattern regstered for mappingSetId " + mappingSetId);
        }
        int end = sourceUri.length() - sourceUriPattern.getPostfix().length();
        if (!sourceUri.startsWith(sourceUriPattern.getPrefix())){
            throw new BridgeDBException("SourceUri: " + sourceUri + " does not match the registered pattern "+sourceUriPattern);
        }
        if (!sourceUri.endsWith(sourceUriPattern.getPostfix())){
            throw new BridgeDBException("SourceUri: " + sourceUri + " does not match the registered pattern "+sourceUriPattern);
        }
        String sourceId = sourceUri.substring(sourceUriPattern.getPrefix().length(), end);
  
        RegexUriPattern targetUriPattern = targetUriPatterns.get(mappingSetId);
        if (targetUriPattern == null){
            throw new BridgeDBException("No TargetURIPattern regstered for mappingSetId " + mappingSetId);
        }
        if (!targetUri.startsWith(targetUriPattern.getPrefix())){
            throw new BridgeDBException("TargetUri: " + targetUri + " does not match the registered pattern "+targetUriPattern);
        }
        if (!targetUri.endsWith(targetUriPattern.getPostfix())){
            throw new BridgeDBException("TargetUri: " + targetUri + " does not match the registered pattern "+sourceUriPattern);
        }
        end = targetUri.length() - targetUriPattern.getPostfix().length();
        String targetId = targetUri.substring(targetUriPattern.getPrefix().length(), end);
        
        if (processingRawLinkset){
            if (sourceUriPattern.getRegex() != null){
                Matcher matcher = sourceUriPattern.getRegex().matcher(sourceId);
                if (!matcher.matches()){
                    if (!sourceUri.endsWith("-201")){
                        System.err.println(sourceUri + '"' + sourceId + '"');
                        System.err.println(sourceUriPattern.getRegex().pattern());
                    }
                    ok = false;
                } 
            }
            if (targetUriPattern.getRegex() != null){
                Matcher matcher = targetUriPattern.getRegex().matcher(targetId);
                if (!matcher.matches()){
                    if (!targetUri.endsWith("-201")){
                        System.err.println(targetUri + '"' + targetId + '"');
                        System.err.println(targetUriPattern.getRegex().pattern());
                        System.err.println(targetUriPattern.getSysCode());
                    }
                    ok = false;
                } 
            }
        }

        if (ok){
            this.insertLink(sourceId, targetId, mappingSetId, symetric);
        }
    }

    @Override
    public void closeInput() throws BridgeDBException {
        super.closeInput();
        countLinks();
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
    
    private DataSetInfo findDataSetInfo(String sysCode) throws BridgeDBException{
        DataSource ds = DataSource.getExistingBySystemCode(sysCode);
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

    private Set<Mapping> resultSetToMappingSet(IdSysCodePair sourceRef, ResultSet rs) throws BridgeDBException {
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
                if (sourceRef == null){
                    String sourceId = rs.getString(SOURCE_ID_COLUMN_NAME);
                    String sourceSysCode = rs.getString(SOURCE_DATASOURCE_COLUMN_NAME);
                    IdSysCodePair pair = new IdSysCodePair(sourceId, sourceSysCode);
                    source = codeMapper1.toXref(pair);
                } else {
                    source = codeMapper1.toXref(sourceRef);
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
    public List<MappingSetInfo> resultSetToMappingSetInfos(ResultSet rs ) throws BridgeDBException{
        ArrayList<MappingSetInfo> results = new ArrayList<MappingSetInfo>();
        try {
            while (rs.next()){
                int id = rs.getInt(ID_COLUMN_NAME);
                Set<DataSetInfo> viaSysCodes = getViaCodes(id);
                Set<Integer> chainIds = getChainIds(id);
                DataSetInfo sourceInfo = findDataSetInfo(rs.getString(SOURCE_DATASOURCE_COLUMN_NAME));
                DataSetInfo targetInfo = findDataSetInfo(rs.getString(TARGET_DATASOURCE_COLUMN_NAME));
              
                results.add(new MappingSetInfo(id, 
                        sourceInfo, 
                        rs.getString(PREDICATE_COLUMN_NAME), 
                        targetInfo, 
                        rs.getString(JUSTIFICATION_COLUMN_NAME), 
                        rs.getString(MAPPING_RESOURCE_COLUMN_NAME), 
                        rs.getString(MAPPING_SOURCE_COLUMN_NAME), 
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
                String mappingSource = rs.getString(MAPPING_SOURCE_COLUMN_NAME);
                String mappingResource = rs.getString(MAPPING_RESOURCE_COLUMN_NAME);
                mappingsBySet.addMapping(mappingSetId, predicate, justification, mappingSource, mappingResource, 
                        sourceUri, targetUris);
            }
       } catch (SQLException ex) {
            throw new BridgeDBException("Unable to parse results.", ex);
       }              
    }

    private void resultSetAddToMappingsBySet(ResultSet rs, String sourceUri, MappingsBySet mappingsBySet, 
            RegexUriPattern tgtUriPattern) throws BridgeDBException {
        try {
            while (rs.next()){
                String targetId = rs.getString(TARGET_ID_COLUMN_NAME);
                String targetUri = tgtUriPattern.getUri(targetId);
                Integer mappingSetId = rs.getInt(MAPPING_SET_ID_COLUMN_NAME);
                String predicate = rs.getString(PREDICATE_COLUMN_NAME);
                String justification = rs.getString(JUSTIFICATION_COLUMN_NAME);
                String mappingSource = rs.getString(MAPPING_SOURCE_COLUMN_NAME);
                String mappingResource = rs.getString(MAPPING_RESOURCE_COLUMN_NAME);
                mappingsBySet.addMapping(mappingSetId, predicate, justification, mappingSource, mappingResource, 
                        sourceUri, targetUri);
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
    private void checkExistingUriPatterns(RegexUriPattern uriPattern) throws BridgeDBException {
        String code = uriPattern.getSysCode();
        String prefix = uriPattern.getPrefix();
        String postfix = uriPattern.getPostfix();
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
        query.append(" = ? AND ");
        query.append(POSTFIX_COLUMN_NAME);
        query.append(" = ? ");
        if (uriPattern.getRegex() != null){
            query.append(" AND ");
            query.append(REGEX_COLUMN_NAME);
            query.append(" = ? ");
        }
        PreparedStatement statement = this.createPreparedStatement(query.toString());
        ResultSet rs;
        try {
            statement.setString(1, prefix);
            statement.setString(2, postfix);
            if (uriPattern.getRegex() != null){
                statement.setString(3, uriPattern.getRegex().pattern());
            }
            rs = statement.executeQuery();
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }    
        try {
            if (rs.next()){
                String dataSourceKey = rs.getString(DATASOURCE_COLUMN_NAME);
                if (!code.equals(dataSourceKey)){
                    throw new BridgeDBException ("UriPattern " + prefix + "$id" + postfix + " already mapped to " + dataSourceKey 
                        + " Which does not match " + code);
                }
            }
        } catch (SQLException ex) {
            throw new BridgeDBException("Unable to get SysCode. " + statement, ex);
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
        IdSysCodePair ref = toIdSysCodePair(xref);
        if (ref == null){
            return new HashSet<String>();
        }
        return toUris(ref);
    }

    private Set<String> toUris(IdSysCodePair ref) throws BridgeDBException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT " + PREFIX_COLUMN_NAME + ", " + POSTFIX_COLUMN_NAME);
        query.append(" FROM ");
        query.append(URI_TABLE_NAME);
        query.append(" WHERE ");
        query.append(DATASOURCE_COLUMN_NAME);
        query.append(" = '");
        query.append(ref.getSysCode());
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
                String uri = prefix + ref.getId() + postfix;
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

    private Set<String> getPatternCodes(String column) throws BridgeDBException {
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

    public Set<String> getJustifications() throws BridgeDBException {
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
        query.append(ID_COLUMN_NAME);
        query.append(") as maxCount FROM ");
        query.append(MAPPING_SET_TABLE_NAME);
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
    
    /**
     * Updates the count variable for each Mapping Sets.
     * <p>
     * This allows the counts of the mappings in each Mapping Set to be quickly returned.
     * @throws BridgeDBException 
     */
    private void countLinks () throws BridgeDBException{
        logger.info ("Updating link counts. Please Wait!");
        Statement countStatement = this.createStatement();
        String query = ("select " + ID_COLUMN_NAME
                + " from " + MAPPING_SET_TABLE_NAME 
                + " where " + MAPPING_LINK_COUNT_COLUMN_NAME + " is NULL");  
        ResultSet rs;
        //ystem.out.println(query);
        try {
            rs = countStatement.executeQuery(query);    
            while (rs.next()){
                int mappingSetId = rs.getInt(ID_COLUMN_NAME);
                int mappings = countLinks(mappingSetId);
                countFrequency(mappingSetId);
            }
            logger.info ("Updating counts finished!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
    }
    
    /**
     * Updates the count variable for each Mapping Sets.
     * <p>
     * This allows the counts of the mappings in each Mapping Set to be quickly returned.
     * @throws BridgeDBException 
     */
    private int countLinks (int mappingSetId ) throws BridgeDBException{
        logger.info ("Updating link count for " + mappingSetId + ". Please Wait!");
        Statement countStatement = this.createStatement();
        Statement updateStatement = this.createStatement();
        StringBuilder query = new StringBuilder("select count(distinct("); 
        query.append(SOURCE_ID_COLUMN_NAME); 
        query.append("))AS sources,");
        query.append(" COUNT(distinct("); 
        query.append(TARGET_ID_COLUMN_NAME); 
        query.append(")) as targets,");
        query.append(" COUNT(*) as mappings "); 
        query.append(" FROM "); 
        query.append(MAPPING_TABLE_NAME); 
        addStatsMappingSetIdConditions(query, mappingSetId);
        ResultSet rs;
        try {
            rs = countStatement.executeQuery(query.toString());    
            logger.info ("Count query run. Updating link count now");
            while (rs.next()){
                int sources = rs.getInt("sources");
                int targets = rs.getInt("targets");
                int mappings = rs.getInt("mappings");
                StringBuilder update = new StringBuilder("UPDATE ");
                update.append(MAPPING_SET_TABLE_NAME); 
                update.append(" SET "); 
                update.append(MAPPING_SOURCE_COUNT_COLUMN_NAME); 
                update.append(" = "); 
                update.append(sources); 
                update.append(", ");
                update.append(MAPPING_TARGET_COUNT_COLUMN_NAME);
                update.append(" = ");
                update.append(targets); 
                update.append(", ");
                update.append(MAPPING_LINK_COUNT_COLUMN_NAME);
                update.append(" = ");
                update.append(mappings);
                addStatsIdConditions(update, mappingSetId);
                //ystem.out.println(update);
                try {
                    int updateCount = updateStatement.executeUpdate(update.toString());
                    if (updateCount != 1){
                        throw new BridgeDBException("Updated rows " + updateCount + " <> 1 when running " + update);
                    }
                    logger.info ("Updating counts finished!");
                    return mappings;
                } catch (SQLException ex) {
                     throw new BridgeDBException("Unable to run update. " + update, ex);
                }
            }
            throw new BridgeDBException("No results for " + query);            
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
    }
        
    private void addStatsIdConditions(StringBuilder query, int mappingSetId){
        query.append(" WHERE "); 
        query.append(ID_COLUMN_NAME); 
        query.append(" = "); 
        query.append(mappingSetId);          
    }
    
    private void addStatsMappingSetIdConditions(StringBuilder query, int mappingSetId){
        query.append(" WHERE "); 
        query.append(MAPPING_SET_ID_COLUMN_NAME); 
        query.append(" = "); 
        query.append(mappingSetId);          
    }

    /**
     * Updates the count variable for each Mapping Sets.
     * <p>
     * This allows the counts of the mappings in each Mapping Set to be quickly returned.
     * @throws BridgeDBException 
     */
    private void countFrequency (int mappingSetId) throws BridgeDBException{
        float mappingsF = 0;
        //ystem.out.println ("Updating frequency count for " + mappingSetId + ". Please Wait!");
        logger.info ("Updating frequency count for " + mappingSetId + ". Please Wait!");
        Statement countStatement = this.createStatement();
        Statement updateStatement = this.createStatement();
        StringBuilder query = new StringBuilder("SELECT targetFrequency, COUNT("); 
        query.append(SOURCE_ID_COLUMN_NAME + ") as frequency"); 
        query.append(" FROM (SELECT ");  
        query.append(SOURCE_ID_COLUMN_NAME);  
        query.append(", COUNT(DISTINCT(");  
        query.append(TARGET_ID_COLUMN_NAME);  
        query.append(")) as targetFrequency"); 
        query.append(" from mapping"); 
        addStatsMappingSetIdConditions(query, mappingSetId); 
        query.append(" GROUP BY "); 
        query.append(SOURCE_ID_COLUMN_NAME); 
        query.append(") AS innerQuery"); 
        query.append(" GROUP BY targetFrequency ORDER BY targetFrequency");
        ResultSet rs;
        try {
            //ystem.out.println(query);
            rs = countStatement.executeQuery(query.toString());    
            logger.info ("Count query run. Updating link count now");
            while (rs.next()){
                mappingsF++;
            }
            rs.first();
            int frequencyCount = 0;
            int freqMedium = -1;
            int freq75 = -1;
            int freq90 = -1;
            int freq99 = -1;
            int targetFrequency = -1;
            while (rs.next()){
                targetFrequency = rs.getInt("targetFrequency");
                int frequency = rs.getInt("frequency");
                frequencyCount++;
                float frequencyPercent = frequencyCount/ mappingsF;
                if (frequencyPercent >= 0.5){
                    if (frequencyPercent >= 0.75){
                        if (frequencyPercent >= 0.90){
                            if (frequencyPercent >= 0.99){
                                if (freq99 < 0){
                                    freq99 = targetFrequency;
                                }
                            } else {
                                if (freq90 < 0){
                                    freq90 = targetFrequency;
                                }                                
                            }
                        } else {
                            if (freq75 < 0){
                                freq75 = targetFrequency;
                            }
                        }
                    } else {
                        if (freqMedium < 0){
                            freqMedium = targetFrequency;
                        }                    
                    }
                }
            }
            if (freq99 < 0){
                freq99 = targetFrequency;
            }                                
            if (freq90 < 0){
                freq90 = freq99;
            }                                
            if (freq75 < 0){
                freq75 = freq90;
            }                                
            if (freqMedium < 0){
                freqMedium = freq75;
            }                                
            StringBuilder update = new StringBuilder("update ");
            update.append(MAPPING_SET_TABLE_NAME); 
            update.append(" set "); 
            update.append(MAPPING_MEDIUM_FREQUENCY_COLUMN_NAME); 
            update.append(" = "); 
            update.append(freqMedium); 
            update.append(", "); 
            update.append(MAPPING_75_PERCENT_FREQUENCY_COLUMN_NAME); 
            update.append(" = "); 
            update.append(freq75); 
            update.append(", "); 
            update.append(MAPPING_90_PERCENT_FREQUENCY_COLUMN_NAME); 
            update.append(" = "); 
            update.append(freq90);
            update.append(", "); 
            update.append(MAPPING_99_PERCENT_FREQUENCY_COLUMN_NAME); 
            update.append(" = "); 
            update.append(freq99);
            update.append(", "); 
            update.append(MAPPING_MAX_FREQUENCY_COLUMN_NAME); 
            update.append(" = "); 
            update.append(targetFrequency);
            addStatsIdConditions(update, mappingSetId);
            //ystem.out.println(update);
            try {
                int updateCount = updateStatement.executeUpdate(update.toString());
                if (updateCount != 1){
                    throw new BridgeDBException("Updated rows " + updateCount + " <> 1 when running " + update);
                }
            } catch (SQLException ex) {
                    throw new BridgeDBException("Unable to run update. " + update, ex);
            }
            logger.info ("Updating frequency finished!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
    }

    private Set<RegexUriPattern> mergeGraphAndTargets(String graph, RegexUriPattern[] tgtUriPatterns) throws BridgeDBException {
        if (tgtUriPatterns == null || tgtUriPatterns.length == 0){
            return GraphResolver.getUriPatternsForGraph(graph);
        }
        if (graph == null || graph.trim().isEmpty()){            
            HashSet<RegexUriPattern> results = new HashSet<RegexUriPattern>(Arrays.asList(tgtUriPatterns));
            return results;
        }
        throw new BridgeDBException ("Illegal call with both graph and tgtUriPatterns parameters");
    }

}
 
