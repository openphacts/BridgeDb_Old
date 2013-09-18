/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

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
        BridgeDBRdfHandler.init();
        UriPattern pattern = UriPattern.byPattern("http://identifiers.org/mgd/MGI:$id");
        assertEquals("M", pattern.getCode());
        
        pattern = UriPattern.byPattern("http://www.informatics.jax.org/marker/MGI:$id");
        assertEquals("M", pattern.getCode());
        
        pattern = UriPattern.byPattern("http://purl.uniprot.org/mgi/$id");
        assertEquals("M", pattern.getCode());
    }

}
