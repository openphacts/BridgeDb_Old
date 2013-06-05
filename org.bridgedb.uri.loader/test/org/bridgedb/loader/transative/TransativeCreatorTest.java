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
package org.bridgedb.loader.transative;

import org.bridgedb.uri.loader.transative.TransativeCreator;
import java.io.File;
import org.bridgedb.uri.loader.LinksetListener;
import org.bridgedb.uri.loader.RdfParser;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.TransitiveConfig;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author Christian
 */
public class TransativeCreatorTest {
    
    protected static final String MAIN_JUSTIFCATION = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
    static final String LENS_JUSTIFCATION = "http://www.bridgedb.org/test#testJustification";
    static final URI linkPredicate = new URIImpl("http://www.w3.org/2004/02/skos/core#exactMatch");
    static SQLUriMapper uriListener;
    static LinksetListener instance;

    public TransativeCreatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws BridgeDBException {
        TransitiveConfig.useTestDirectory();
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

    protected void loadFile(String fileName, String justification) throws BridgeDBException{
        File file = new File(fileName);
        loadFile(file, justification);
    }
    
    protected void loadFile(File file, String justification) throws BridgeDBException{
        Reporter.println("parsing " + file.getAbsolutePath());
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
        File transative = TransativeCreator.doTransativeIfPossible(1, 3, StoreType.TEST);
        assertTrue(transative.exists());
        loadFile(transative, MAIN_JUSTIFCATION);
    }

 }
