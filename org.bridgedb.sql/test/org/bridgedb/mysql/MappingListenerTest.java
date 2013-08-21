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
package org.bridgedb.mysql;

import java.util.Set;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.sql.SQLIdMapper;
import org.bridgedb.sql.SQLListener;
import org.bridgedb.sql.TestSqlFactory;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Loads the test data in and then runs the IDmapper and IDCapabiliies tests
 *
 * @author Christian
 */
//@Ignore 
public class MappingListenerTest extends org.bridgedb.mapping.MappingListenerTest {
    
    static final Logger logger = Logger.getLogger(MappingListenerTest.class);
    static SQLIdMapper sqlIdMapper;
    
    @BeforeClass
    public static void setupIDMapper() throws BridgeDBException{
        connectionOk = false;
        TestSqlFactory.checkSQLAccess();
        ConfigReader.useTest();
        listener = new SQLListener(true);
        loadData();
        sqlIdMapper = new SQLIdMapper(false);
        idMapper  = sqlIdMapper;
        connectionOk = true;
        capabilities = idMapper.getCapabilities(); 
        logger.info("MySQL Setup successfull");
    }
    
    @Test
    public void testMapIDOneToOne() throws IDMapperException{
        report("MapIDOneToOne");
        Set<Xref> results = sqlIdMapper.mapID(map1xref1, DataSource2);
        assertTrue(results.contains(map1xref2));
        assertFalse(results.contains(map1xref3));
        assertFalse(results.contains(map2xref1));
        assertFalse(results.contains(map2xref2));
        assertFalse(results.contains(map2xref2));
    }
   
    @Test
    public void testFrequency() throws IDMapperException{
        report("Frequency");
        DataSource DataSourceA = DataSource.register("testFrequencyA", "testFrequencyA").asDataSource();
        DataSource DataSourceB = DataSource.register("testFrequencyB", "testFrequencyB").asDataSource();
        int mappingSet = listener.registerMappingSet(DataSourceA, TEST_PREDICATE, TEST_JUSTIFICATION1, DataSourceB, 
                "MappingListenerTest.loadData()", SYMETRIC, NO_VIA, NO_CHAIN);
        listener.insertLink("1", "A", mappingSet, SYMETRIC);
        listener.insertLink("1", "B", mappingSet, SYMETRIC);
        listener.insertLink("1", "C", mappingSet, SYMETRIC);
        listener.insertLink("1", "D", mappingSet, SYMETRIC);
        listener.insertLink("1", "E", mappingSet, SYMETRIC);
        listener.insertLink("11", "A1", mappingSet, SYMETRIC);
        listener.insertLink("11", "B1", mappingSet, SYMETRIC);
        listener.insertLink("11", "C1", mappingSet, SYMETRIC);
        listener.insertLink("11", "D1", mappingSet, SYMETRIC);
        listener.insertLink("11", "E1", mappingSet, SYMETRIC);
        listener.insertLink("12", "A1", mappingSet, SYMETRIC);
        listener.insertLink("12", "B1", mappingSet, SYMETRIC);
        listener.insertLink("12", "C1", mappingSet, SYMETRIC);
        listener.insertLink("12", "D1", mappingSet, SYMETRIC);
        listener.insertLink("2", "A", mappingSet, SYMETRIC);
        listener.insertLink("2", "B", mappingSet, SYMETRIC);
        listener.insertLink("2", "C", mappingSet, SYMETRIC);
        listener.insertLink("2", "D", mappingSet, SYMETRIC);
        listener.insertLink("3", "A", mappingSet, SYMETRIC);
        listener.insertLink("3", "B", mappingSet, SYMETRIC);
        listener.insertLink("3", "C", mappingSet, SYMETRIC);
        listener.insertLink("4", "A", mappingSet, SYMETRIC);
        listener.insertLink("4", "B", mappingSet, SYMETRIC);
        listener.insertLink("5", "B", mappingSet, SYMETRIC);
        listener.closeInput();
     }
      
}
