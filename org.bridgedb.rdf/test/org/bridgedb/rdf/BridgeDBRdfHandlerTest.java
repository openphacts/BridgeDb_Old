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
package org.bridgedb.rdf;

import java.io.File;
import org.bridgedb.IDMapperException;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.utils.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christian
 */
public class BridgeDBRdfHandlerTest extends TestUtils{
    
    private static File file1 = new File("test-data/CreatedByTest.ttl");
    private static File file2 = new File("test-data/CreatedByTestWithPrimaries.ttl");

    public BridgeDBRdfHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IDMapperException {
        BioDataSource.init();
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

    /**
     * Test of writeRdfToFile method, of class BridgeDBRdfHandler.
     */
    @Test
    public void testWriteRdfToFile() throws Exception {
        report("writeRdfToFile");
        BridgeDBRdfHandler.writeRdfToFile(file1, false);
    }

    /**
     * Test of writeRdfToFile method, of class BridgeDBRdfHandler.
     */
    @Test
    public void testWriteRdfToFileAddPrimaries() throws Exception {
        report("writeRdfToFile");
        BridgeDBRdfHandler.writeRdfToFile(file2, true);
    }

    /**
     * Test of parseRdfFile method, of class BridgeDBRdfHandler.
     */
    @Test
    public void testParseRdfFile() throws Exception {
        report("parseRdfFile ");
        BridgeDBRdfHandler.parseRdfFile(file1);
    }

   /**
     * Test of parseRdfFile method, of class BridgeDBRdfHandler.
     */
    @Test
    public void testParseRdfFileWithPrimaries() throws Exception {
        report("parseRdfFile ");
        BridgeDBRdfHandler.parseRdfFile(file2);
    }
}