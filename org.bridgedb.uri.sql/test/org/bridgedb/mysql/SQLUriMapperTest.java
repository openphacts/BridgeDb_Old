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
import org.bridgedb.DataSource;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.TestSqlFactory;
import org.bridgedb.uri.UriListenerTest;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the UriMapper interface (and by loading the UriListener interface)
 *
 * Should be passable by any implementation of UriMapper that has the test data loaded.
 * 
 * @author Christian
 */
public abstract class SQLUriMapperTest extends UriListenerTest{
   
    private static SQLUriMapper sqlUriMapper;
    
    @BeforeClass
    public static void setupIDMapper() throws BridgeDBException{
        TestSqlFactory.checkSQLAccess();
        sqlUriMapper = SQLUriMapper.factory(false, StoreType.TEST);
    }
    
    @Test 
    public void testToUriPattern() throws BridgeDBException{
        report("ToUriPattern");
        UriPattern result = sqlUriMapper.toUriPattern(map1Uri1);
        assertEquals(uriPattern1, result);
    }

    @Test 
    public void testToUriPatternUsingLike() throws BridgeDBException{
        report("ToUriPatternUsingLike");
        UriPattern result = sqlUriMapper.toUriPattern("http://bio2rdf.org/chebi:1234");
        UriPattern target = UriPattern.byPrefixOrNameSpace("http://bio2rdf.org/chebi:");
        assertEquals(target, result);
    }

    @Test 
    public void testToUriPatternUsingLike2() throws BridgeDBException{
        report("ToUriPatternUsingLike2");
        DataSource test2 = DataSource.register("testToUriPatternUsingLike2", "testToUriPatternUsingLike2").asDataSource();
        UriPattern target = UriPattern.byPrefixAndPostfix("http://bio2rdf.org/junk:", ".html");
        sqlUriMapper.registerUriPattern(test2, "http://bio2rdf.org/junk:$id.html");
        UriPattern result = sqlUriMapper.toUriPattern("http://bio2rdf.org/junk:1234.html");
        assertEquals(target, result);
    }
    
    @Test 
    public void testGetJustifications() throws BridgeDBException{
        report("GetJustifications");
        Set<String> results = sqlUriMapper.getJustifications();
        assertThat (results.size(), greaterThanOrEqualTo(2));
    }
   
}
