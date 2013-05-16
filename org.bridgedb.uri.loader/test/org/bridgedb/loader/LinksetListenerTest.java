/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.loader;

import java.io.File;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
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
public class LinksetListenerTest {
    
    static final String MAIN_JUSTIFCATION = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
    static final String LENS_JUSTIFCATION = "http://www.bridgedb.org/test#testJustification";
    static final URI linkPredicate = new URIImpl("http://www.w3.org/2004/02/skos/core#exactMatch");
    static SQLUriMapper uriListener;
    static LinksetListener instance;

    public LinksetListenerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws BridgeDBException {
        uriListener = SQLUriMapper.factory(true, StoreType.TEST);
        instance = new LinksetListenerImpl(uriListener);
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
        Reporter.println("parsing " + fileName);
        File file = new File(fileName);
        int mappingSetId = instance.parse(file, linkPredicate, justification);
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
        loadFile("../org.bridgedb.uri.loader/test-data/cw-cm.ttl", MAIN_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cw-ct.ttl", MAIN_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cw-dd.ttl", MAIN_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cw-dt.ttl", MAIN_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cw-cs_test_lens.ttl", LENS_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cs-cm_test_lens.ttl", LENS_JUSTIFCATION);
        loadFile("../org.bridgedb.uri.loader/test-data/cw-cm_test_lens.ttl", LENS_JUSTIFCATION);
    }

 }
//        linksetLoader.load("../org.bridgedb.tools.metadata/test-data/chemspider-void.ttl", StoreType.LOAD, ValidationType.VOID);
//        linksetLoader.load("../org.bridgedb.tools.metadata/test-data/chembl-rdf-void.ttl", StoreType.LOAD, ValidationType.VOID);
