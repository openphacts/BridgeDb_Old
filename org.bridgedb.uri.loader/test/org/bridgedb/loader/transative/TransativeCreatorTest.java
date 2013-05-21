/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.loader.transative;

import java.io.File;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.RdfParser;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.DirectoriesConfig;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Christian
 */
public class TransativeCreatorTest {
    
    static final String MAIN_JUSTIFCATION = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
    static final String LENS_JUSTIFCATION = "http://www.bridgedb.org/test#testJustification";
    static final URI linkPredicate = new URIImpl("http://www.w3.org/2004/02/skos/core#exactMatch");
    static SQLUriMapper uriListener;
    static LinksetListener instance;

    public TransativeCreatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws BridgeDBException {
        DirectoriesConfig.useTestDirectory();
        uriListener = SQLUriMapper.factory(true, StoreType.TEST);
        instance = new LinksetListener(uriListener);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private void loadFile(String fileName, String justification) throws BridgeDBException{
        File file = new File(fileName);
        loadFile(file, justification);
    }
    
    private void loadFile(File file, String justification) throws BridgeDBException{
        Reporter.println("parsing " + file.getAbsolutePath());
        String source = RdfParser.fileToURI(file);
        int mappingSetId = instance.parse(file, source, linkPredicate, justification);
        MappingSetInfo mapping = uriListener.getMappingSetInfo(mappingSetId);
        int numberOfLinks = mapping.getNumberOfLinks();
        assertThat(numberOfLinks, greaterThanOrEqualTo(3));      
    }
    /**
     * Test of parse method, of class LinksetListener.
     */
    @Test
    public void testLoadTestData() throws Exception {
        Reporter.println("LoadTestData");
        loadFile("../org.bridgedb.uri.loader/test-data/cw-cs.ttl", MAIN_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cs-cm.ttl", MAIN_JUSTIFCATION);
        File transative = TransativeCreator.doTransativeIfPossible(1, 3, StoreType.TEST);
        assertTrue(transative.exists());
        loadFile(transative, MAIN_JUSTIFCATION);
    }

 }
