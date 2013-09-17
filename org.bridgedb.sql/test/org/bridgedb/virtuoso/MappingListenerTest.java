// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright      2012  Christian Y. A. Brenninkmeijer
// Copyright      2012  OpenPhacts
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
package org.bridgedb.virtuoso;

import org.apache.log4j.Logger;
import org.bridgedb.sql.SQLAccess;
import org.bridgedb.sql.SQLIdMapper;
import org.bridgedb.sql.SQLListener;
import org.bridgedb.sql.SqlFactory;
import org.bridgedb.sql.SyscodeBasedCodeMapper;
import org.bridgedb.sql.TestSqlFactory;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.junit.BeforeClass;

/**
 * Loads the test data in and then runs the IDmapper and IDCapabiliies tests
 *
 * @author Christian
 */
//@Ignore 
public class MappingListenerTest extends org.bridgedb.mapping.MappingListenerTest {
    
    static final Logger logger = Logger.getLogger(MappingListenerTest.class);

    @BeforeClass
    public static void setupIDMapper() throws BridgeDBException{
        connectionOk = false;
        SqlFactory.setUseMySQL(false);
        TestSqlFactory.checkSQLAccess();
        ConfigReader.useTest();
        listener = new SQLListener(true);
        loadData();
        idMapper = new SQLIdMapper(false, new SyscodeBasedCodeMapper());
        connectionOk = true;
        capabilities = idMapper.getCapabilities(); 
        logger.info("Virtuoso Setup successfull");
    }
            
}
