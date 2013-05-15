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
package org.bridgedb.linkset.transative;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.bridgedb.DataSource;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.TestSqlFactory;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.Lens;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import org.bridgedb.utils.TestUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author Christian
 */
@Ignore
public class TransativeFinderTest extends TestUtils  {
 
    SQLUriMapper mapper;
//    LinksetLoader linksetLoader;
    
    @Before
    public void testLoader() throws BridgeDBException, IOException, OpenRDFException, FileNotFoundException {
        //Check database is running and settup correctly or kill the test. 
        TestSqlFactory.checkSQLAccess();
        mapper = SQLUriMapper.factory(false, StoreType.TEST);
//        linksetLoader = new LinksetLoader();
//        linksetLoader.clearExistingData( StoreType.TEST);  
        DataSource transativeTestA = DataSource.register("TransativeTestA", "TransativeTestA").asDataSource();
        mapper.registerUriPattern(transativeTestA, "http://www.example.com/DS_A/$id");
        DataSource transativeTestB = DataSource.register("TransativeTestB", "TransativeTestB").asDataSource();
        mapper.registerUriPattern(transativeTestB, "http://www.example.com/DS_B/$id");
        DataSource transativeTestC = DataSource.register("TransativeTest_C", "TransativeTest_C").asDataSource();
        mapper.registerUriPattern(transativeTestC, "http://www.example.com/DS_C/$id");
        DataSource transativeTestD = DataSource.register("TransativeTestD", "TransativeTestD").asDataSource();
        mapper.registerUriPattern(transativeTestD, "http://www.example.com/DS_D/$id");
        DataSource transativeTestE = DataSource.register("TransativeTestE", "TransativeTestE").asDataSource();
        mapper.registerUriPattern(transativeTestE, "http://www.example.com/DS_E/$id");
        DataSource transativeTestF = DataSource.register("TransativeTestF", "TransativeTestF").asDataSource();
        mapper.registerUriPattern(transativeTestE, "http://www.example.com/DS_F/$id");
	}

    @Test
	public void testFinder1() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleEToD.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToC.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToD.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(20, results.getNumberOfMappingSets());
        report("testFinder Done");
	}
	
    @Test
 	public void testFinder2() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder2");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToC.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToD.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleEToD.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(20, results.getNumberOfMappingSets());
        report("testFinder2 Done");
	}

    @Test
 	public void testFinder3() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder3");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleBToC.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleEToD.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleCToD.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(20, results.getNumberOfMappingSets());
        report("testFinder3Done");
	}

    @Test
 	public void testFinder4() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder4");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleBToC.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleEToD.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleEToF.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleCToD.ttl", StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(30, results.getNumberOfMappingSets());
        report("testFinder4Done");
	}

    @Test
 	public void testFinder5() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder5");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToA.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(8, results.getNumberOfMappingSets());
        report("testFinder5Done");
	}

    @Test
    public void testFinder6() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder6");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToA.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToC.ttl", StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(16, results.getNumberOfMappingSets());
        report("testFinder6Done");
	}

    @Test
 	public void testFinder7() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder7");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToA.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleBToC.ttl", StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(16, results.getNumberOfMappingSets()); //14 is correct as coded but should we do C -> C'?
        report("testFinder7Done");
	}
    
    @Test
 	public void testFinder8() throws BridgeDBException, RDFHandlerException, IOException {	
        report("testFinder8");
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToB.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToA.ttl", StoreType.TEST);
        linksetLoader.load("../org.bridgedb.tools.transitive/test-data/sampleAToA_1.ttl", StoreType.TEST);
        TransativeFinder transativeFinder = new TransativeFinder(StoreType.TEST);
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(14, results.getNumberOfMappingSets());
        report("testFinder8Done");
	}

}
