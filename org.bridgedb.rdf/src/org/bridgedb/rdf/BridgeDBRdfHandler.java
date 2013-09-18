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
package org.bridgedb.rdf;

import info.aduna.lang.FileFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.RdfConstants;
import org.bridgedb.rdf.pairs.RdfBasedCodeMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author Christian
 */
public class BridgeDBRdfHandler extends RdfBase{
   
    static boolean initialized = false;
    public static String DEFAULT_BASE_URI = "http://no/BaseURI/Set/";
    public static RDFFormat DEFAULT_FILE_FORMAT = RDFFormat.TURTLE;
    public static final String CONFIG_FILE_NAME = "DataSource.ttl";

    static final Logger logger = Logger.getLogger(BridgeDBRdfHandler.class);

    private HashMap<Resource, DataSource> dataSourceRegister = new HashMap<Resource, DataSource>();
    private HashMap<Resource, UriPattern> uriPatternRegister = new HashMap<Resource, UriPattern>();

    private BridgeDBRdfHandler(){
        
    }
    
    private void doParseRdfInputStream(InputStream stream) throws BridgeDBException {
        Repository repository = null;
        RepositoryConnection repositoryConnection = null;
        try {
            repository = new SailRepository(new MemoryStore());
            repository.initialize();
            repositoryConnection = repository.getConnection();
            repositoryConnection.add(stream, DEFAULT_BASE_URI, DEFAULT_FILE_FORMAT);
            readAllDataSources(repositoryConnection);
            readAllUriPatterns(repositoryConnection);      
        } catch (Exception ex) {
            throw new BridgeDBException ("Error parsing Rdf inputStream ", ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                logger.error("Error closing input Stream", ex);
            }
            shutDown(repository, repositoryConnection);
        }
    }

    private void readAllDataSources(RepositoryConnection repositoryConnection) throws RepositoryException, BridgeDBException {
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, RdfConstants.TYPE_URI, BridgeDBConstants.DATA_SOURCE_URI, true);
                //repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Resource dataSourceResource = statement.getSubject();
            DataSource dataSource = getDataSource(repositoryConnection, dataSourceResource);
        }
    }
    
    private DataSource getDataSource(RepositoryConnection repositoryConnection, Resource dataSourceResource) 
            throws BridgeDBException, RepositoryException {
        DataSource result = dataSourceRegister.get(dataSourceResource);
        if (result == null){
            result = readDataSource(repositoryConnection, dataSourceResource);
            dataSourceRegister.put(dataSourceResource, result);
        }
        return result;
    }

    public DataSource readDataSource(RepositoryConnection repositoryConnection, Resource dataSourceId) 
            throws BridgeDBException, RepositoryException{
        String fullName = getSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.FULL_NAME_URI);
        String systemCode = getSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.SYSTEM_CODE_URI);
        DataSource.Builder builder = DataSource.register(systemCode, fullName);

        String idExample = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.ID_EXAMPLE_URI);
        if (idExample != null){
            builder.idExample(idExample);
        }
        
        String mainUrl = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.MAIN_URL_URI);
        if (mainUrl != null){
            builder.mainUrl(mainUrl);
        }
  
        Value organismId = getPossibleSingleton(repositoryConnection, dataSourceId, BridgeDBConstants.ORGANISM_URI);
        if (organismId != null){
            Object organism = OrganismRdf.byRdfResource(organismId);
            builder.organism(organism);
        }
            
        String primary = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.PRIMAY_URI);
        if (primary != null){
            builder.primary(Boolean.parseBoolean(primary));
        }

        String type = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.TYPE_URI);
        if (type != null){
            builder.type(type);
        }

        Value regexValue = getPossibleSingleton(repositoryConnection, dataSourceId, BridgeDBConstants.HAS_REGEX_PATTERN_URI);
        if (regexValue != null){
            Pattern pattern = Pattern.compile(regexValue.stringValue());
            DataSourcePatterns.registerPattern(builder.asDataSource(), pattern);
        }
        
        String xrefPrefix = readCodeMapper (repositoryConnection, systemCode);
        
        Value uriValue = getPossibleSingleton(repositoryConnection, dataSourceId, BridgeDBConstants.HAS_URL_PATTERN_URI);
        if (uriValue != null){
            String pattern = getUriPatternWithoutXrefPrefix(repositoryConnection, (Resource)uriValue, systemCode, xrefPrefix);
            builder.urlPattern(pattern);
        }
        
        String urnBase = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.URN_BASE_URI);
        if (urnBase != null){
            builder.urnBase(urnBase);
        }
        
        Value identifiersOrgValue = getPossibleSingleton(repositoryConnection, dataSourceId, BridgeDBConstants.HAS_IDENTIFERS_ORG_PATTERN_URI);
        if (identifiersOrgValue != null){
            String pattern = getUriPatternWithoutXrefPrefix(repositoryConnection, (Resource)identifiersOrgValue, systemCode, xrefPrefix);
            builder.identifiersOrgBase(pattern);
        }
        
        readUriPatterns(repositoryConnection, dataSourceId, systemCode, xrefPrefix);
 
        return builder.asDataSource();
    }
    
    private String getUriPatternWithoutXrefPrefix(RepositoryConnection repositoryConnection, Resource uriPatternId, 
            String sysCode, String xrefPrefix) throws BridgeDBException, RepositoryException{
         UriPattern uriPattern = getUriPattern(repositoryConnection, uriPatternId, sysCode, xrefPrefix);
         String pattern = uriPattern.getUriPattern();
         if (xrefPrefix != null){
             pattern = pattern.replace(xrefPrefix + "$id", "$id");
         }
         return pattern;
    }
    
    private String readCodeMapper(RepositoryConnection repositoryConnection, String systemCode) throws RepositoryException, BridgeDBException {
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, BridgeDBConstants.SYSTEM_CODE_URI, new LiteralImpl(systemCode), true);
        String xrefPrefix = null;
        Resource codeMapperReseource = null;
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Resource subject = statement.getSubject();
            String newPrefix = getPossibleSingletonString(repositoryConnection, subject, BridgeDBConstants.XREF_PREFIX_URI);
            if (newPrefix != null){
                if (xrefPrefix == null){
                    xrefPrefix = newPrefix;
                    codeMapperReseource = subject;
                    RdfBasedCodeMapper.addXrefPrefix(systemCode, newPrefix);
                } else {
                    throw new BridgeDBException (" Two different " + BridgeDBConstants.XREF_PREFIX_URI 
                            + " statements found for sysCode " + systemCode
                            + " with " + codeMapperReseource + " and " + subject);
                }
            }
            if (codeMapperReseource != null){
                //Uris here will NOT include the prefix
                this.readUriPatterns(repositoryConnection, codeMapperReseource, systemCode, null);
            }
        }
        return xrefPrefix;
    }

    private void readUriPatterns(RepositoryConnection repositoryConnection, Resource subject, String sysCode,
            String xrefPrefix) throws BridgeDBException, RepositoryException {
       RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(subject, BridgeDBConstants.HAS_URI_PATTERN_URI, null, true);
                //repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Value uriValue = statement.getObject();
            UriPattern uriPattern = getUriPattern(repositoryConnection, (Resource)uriValue, sysCode, xrefPrefix);
         }
    }

    private UriPattern getUriPattern(RepositoryConnection repositoryConnection, Resource uriPatternResource, String code,
            String xrefPrefix) 
            throws BridgeDBException, RepositoryException {
        UriPattern result = uriPatternRegister.get(uriPatternResource);
        if (result == null){
            result = UriPattern.readUriPattern(repositoryConnection, uriPatternResource, code, xrefPrefix);
            uriPatternRegister.put(uriPatternResource, result);
        }
        return result;
    }

    private void readAllUriPatterns(RepositoryConnection repositoryConnection) throws RepositoryException, BridgeDBException {
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, RdfConstants.TYPE_URI, BridgeDBConstants.URI_PATTERN_URI, true);
                //repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Resource uriPatternResource = statement.getSubject();
            UriPattern uriPattern = uriPatternRegister.get(uriPatternResource);
            if (uriPattern == null){
                throw new BridgeDBException ("Found an unused  "+ BridgeDBConstants.URI_PATTERN_URI + uriPatternResource);
            }
        }
   }
    //Static methods
    
    public static void parseRdfFile(File file) throws BridgeDBException{
        try {
            InputStream inputStream = new FileInputStream(file);
            parseRdfInputStream(inputStream);
        } catch (IOException ex) {
            throw new BridgeDBException ("Error accessing file " + file.getAbsolutePath(), ex);
        }
    }
    
    static void parseRdfInputStream(InputStream stream) throws BridgeDBException {
        BridgeDBRdfHandler handler = new BridgeDBRdfHandler();
        handler.doParseRdfInputStream(stream);
    }
    
    public static void main(String[] args) throws RepositoryException, BridgeDBException, IOException, RDFParseException, RDFHandlerException {
        ConfigReader.logToConsole();
        File file1 = new File ("C:\\OpenPhacts\\BridgeDb\\org.bridgedb.rdf\\resources\\DataSource.ttl");
        parseRdfFile(file1);
    }

    public static void init() throws BridgeDBException{
        if (initialized){
            return;
        }
        InputStream stream = ConfigReader.getInputStream(CONFIG_FILE_NAME);
        parseRdfInputStream(stream);
        initialized = true;
        Reporter.println("BridgeDBRdfHandler initialized");
    }
    
    public static void writeRdfToFile(File file) throws BridgeDBException{
        TreeSet<DataSource> sortedDataSources = new TreeSet<DataSource>(new  DataSourceComparator());
        sortedDataSources.addAll(DataSource.getDataSources());
        writeRdfToFile(file, sortedDataSources);
    }
    
    public static void writeRdfToFile(File file, SortedSet<DataSource> dataSources) throws BridgeDBException{
        Reporter.println("Writing DataSource RDF to " + file.getAbsolutePath());
        Repository repository = null;
        RepositoryConnection repositoryConnection = null;
        try {
            repository = new SailRepository(new MemoryStore());
            repository.initialize();
            repositoryConnection = repository.getConnection();
            for (DataSource dataSource: dataSources){
                writeDataSource(repositoryConnection, dataSource);
            }
            OrganismRdf.addAll(repositoryConnection);
            UriPattern.addAll(repositoryConnection);
            writeRDF(repositoryConnection, file);        
        } catch (Exception ex) {
            throw new BridgeDBException ("Error writing Rdf to file ", ex);
        } finally {
            shutDown(repository, repositoryConnection);
        }
    }
    
    private static void writeDataSource(RepositoryConnection repositoryConnection, DataSource dataSource) throws RepositoryException {
        Resource id = asResource(dataSource);
        repositoryConnection.add(id, RdfConstants.TYPE_URI, BridgeDBConstants.DATA_SOURCE_URI);         
        
        if (dataSource.getFullName() != null){
            repositoryConnection.add(id, BridgeDBConstants.FULL_NAME_URI, new LiteralImpl(dataSource.getFullName()));
        }

        if (dataSource.getSystemCode() != null && (!dataSource.getSystemCode().trim().isEmpty())){
            repositoryConnection.add(id, BridgeDBConstants.SYSTEM_CODE_URI, new LiteralImpl(dataSource.getSystemCode()));
        }
        
        if (dataSource.getMainUrl() != null){
            repositoryConnection.add(id, BridgeDBConstants.MAIN_URL_URI, new LiteralImpl(dataSource.getMainUrl()));
        }

        if (dataSource.getExample() != null && dataSource.getExample().getId() != null){
            repositoryConnection.add(id, BridgeDBConstants.ID_EXAMPLE_URI, new LiteralImpl(dataSource.getExample().getId()));
        }
 
        if (dataSource.isPrimary()){
            repositoryConnection.add(id, BridgeDBConstants.PRIMAY_URI, BooleanLiteralImpl.TRUE);
        } else {
            repositoryConnection.add(id, BridgeDBConstants.PRIMAY_URI, BooleanLiteralImpl.FALSE);
        }
 
        if (dataSource.getType() != null){
            repositoryConnection.add(id, BridgeDBConstants.TYPE_URI, new LiteralImpl(dataSource.getType()));
        } 

        String url = dataSource.getKnownUrl("$id");
        if (url != null){
            //HACK as SwissProt and Trembl share same URL
            if (!dataSource.getSystemCode().equals("Sp")){
                URIImpl URL = new URIImpl(url);
                repositoryConnection.add(id, BridgeDBConstants.HAS_URL_PATTERN_URI, URL);
            }
        }

        String identifersOrgPattern = dataSource.getIdentifiersOrgUri("$id");
        if (identifersOrgPattern != null){
            URIImpl URL = new URIImpl(identifersOrgPattern);
            repositoryConnection.add(id, BridgeDBConstants.HAS_IDENTIFERS_ORG_PATTERN_URI, URL);
        }

        if (dataSource.getOrganism() != null){
            Organism organism = (Organism)dataSource.getOrganism();
            repositoryConnection.add(id, BridgeDBConstants.ORGANISM_URI, OrganismRdf.getResourceId(organism));
        }
        
        Pattern pattern = DataSourcePatterns.getPatterns().get(dataSource);
        if (pattern != null){
            Value patternValue = new LiteralImpl(pattern.toString());
            repositoryConnection.add(id, BridgeDBConstants.HAS_REGEX_PATTERN_URI, patternValue);            
        }
    }

    private static void writeRDF(RepositoryConnection repositoryConnection, File file) 
            throws IOException, RDFHandlerException, RepositoryException{
        Writer writer = new FileWriter (file);
        TurtleWriter turtleWriter = new TurtleWriter(writer);
        writeRDF(repositoryConnection, turtleWriter);
        writer.close();
    }
    
    private static void writeRDF(RepositoryConnection repositoryConnection, RDFWriter rdfWriter) 
            throws IOException, RDFHandlerException, RepositoryException{ 
        rdfWriter.handleNamespace(BridgeDBConstants.PREFIX_NAME1, BridgeDBConstants.PREFIX);
        rdfWriter.handleNamespace("", DEFAULT_BASE_URI);
        rdfWriter.startRDF();
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            rdfWriter.handleStatement(statement);
        }
        rdfWriter.endRDF();
    }
    
    private static RDFFormat getFormat(File file){
        String fileName = file.getName();
        if (fileName.endsWith(".n3")){
            fileName = "try.ttl";
        }
        RDFParserRegistry reg = RDFParserRegistry.getInstance();
        FileFormat fileFormat = reg.getFileFormatForFileName(fileName);
        if (fileFormat == null || !(fileFormat instanceof RDFFormat)){
            //added bridgeDB/OPS specific extension here if required.           
            logger.warn("OpenRDF does not know the RDF Format for " + fileName);
            logger.warn("Using the default format " + DEFAULT_FILE_FORMAT);
            return DEFAULT_FILE_FORMAT;
        } else {
            return (RDFFormat)fileFormat;
        }
    }

    protected static Resource asResource(DataSource dataSource) {
        if (dataSource.getFullName() == null){
            return new URIImpl(BridgeDBConstants.DATA_SOURCE1 + "_bysysCode_" + scrub(dataSource.getSystemCode()));
        } else {
            return new URIImpl(BridgeDBConstants.DATA_SOURCE1 + "_" + scrub(dataSource.getFullName()));
        }
    }


}
