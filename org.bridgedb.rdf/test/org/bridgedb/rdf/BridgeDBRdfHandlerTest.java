/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

import java.io.File;
import java.util.SortedSet;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.utils.Reporter;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
        DataSourceTxtReader.init();
        UriPattern.registerUriPatterns();
        UriPattern.refreshUriPatterns(); 
        BridgeDBRdfHandler.init();
        
        SortedSet<UriPattern> result = UriPattern.byCodeAndType("M", UriPatternType.mainUrlPattern);
        UriPattern expected = UriPattern.byPattern("http://www.informatics.jax.org/marker/$id");
        assertThat(result, hasItem(expected));
        assertEquals(1, result.size());
        
        result = UriPattern.byCodeAndType("M", UriPatternType.identifiersOrgPattern);
        expected = UriPattern.byPattern("http://identifiers.org/mgd/$id");
        assertThat(result, hasItem(expected));
        assertEquals(1, result.size());
        
        result = UriPattern.byCodeAndType("M", UriPatternType.codeMapperPattern);
        expected = UriPattern.byPattern("http://purl.uniprot.org/mgi/$id");
        assertThat(result, hasItem(expected));
        assertThat(result.size(), greaterThanOrEqualTo(1));
        
        File file = new File ("test-data/GeneratedDataSource.ttl");
        Reporter.println("writing to " + file.getAbsolutePath());
        BridgeDBRdfHandler.writeRdfToFile(file);
        
        Reporter.println("Reading back in " + file.getAbsolutePath());
        BridgeDBRdfHandler.parseRdfFile(file);
    }

}
