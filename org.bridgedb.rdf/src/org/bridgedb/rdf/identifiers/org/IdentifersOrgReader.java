/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf.identifiers.org;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.rdf.RdfBase;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.UriPatternType;
import org.bridgedb.rdf.constants.DCatConstants;
import org.bridgedb.rdf.constants.IdenitifiersOrgConstants;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.Reporter;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author Christian
 */
public class IdentifersOrgReader extends RdfBase {
    
    private static final Logger logger = Logger.getLogger(IdentifersOrgReader.class);  
    
    private static final Set<String> multiples;
    private static boolean initRun = false;
    
    static {
        multiples = new HashSet();
        multiples.add("http://linkedchemistry.info/chembl/chemblid/$id");
     }
 
    private void doParseRdfInputStream(InputStream stream) throws BridgeDBException {
        Repository repository = null;
        RepositoryConnection repositoryConnection = null;
        try {
            repository = new SailRepository(new MemoryStore());
            repository.initialize();
            repositoryConnection = repository.getConnection();
            repositoryConnection.add(stream, DEFAULT_BASE_URI, DEFAULT_FILE_FORMAT);
//            for (String multiple:multiples){
//                checkMultiple(repositoryConnection, multiple);
//            }
            Reporter.println("Registry read in. Now loading DataSources");
            loadData(repositoryConnection);
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

    public static void init() throws BridgeDBException{
        if (initRun){
            return;
        }
        try {
            URL url = new URL("http://www.ebi.ac.uk/miriam/main/export/registry.ttl");
            Reporter.println("Readng " + url);
            InputStream stream = url.openStream();
            IdentifersOrgReader reader = new IdentifersOrgReader();
            reader.doParseRdfInputStream(stream);
            stream.close();        
            initRun = true;
        } catch (MalformedURLException ex) {
            throw new BridgeDBException ("Error reading miriam registry.", ex);
        } catch (IOException ex) {
            throw new BridgeDBException ("Error reading miriam registry.", ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
        DataSourceTxt.init();
        UriPattern.registerUriPatterns();
        BridgeDBRdfHandler.init();
        init();
        
        File mergedFile = new File("resources/IdentifiersOrgDataSource.ttl");
        BridgeDBRdfHandler.writeRdfToFile(mergedFile);
        BridgeDBRdfHandler.parseRdfFile(mergedFile);  
        
        File textFile = new File("resources/IdentifiersOrgDataSource.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
        DataSourceTxt.writeToBuffer(writer);
        InputStream is = new FileInputStream(textFile);
        DataSourceTxt.loadInputStrem(is);
    }

    private void loadData(RepositoryConnection repositoryConnection) throws Exception{
        int count = 0;
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, VoidConstants.URI_SPACE_URI, null, true);
        while(statements.hasNext()) {
            Statement statement = statements.next();
            DataSource dataSource = DataSource.getByIdentiferOrgBase(statement.getObject().stringValue());
            Resource catalogRecord = statement.getSubject();  
            if (dataSource == null){
                dataSource = readDataSource(repositoryConnection, catalogRecord, statement.getObject().stringValue());
            } else {
                compareDataSource(repositoryConnection, catalogRecord, dataSource);
            }
            loadExtraDataSourceInfo(repositoryConnection, catalogRecord, dataSource);
            Pattern regex = loadRegex(repositoryConnection, catalogRecord, dataSource);
            loadUriPatterns(repositoryConnection, catalogRecord, dataSource, regex);
            count++;
        }
    }

    private DataSource readDataSource(RepositoryConnection repositoryConnection, Resource catalogRecord, 
            String identiferOrgBase) throws Exception{
        String sysCode = getSingletonString(repositoryConnection, catalogRecord, IdenitifiersOrgConstants.NAMESPACE_URI);
        String fullName = getSingletonString(repositoryConnection, catalogRecord, DCatConstants.TITLE_URI);
        if (fullName.equals("UniGene")){
            fullName = "UniGene number";
        }
        UriPattern identifiersOrgPattern = 
            UriPattern.register(identiferOrgBase + "$id", sysCode, UriPatternType.identifiersOrgPattern);
             
        DataSource ds = DataSource.register(sysCode, fullName)
                .identifiersOrgBase(identiferOrgBase)
                .asDataSource();
        return ds;
    }

    private void compareDataSource(RepositoryConnection repositoryConnection, Resource catalogRecord, 
            DataSource dataSource) throws Exception{
        String sysCode = getSingletonString(repositoryConnection, catalogRecord, IdenitifiersOrgConstants.NAMESPACE_URI);
        String fullName = getSingletonString(repositoryConnection, catalogRecord, DCatConstants.TITLE_URI);
        if (!dataSource.getFullName().equals(fullName)){
            System.err.println("FullName mistamtch for " + dataSource.getSystemCode() + " BridgeDb has " + dataSource.getFullName() 
                    + " while miriam uses " + fullName);
        }
        if (dataSource.getAlternative() != null && !dataSource.getAlternative().equals(fullName)){
            System.err.println("Alternative mistamtch for " + dataSource.getSystemCode() + " BridgeDb has " + dataSource.getAlternative()
                    + " while miriam uses " + fullName);
        }
    }
    
    private void loadExtraDataSourceInfo(RepositoryConnection repositoryConnection, Resource catalogRecord, 
            DataSource dataSource) throws RepositoryException, BridgeDBException {
        if (dataSource.getExample().getId() == null){
            String id = getPossibleSingletonString(repositoryConnection, catalogRecord, VoidConstants.EXAMPLE_RESOURCE);
            DataSource.register(dataSource.getSystemCode(), dataSource.getFullName()).idExample(id);
        }
   }

    private Pattern loadRegex(RepositoryConnection repositoryConnection, Resource catalogRecord, DataSource dataSource) 
            throws RepositoryException, BridgeDBException {
        String regexSt = getPossibleSingletonString(repositoryConnection, catalogRecord, IdenitifiersOrgConstants.REGEX_URI);
        Pattern regex = null;
        if (regexSt != null){
            regex = Pattern.compile(regexSt);
        }
        Pattern dataSourceRegex = DataSourcePatterns.getPatterns().get(dataSource);
        if (dataSourceRegex != null && !dataSourceRegex.pattern().equals(regex.pattern())){
            System.err.println("Regex patterns do not match for " + catalogRecord 
                    + " was " + regex + " but BridgeBD has " + dataSourceRegex);
        }
        if (regex != null){
            DataSourcePatterns.registerPattern(dataSource, regex);
        }
        return regex;
    }

    private void loadUriPatterns(RepositoryConnection repositoryConnection, Resource CatalogRecord, 
            DataSource dataSource, Pattern regex) throws Exception{
        //ystem.out.println("Looking for " + CatalogRecord);
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(CatalogRecord, DCatConstants.DISTRIBUTION_URI, null, true);
        while(statements.hasNext()) {
            Statement statement = statements.next();
            Resource Distribution = (Resource)statement.getObject();  
            RepositoryResult<Statement> accessUrlStatements = 
                    repositoryConnection.getStatements(Distribution, DCatConstants.ACCESS_URL_URI, null, true);
            while(accessUrlStatements.hasNext()) {
                Statement accessUrlStatement = accessUrlStatements.next();
                String patternString =  accessUrlStatement.getObject().stringValue();
                if (multiples.contains(patternString)){
                    //ystem.out.println("\t Skipping shared " + patternString);
                } else {
                    //ystem.out.println("\t" + patternString);
                    //UriPattern pattern = UriPattern.byPattern(accessUrlStatement.getObject().stringValue());
                    UriPattern pattern = UriPattern.register(patternString, dataSource.getSystemCode(), UriPatternType.dataSourceUriPattern);
                    String dataSourceSysCode = null;
                    if (dataSource != null){
                        dataSourceSysCode = dataSource.getSystemCode();
                        if (dataSource.getKnownUrl("$id") == null){
                            DataSource.register(dataSourceSysCode, dataSource.getFullName()).urlPattern(patternString);
                        }
                    }
                }
            }
        }       
    }
    
    private void checkMultiple(RepositoryConnection repositoryConnection, String multiple) throws Exception{
        URI uri = new URIImpl(multiple);
        RepositoryResult<Statement> accessStatements = 
                repositoryConnection.getStatements(null, null, uri, true);
        while(accessStatements.hasNext()) {
            Statement accessStatement = accessStatements.next();
            //ystem.out.println(accessStatement);
            Resource distribution = accessStatement.getSubject();
            RepositoryResult<Statement> distributionStatements = 
                    repositoryConnection.getStatements(null, null, distribution , true);
            while(distributionStatements.hasNext()) {
               Statement distributionStatement = distributionStatements.next();
               //ystem.out.println("\t" + distributionStatement);
               Resource catalog = distributionStatement.getSubject();
               RepositoryResult<Statement> regexStatements = 
                    repositoryConnection.getStatements(catalog, IdenitifiersOrgConstants.REGEX_URI, null,  true);
               while(regexStatements.hasNext()) {
                   Statement regexStatement = regexStatements.next();
                   String regex = regexStatement.getObject().stringValue();
               }
            }
        }
    }


}
