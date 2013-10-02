package org.bridgedb.uri;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;

/**
 *
 * @author Christian
 */
public class GraphResolver {

   private HashMap<String,Set<RegexUriPattern>> allowedUriPattern;
    
    private final static String PROPERTIES_FILE = "graph.properties";
    private final static String PROPERTY_PREFIX = "context.";
    private final static String PATTERN  = "pattern";
    private final static String GRAPH_POSTFIX  = ".graph";
    
    private static GraphResolver instance;
    
    public static GraphResolver getInstance() throws BridgeDBException{
        if (instance == null){
            BridgeDBRdfHandler.init();
            instance = new GraphResolver();
        }
        return instance;
    }
    
    private GraphResolver() throws BridgeDBException{
        readProperties();
    }
    
    private void readProperties() throws BridgeDBException{
        allowedUriPattern = new HashMap<String,Set<RegexUriPattern>>();
        Properties properties = ConfigReader.getProperties(PROPERTIES_FILE);
        Set<String> keys = properties.stringPropertyNames();
        for (String key:keys){
            if (key.startsWith(PROPERTY_PREFIX)){
                String[] parts = key.split("\\.");
                if (parts[2].equals(PATTERN)){
                    String graphKey = PROPERTY_PREFIX + parts[1] + GRAPH_POSTFIX;
                    String graph =  properties.getProperty(graphKey);
                    String pattern = properties.getProperty(key);
                    addPattern(graph, pattern);
                }
            }
        }
    }
    
    private void addPattern(String graph, String pattern) throws BridgeDBException{
        RegexUriPattern uriPattern = RegexUriPattern.byPattern(pattern);
        if (uriPattern == null){
            throw new BridgeDBException("no UriPattern known for " + pattern);
        }
        addPattern(graph, uriPattern);
    }
    
    private void addPattern(String graph, RegexUriPattern uriPattern) throws BridgeDBException{
        Set<RegexUriPattern> patterns = allowedUriPattern.get(graph);
        if (patterns == null){
            patterns = new HashSet<RegexUriPattern>();
        }
        patterns.add(uriPattern);
        allowedUriPattern.put(graph, patterns);
    }

    public static Set<RegexUriPattern> getUriPatternsForGraph(String graph) throws BridgeDBException {
        if (graph == null || graph.isEmpty()){
            return new HashSet<RegexUriPattern>();
        }
        GraphResolver resolver = getInstance();
        Set<RegexUriPattern> results = resolver.getAllowedPatterns(graph);
        return results;
    }

    public static void addMapping(String graph, String pattern) throws BridgeDBException{
        GraphResolver gr = getInstance();
        gr.addPattern(graph, pattern); 
    }
    
    public static void addMapping(String graph, UriPattern uriPattern) throws BridgeDBException{
        GraphResolver gr = getInstance();
        gr.addPattern(graph, RegexUriPattern.byPattern(uriPattern));        
    }
    
    public static void addTestMappings() throws BridgeDBException{
        GraphResolver gr = getInstance();
        gr.addPattern("http://larkc.eu#Fixedcontext", "http://www.conceptwiki.org/concept/$id");
        gr.addPattern("http://www.chemspider.com", "http://rdf.chemspider.com/$id");
        gr.addPattern("http://data.kasabi.com/dataset/chembl-rdf","http://data.kasabi.com/dataset/chembl-rdf/molecule/m$id");
        gr.addPattern("http://data.kasabi.com/dataset/chembl-rdf","http://data.kasabi.com/dataset/chembl-rdf/target/t$id");
    }

    private Set<RegexUriPattern> getAllowedPatterns(String graph) {
        return allowedUriPattern.get(graph);
    }

 }
