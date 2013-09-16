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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.RdfConstants;
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
            readRegexPattern(repositoryConnection, dataSourceResource, dataSource);
            readUriPatterns(repositoryConnection, dataSourceResource, dataSource);
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
        String fullName = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.FULL_NAME_URI);
        String systemCode = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.SYSTEM_CODE_URI);
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

        Value uriValue = getPossibleSingleton(repositoryConnection, dataSourceId, BridgeDBConstants.HAS_URL_PATTERN_URI);
        if (uriValue != null){
            UriPattern uriPattern = getUriPattern(repositoryConnection, (Resource)uriValue);
            uriPattern.setDataSource(builder.asDataSource());
            builder.urlPattern(uriPattern.getUriPattern());
        }
        
        String urnBase = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.URN_BASE_URI);
        if (urnBase != null){
            builder.urnBase(urnBase);
        }
        
        Value identifiersOrgValue = getPossibleSingleton(repositoryConnection, dataSourceId, BridgeDBConstants.HAS_IDENTIFERS_ORG_PATTERN_URI);
        if (identifiersOrgValue != null){
            UriPattern uriPattern = getUriPattern(repositoryConnection, (Resource)identifiersOrgValue);
            uriPattern.setDataSource(builder.asDataSource());
            builder.identifiersOrgBase(uriPattern.getUriPattern());
        }
        
        return builder.asDataSource();
    }
    
    private static void readUrlPattern(RepositoryConnection repositoryConnection, Resource dataSourceId, 
            DataSource.Builder builder) throws BridgeDBException, RepositoryException{
//TODO not use DataSourceURI
        UriPattern uriPattern = UriPattern.readUriPattern(repositoryConnection, dataSourceId, null, 
                BridgeDBConstants.HAS_URL_PATTERN_URI);
        if (uriPattern != null){
            builder.urlPattern(uriPattern.getUriPattern());
        }       
    }
 
    private void readRegexPattern(RepositoryConnection repositoryConnection, Resource subject, DataSource dataSource) {
        //ystem.out.println("skipping read regex");
    }

    private void readUriPatterns(RepositoryConnection repositoryConnection, Resource subject, DataSource dataSource) throws BridgeDBException, RepositoryException {
       RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(subject, BridgeDBConstants.HAS_URI_PATTERN_URI, null, true);
                //repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Value uriValue = statement.getObject();
            UriPattern uriPattern = getUriPattern(repositoryConnection, (Resource)uriValue);
            uriPattern.setDataSource(dataSource);
         }
    }

    private void readAllUriPatterns(RepositoryConnection repositoryConnection) throws RepositoryException, BridgeDBException {
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, RdfConstants.TYPE_URI, BridgeDBConstants.URI_PATTERN_URI, true);
                //repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            Resource uriPatternResource = statement.getSubject();
            UriPattern uriPattern = getUriPattern(repositoryConnection, uriPatternResource);
        }
   }

    private UriPattern getUriPattern(RepositoryConnection repositoryConnection, Resource uriPatternResource) 
            throws BridgeDBException, RepositoryException {
        UriPattern result = uriPatternRegister.get(uriPatternResource);
        if (result == null){
            result = UriPattern.readUriPattern(repositoryConnection, uriPatternResource);
            uriPatternRegister.put(uriPatternResource, result);
        }
        return result;
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
        writeRdfToFile(file, DataSource.getDataSources());
    }
    
    public static void writeRdfToFile(File file, Collection<DataSource> dataSources) throws BridgeDBException{
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

//        for (String alternativeFullName:inner.getAlternativeFullNames()){
//            repositoryConnection.add(id, BridgeDBConstants.ALTERNATIVE_FULL_NAME_URI, new LiteralImpl(alternativeFullName));            
//        }
        
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
            URIImpl URL = new URIImpl(url);
            repositoryConnection.add(id, BridgeDBConstants.HAS_URL_PATTERN_URI, URL);
        }

/*        String identifersOrgPattern = inner.getIdentifiersOrgUri("$id");
        if (identifersOrgPattern == null){
            String urnPattern = inner.getURN("");
            if (urnPattern.length() > 1){
                Value urnBase = new LiteralImpl(urnPattern.substring(0, urnPattern.length()-1));
                repositoryConnection.add(id, BridgeDBConstants.URN_BASE_URI, urnBase);
            }
        } else {            
            UriPattern identifersOrgUriPattern = UriPattern.existingOrCreateByPattern(identifersOrgPattern);
            writeUriPattern(repositoryConnection, BridgeDBConstants.HAS_IDENTIFERS_ORG_PATTERN_URI, identifersOrgUriPattern);
        }
*/
        if (dataSource.getOrganism() != null){
            Organism organism = (Organism)dataSource.getOrganism();
            repositoryConnection.add(id, BridgeDBConstants.ORGANISM_URI, OrganismRdf.getResourceId(organism));
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
