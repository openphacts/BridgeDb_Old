/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.RdfConstants;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 *
 * @author Christian
 */
public class DataSourceRdf extends RdfBase  {
    
    private static HashMap<String, DataSource> register = new HashMap<String, DataSource>();

    public static String getRdfLabel(DataSource dataSource) {
        return scrub(dataSource.getFullName());        
    }
    
    public static String getRdfId(DataSource dataSource) {
        return ":" + BridgeDBConstants.DATA_SOURCE_LABEL + "_" + getRdfLabel(dataSource);
    }

    public static URI getResourceId(DataSource dataSource) {
        return new URIImpl(BridgeDBConstants.DATA_SOURCE1 + "_" + getRdfLabel(dataSource));
    }
    
    static DataSource byRdfResource(Value dataSourceId) throws BridgeDBException {
        String shortName = convertToShortName(dataSourceId);
        DataSource result = register.get(shortName);
        if (result == null){
            //Load all Datasource in case it came from elseWhere
            for (DataSource dataSource: DataSource.getDataSources()){
                register.put(getRdfId(dataSource), dataSource);
            }
            //Check again
            result = register.get(shortName);
        }
        if (result == null){
            throw new BridgeDBException("No DataSource known for Id " + dataSourceId + " / " + shortName);
        }
        return result;
    }

    public static void addAll(RepositoryConnection repositoryConnection) throws IOException, RepositoryException {
        for (DataSource dataSource:DataSource.getDataSources()){
            add(repositoryConnection, dataSource); 
        }
    }

    public static void add(RepositoryConnection repositoryConnection, DataSource dataSource) throws IOException, RepositoryException {
        URI id = getResourceId(dataSource);
        repositoryConnection.add(id, RdfConstants.TYPE_URI, BridgeDBConstants.DATA_SOURCE_URI);         
        repositoryConnection.add(id, BridgeDBConstants.FULL_NAME_URI, new LiteralImpl(dataSource.getFullName()));

        if (dataSource.getSystemCode() != null && (!dataSource.getSystemCode().trim().isEmpty())){
            repositoryConnection.add(id, BridgeDBConstants.SYSTEM_CODE_URI, new LiteralImpl(dataSource.getSystemCode()));
        }

        //Alternative names
        
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

        if (!VERSION2){
            String urlPattern = dataSource.getUrl("$id");
            if (urlPattern.length() > 3){
                repositoryConnection.add(id, BridgeDBConstants.URL_PATTERN_URI, new LiteralImpl(urlPattern));
            }
        }

        if (!VERSION2){
            String urnPattern = dataSource.getURN("");
            if (urnPattern.length() > 1){
                repositoryConnection.add(id, BridgeDBConstants.URN_BASE_URI, 
                        new LiteralImpl(urnPattern.substring(0, urnPattern.length()-1)));
            }
        }

        if (dataSource.getOrganism() != null){
            Organism organism = (Organism)dataSource.getOrganism();
            repositoryConnection.add(id, BridgeDBConstants.ORGANISM_URI, OrganismRdf.getResourceId(organism));
        }
    }

    public static void writeAllAsRDF(BufferedWriter writer) throws IOException {
        for (DataSource dataSource:DataSource.getDataSources()){
            writeAsRDF(writer, dataSource);
        }
    }

    public static void writeAsRDF(BufferedWriter writer, DataSource dataSource) throws IOException {
        writer.write(getRdfId(dataSource)); 
        writer.write(" a ");
        writer.write(BridgeDBConstants.DATA_SOURCE_SHORT);        
        writer.write("; ");        
        writer.newLine();
         
        if (dataSource.getSystemCode() != null && (!dataSource.getSystemCode().trim().isEmpty())){
            writer.write("         ");
            writer.write(BridgeDBConstants.SYSTEM_CODE_SHORT);        
            writer.write(" \"");
            writer.write(dataSource.getSystemCode());
            writer.write("\";");
            writer.newLine();
        }

        if (dataSource.getMainUrl() != null){
            writer.write("         ");
            writer.write(BridgeDBConstants.MAIN_URL_SHORT);        
            writer.write(" \"");
            writer.write(dataSource.getMainUrl());
            writer.write("\";");
            writer.newLine();
        }

       if (dataSource.getExample() != null && dataSource.getExample().getId() != null){
            writer.write("         ");
            writer.write(BridgeDBConstants.ID_EXAMPLE_SHORT);
            writer.write(" \"");
            writer.write(dataSource.getExample().getId());
            writer.write("\";");
            writer.newLine();
        }

        writer.write("         ");
        writer.write(BridgeDBConstants.PRIMAY_SHORT);
        if (dataSource.isPrimary()){
            writer.write(" \"true\"^^xsd:boolean;");
        } else {
            writer.write(" \"false\"^^xsd:boolean;");            
        }
        writer.newLine();

        if (dataSource.getType() != null){
            writer.write("         ");
            writer.write(BridgeDBConstants.TYPE_SHORT);
            writer.write(" \"");
            writer.write(dataSource.getType());
            writer.write("\";");
            writer.newLine();
       }

        if (!VERSION2){
            String urlPattern = dataSource.getUrl("$id");
            if (urlPattern.length() > 3){
                writer.write("         ");
                writer.write(BridgeDBConstants.URL_PATTERN_SHORT);
                writer.write(" \"");
                writer.write(urlPattern);
                writer.write("\";");
                writer.newLine();
            }
        }

        if (!VERSION2){
            String urnPattern = dataSource.getURN("");
            if (urnPattern.length() > 1){
                writer.write("         ");
                writer.write(BridgeDBConstants.URN_BASE_SHORT);
                writer.write(" \"");
                writer.write(urnPattern.substring(0, urnPattern.length()-1));
                writer.write("\";");
                writer.newLine();
                //if (urnPattern.length() >= 11){
                    //String identifersOrgBase = "http://identifiers.org/" + urnPattern.substring(11, urnPattern.length()-1) + "/";
                    //writer.write("         bridgeDB:");
                    //writer.write(BridgeDBConstants.IDENTIFIERS_ORG_BASE);
                    //writer.write(" \"");
                    //writer.write(identifersOrgBase);
                    //writer.write("\";");
                    //writer.newLine();
            }
        }

        /*String wikiNameSpace = mappings.get(dataSource.getFullName());
        if (wikiNameSpace != null){
            writer.write("         bridgeDB:");
            writer.write(BridgeDBConstants.WIKIPATHWAYS_BASE);
            writer.write(" \"");
            writer.write(wikiNameSpace);
            writer.write("\";");
            writer.newLine();
        }
        */
        if (dataSource.getOrganism() != null){
            Organism organism = (Organism)dataSource.getOrganism();
            writer.write("         ");
            writer.write(BridgeDBConstants.ORGANISM_SHORT);
            writer.write(" ");
            writer.write(OrganismRdf.getRdfId(organism));
            writer.write(";");    
            writer.newLine();
        }

        writer.write("         ");
        writer.write(BridgeDBConstants.FULL_NAME_SHORT);
        writer.write(" \"");
        writer.write(dataSource.getFullName());
        writer.write("\".");
        writer.newLine();                
    }

    public static void readAllDataSources(RepositoryConnection repositoryConnection) throws BridgeDBException, RepositoryException{
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, RdfConstants.TYPE_URI, BridgeDBConstants.DATA_SOURCE_URI, true);
                //repositoryConnection.getStatements(null, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            DataSource ds = readDataSources(repositoryConnection, statement.getSubject());
        }
    }

    public static DataSource readDataSources(RepositoryConnection repositoryConnection, Resource dataSourceId) 
            throws BridgeDBException, RepositoryException{
        String fullName = getSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.FULL_NAME_URI);
        String systemCode = getPossibleSingletonString(repositoryConnection, dataSourceId, BridgeDBConstants.SYSTEM_CODE_URI);
        DataSource.Builder builder = DataSource.register(systemCode, fullName);
        
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(dataSourceId, null, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            try{
                processStatement(statement, builder);
            } catch (Exception e){
                throw new BridgeDBException ("Error processing statement " + statement, e);
            }
        }
        return builder.asDataSource();
    }

    private static void processStatement(Statement statement, DataSource.Builder builder) throws BridgeDBException{
        if (statement.getPredicate().equals(RdfConstants.TYPE_URI)){
            //Ignore the type statement
        } else if (statement.getPredicate().equals(BridgeDBConstants.ALTERNATIVE_FULL_NAME_URI)){
            builder.alternativeFullName(statement.getObject().stringValue());
        } else if (statement.getPredicate().equals(BridgeDBConstants.FULL_NAME_URI)){
            //Already used the fullName statement;
        } else if (statement.getPredicate().equals(BridgeDBConstants.ID_EXAMPLE_URI)){
            builder.idExample(statement.getObject().stringValue());
        } else if (statement.getPredicate().equals(BridgeDBConstants.MAIN_URL_URI)){
            builder.mainUrl(statement.getObject().stringValue());
        } else if (statement.getPredicate().equals(BridgeDBConstants.ORGANISM_URI)){
            Value organismId = statement.getObject();
            Object organism = OrganismRdf.byRdfResource(organismId);
            builder.organism(organism);
        } else if (statement.getPredicate().equals(BridgeDBConstants.PRIMAY_URI)){
            builder.primary (Boolean.parseBoolean(statement.getObject().stringValue()));
        } else if (statement.getPredicate().equals(BridgeDBConstants.SYSTEM_CODE_URI)){
            //Already used the systemCode statement;
        } else if (statement.getPredicate().equals(BridgeDBConstants.TYPE_URI)){
            builder.type(statement.getObject().stringValue());
        } else if (statement.getPredicate().equals(BridgeDBConstants.URL_PATTERN_URI)){
            String urlPattern = statement.getObject().stringValue();
            builder.urlPattern(urlPattern);
            registerUriPattern(builder.asDataSource(), urlPattern, UriMappingRelationship.DATA_SOURCE_URL_PATTERN);
        } else if (statement.getPredicate().equals(BridgeDBConstants.URN_BASE_URI)){
            builder.urnBase(statement.getObject().stringValue());
        } else if (statement.getPredicate().equals(BridgeDBConstants.IDENTIFIERS_ORG_BASE_URI)){
            registerNameSpace(builder.asDataSource(), statement.getObject().stringValue(), UriMappingRelationship.IDENTIFERS_ORG);
        } else if (statement.getPredicate().equals(BridgeDBConstants.WIKIPATHWAYS_BASE_URI)){
            registerNameSpace(builder.asDataSource(), statement.getObject().stringValue(), UriMappingRelationship.WIKIPATHWAYS);
        } else if (statement.getPredicate().equals(BridgeDBConstants.SOURCE_RDF_URI)){
            registerUriPattern(builder.asDataSource(), statement.getObject().stringValue(), UriMappingRelationship.SOURCE_RDF);
        } else if (statement.getPredicate().equals(BridgeDBConstants.BIO2RDF_URI)){
            registerUriPattern(builder.asDataSource(), statement.getObject().stringValue(), UriMappingRelationship.BIO2RDF_URI);
        } else {
            throw new BridgeDBException ("Unexpected Statement " + statement);
        }
    }
    
    public static DataSource readRdf(Resource dataSourceId, Set<Statement> dataSourceStatements) throws BridgeDBException{
        String fullName = null;
        String idExample = null;
        String mainUrl = null;
        Object organism = null;
        String primary = null;
        String systemCode = null;
        String type = null;
        String urlPattern = null;
        String urnBase = null;
        String identifiersOrgBase = null;
        String wikipathwaysBase = null;
        String bio2RDFPattern = null;
        String sourceRDFURIPattern = null;
        HashSet<String> alternativeFullNames = new HashSet<String>();
        
        for (Statement statement:dataSourceStatements){
            if (statement.getPredicate().equals(RdfConstants.TYPE_URI)){
                //Ignore the type statement
            } else if (statement.getPredicate().equals(BridgeDBConstants.ALTERNATIVE_FULL_NAME_URI)){
                alternativeFullNames.add(statement.getObject().stringValue());
            } else if (statement.getPredicate().equals(BridgeDBConstants.FULL_NAME_URI)){
                fullName = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.ID_EXAMPLE_URI)){
                idExample = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.MAIN_URL_URI)){
                mainUrl = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.ORGANISM_URI)){
                Value organismId = statement.getObject();
                Object Organism = OrganismRdf.byRdfResource(organismId);
            } else if (statement.getPredicate().equals(BridgeDBConstants.PRIMAY_URI)){
                primary = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.SYSTEM_CODE_URI)){
                systemCode = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.TYPE_URI)){
                type = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.URL_PATTERN_URI)){
                urlPattern = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.URN_BASE_URI)){
                urnBase = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.IDENTIFIERS_ORG_BASE_URI)){
                identifiersOrgBase = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.WIKIPATHWAYS_BASE_URI)){
                wikipathwaysBase = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.SOURCE_RDF_URI)){
                sourceRDFURIPattern = statement.getObject().stringValue();
            } else if (statement.getPredicate().equals(BridgeDBConstants.BIO2RDF_URI)){
                bio2RDFPattern = statement.getObject().stringValue();
            } else {
                throw new BridgeDBException ("Unexpected Statement " + statement);
            }
        }
        DataSource.Builder builder = DataSource.register(systemCode, fullName);
        for (String alternativeFullName:alternativeFullNames){
            builder.alternativeFullName(alternativeFullName);
        }
        if (mainUrl != null) {
            builder.mainUrl(mainUrl);
        }
        if (urlPattern != null) {
            builder.urlPattern(urlPattern);
        }
        if (idExample != null) {
            builder.idExample(idExample);
        }
        if (type != null) {
            builder.type(type);
        }
        if (organism != null) {
            builder.organism(organism);
        }					      
        if (primary != null) {
            builder.primary (Boolean.parseBoolean(primary));
        }					      
        if (urnBase != null) {
            builder.urnBase(urnBase);
        }
        DataSource dataSource = builder.asDataSource();
        registerUriPattern(dataSource, urlPattern, UriMappingRelationship.DATA_SOURCE_URL_PATTERN);
        registerNameSpace(dataSource, identifiersOrgBase, UriMappingRelationship.IDENTIFERS_ORG);
        registerNameSpace(dataSource, wikipathwaysBase, UriMappingRelationship.WIKIPATHWAYS);
        registerUriPattern(dataSource, sourceRDFURIPattern, UriMappingRelationship.SOURCE_RDF);
        registerUriPattern(dataSource, bio2RDFPattern, UriMappingRelationship.BIO2RDF_URI);
        register.put(getRdfId(dataSource), dataSource);
        return dataSource;
    }

    private static void registerUriPattern(DataSource dataSource, String urlPattern, UriMappingRelationship uriMappingRelationship) throws BridgeDBException {
        if (urlPattern == null || urlPattern.isEmpty()) {
            return;
        }
        UriPattern pattern = UriPattern.byUrlPattern(urlPattern);
        UriMapping.addMapping(dataSource, pattern, uriMappingRelationship);
    }

    private static void registerNameSpace(DataSource dataSource, String nameSpace, UriMappingRelationship uriMappingRelationship) throws BridgeDBException {
        if (nameSpace == null || nameSpace.isEmpty()) {
            return;
        }
        UriPattern pattern = UriPattern.byNameSpace(nameSpace);
        UriMapping.addMapping(dataSource, pattern, uriMappingRelationship);
    }

}