/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

import java.io.File;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.utils.Reporter;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Christian
 */
public class BridgeDBRdfHandlerTest {
    
   /**
     * Test of init method, of class BridgeDBRdfHandler.
     */
    @Test
    public void testInit() throws Exception {
        Reporter.println("init");
        BioDataSource.init();
        UriPattern.registerUriPatterns();
        UriPattern.refreshUriPatterns(); 
        BridgeDBRdfHandler.init();
        UriPattern pattern = UriPattern.byPattern("http://identifiers.org/mgd/$id");
        assertEquals("M", pattern.getCode());
        
        pattern = UriPattern.byPattern("http://www.informatics.jax.org/marker/$id");
        assertEquals("M", pattern.getCode());
        
        pattern = UriPattern.byPattern("http://purl.uniprot.org/mgi/$id");
        assertEquals("M", pattern.getCode());
        
        File file = new File ("test-data/GeneratedDataSource.ttl");
        Reporter.println("writing to " + file.getAbsolutePath());
        BridgeDBRdfHandler.writeRdfToFile(file);
        
        Reporter.println("Reading back in " + file.getAbsolutePath());
        BridgeDBRdfHandler.parseRdfFile(file);
    }

}
