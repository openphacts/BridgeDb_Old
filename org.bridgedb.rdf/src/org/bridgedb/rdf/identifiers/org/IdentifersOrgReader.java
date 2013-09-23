/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf.identifiers.org;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.rdf.RdfBase;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.constants.DCatConstants;
import org.bridgedb.rdf.constants.IdenitifiersOrgConstants;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
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
    
    static {
        multiples = new HashSet();
        multiples.add("http://www.ebi.ac.uk/ena/data/view/$id");
        multiples.add("http://www.genome.jp/dbget-bin/www_bget?$id");
        multiples.add("http://www.chemspider.com/$id");
        multiples.add("http://www.uniprot.org/uniprot/$id");
        multiples.add("http://purl.uniprot.org/uniprot/$id");
        multiples.add("http://www.gramene.org/db/genes/search_gene?acc=$id");
        multiples.add("http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val=$id");
        multiples.add("http://www.ncbi.nlm.nih.gov/protein/$id");
    }
 
    private void doParseRdfInputStream(InputStream stream) throws BridgeDBException {
        Repository repository = null;
        RepositoryConnection repositoryConnection = null;
        try {
            repository = new SailRepository(new MemoryStore());
            repository.initialize();
            repositoryConnection = repository.getConnection();
            repositoryConnection.add(stream, DEFAULT_BASE_URI, DEFAULT_FILE_FORMAT);
            showVoidUriSpaces(repositoryConnection);
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

    public static void main(String[] args) throws Exception {
        BioDataSource.init();
        UriPattern.registerUriPatterns();
        //BridgeDBRdfHandler.init();
        URL url = new URL("http://www.ebi.ac.uk/miriam/demo/export/registry.ttl");
        InputStream stream = url.openStream();
        //File file = new File("c:/Temp/registry.ttl");
        //FileInputStream stream = new FileInputStream(file);
        IdentifersOrgReader reader = new IdentifersOrgReader();
        reader.doParseRdfInputStream(stream);
        stream.close();
        
        File mergedFile = new File("resources/IdentifiersOrgDataSource.ttl");
        BridgeDBRdfHandler.writeRdfToFile(mergedFile);
        BridgeDBRdfHandler.parseRdfFile(mergedFile);  
    }

    private void showVoidUriSpaces(RepositoryConnection repositoryConnection) throws Exception{
        int count = 0;
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, VoidConstants.URI_SPACE_URI, null, true);
        while(statements.hasNext()) {
            Statement statement = statements.next();
         
            DataSource dataSource = DataSource.getByIdentiferOrgBase(statement.getObject().stringValue());
            Resource catalogRecord = statement.getSubject();  
            if (dataSource == null){
                dataSource = readDataSource(repositoryConnection, catalogRecord);
            }
            loadUriPatterns(repositoryConnection, catalogRecord, dataSource);
            count++;
        }
        System.out.println("found " + count);
    }

    private DataSource readDataSource(RepositoryConnection repositoryConnection, Resource catalogRecord) throws Exception{
        String sysCode = getSingletonString(repositoryConnection, catalogRecord, IdenitifiersOrgConstants.NAMESPACE_URI);
        String fullName = getSingletonString(repositoryConnection, catalogRecord, DCatConstants.TITLE_URI);
        if (fullName.equals("UniGene")){
            fullName = "UniGene (identifiers.org)";
        }
        DataSource ds = DataSource.register(sysCode, fullName).asDataSource();
        return ds;
    }

    private void loadUriPatterns(RepositoryConnection repositoryConnection, Resource CatalogRecord, DataSource dataSource) throws Exception{
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
                    System.out.println("\t Skipping shared " + patternString);
                } else {
                    System.out.println("\t" + patternString);
                    UriPattern pattern = UriPattern.byPattern(accessUrlStatement.getObject().stringValue());
                    String dataSourceSysCode = null;
                    if (dataSource != null){
                        dataSourceSysCode = dataSource.getSystemCode();
                    }
                    String patternSysCode = null;
                    if (pattern != null){
                        patternSysCode = pattern.getCode();
                    }
                    System.out.println("\t\t" + dataSourceSysCode + "   " + patternSysCode);
                    if (patternSysCode != null && !patternSysCode.equals(dataSourceSysCode)){
                        throw new BridgeDBException (patternString +" maps to " + patternSysCode 
                                + " but Datasource is " + dataSourceSysCode + " from " + dataSource);
                    }
                }
            }
        }       
    }


}
//http://www.ebi.ac.uk/ena/data/view/$id
//http://www.genome.jp/dbget-bin/www_bget?$id
//http://www.chemspider.com/$id
//http://www.uniprot.org/uniprot/$id
//http://purl.uniprot.org/uniprot/$id
//http://www.gramene.org/db/genes/search_gene?acc=$id
//http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val=$id
