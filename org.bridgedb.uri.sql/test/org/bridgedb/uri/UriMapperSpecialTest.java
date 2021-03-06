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
package org.bridgedb.uri;

import java.util.List;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.sql.SQLListener;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.utils.BridgeDBException;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the UriMapper interface (and by loading the UriListener interface)
 *
 * Should be passable by any implementation of UriMapper that has the test data loaded.
 * 
 * @author Christian
 */
public abstract class UriMapperSpecialTest extends UriListenerTest{
           
    private static  final String NULL_GRAPH = null;

    @Test 
    public void testMapIDOneBad() throws BridgeDBException{
        report("MapIDOneBad");
        Set<String> results = uriMapper.mapUri(mapBadUri1, Lens.getDefaultLens(), NULL_GRAPH);
        //According to Martijn and the OPS needs mappers should return the incoming URI where appropiate.
        //Still optional as I am not sure text does.
        //Not all mappers will have the pattern matching to notice this is an invalid URI
        assertTrue(results.size() <= 1);
    }

    @Test 
    public void testMapFullOneBad() throws BridgeDBException{
        report("MapFullOneBad");
        Set<Mapping> results = uriMapper.mapFull(mapBadUri1, Lens.getDefaultLens());
        assertTrue(results.size() <= 1);
    }

    @Test 
    public void testMapFullOneBadOneNameSpace() throws BridgeDBException{
        report("MapFullOneBadOneNameSpace");
        Set<Mapping> results = uriMapper.mapFull(mapBadUri1, Lens.getDefaultLens(), NULL_GRAPH, uriPattern2);
        assertTrue(results.size() <= 1);
    }

    @Test
    public void testMapNoneExistingDataSource() throws BridgeDBException{
        report("MapNoneExistingDataSource");
        Set<String> results = uriMapper.mapUri(map1Uri2, Lens.getDefaultLens(), NULL_GRAPH, uriPatternBad);
        assertEquals(0,results.size());
    }

    @Test
    public void testUriSupported() throws Exception {
        report("UriSupported");
        assertTrue(uriMapper.uriExists(map1Uri1));
        assertTrue(uriMapper.uriExists(map1Uri2));
        assertTrue(uriMapper.uriExists(map1Uri3));
        assertTrue(uriMapper.uriExists(map2Uri1));
        assertTrue(uriMapper.uriExists(map2Uri2));
        assertTrue(uriMapper.uriExists(map2Uri3));
        assertTrue(uriMapper.uriExists(map3Uri1));
        assertTrue(uriMapper.uriExists(map3Uri2));
        assertTrue(uriMapper.uriExists(map3Uri3));
        assertFalse(uriMapper.uriExists(mapBadUri1));
        assertFalse(uriMapper.uriExists(mapBadUri2));
        assertFalse(uriMapper.uriExists(mapBadUri3));
    }
        
    @Test
    public void testFreeSearchBad() throws BridgeDBException{
        org.junit.Assume.assumeTrue(uriMapper.getCapabilities().isFreeSearchSupported());       
        org.junit.Assume.assumeTrue(badID != null);
        report("FreeSearchBad");
        Set<String> results = uriMapper.uriSearch(badID, 10);
        assertTrue (results == null || results.isEmpty());
    }
    
    @Test
    public void testFreeSearchGood() throws BridgeDBException{
        org.junit.Assume.assumeTrue(uriMapper.getCapabilities().isFreeSearchSupported());       
        report("FreeSearchGood");
        Set<String> results = uriMapper.uriSearch(ds2Id3, 10);
        //Skip these if there are 10 or more possible ones. No Gurantee whiuch come back
        if (results.size() < 10){
            assertTrue (results.contains(map3Uri2));
            assertTrue (results.contains(map3Uri2a));
        }
        assertFalse (results.contains(map3Uri1));
        assertFalse (results.contains(map1Uri2));
    }
    
    @Test
    public void testFreeSearchGoodJust2() throws BridgeDBException{
        org.junit.Assume.assumeTrue(uriMapper.getCapabilities().isFreeSearchSupported());       
        report("FreeSearchGoodJust2");
        Set<String> results = uriMapper.uriSearch(ds2Id2, 2);
        assertEquals (2, results.size());
     }

    @Test
    public void testGetXrefGood() throws BridgeDBException {
        report("GetXrefGood");
        Xref result = uriMapper.toXref(map2Uri2);
        assertEquals(map2xref2, result);
    }

    @Test
    public void testGetOverallStatistics() throws BridgeDBException {
        report("GetOverallStatistics()");
        OverallStatistics results = uriMapper.getOverallStatistics(Lens.getDefaultLens());
        System.out.println(results);
        assertThat (results.getNumberOfMappings(), greaterThanOrEqualTo(18));
        assertThat (results.getNumberOfMappingSets(), greaterThanOrEqualTo(6));
        assertThat (results.getNumberOfSourceDataSources(), greaterThanOrEqualTo(3));
        assertThat (results.getNumberOfTargetDataSources(), greaterThanOrEqualTo(3));
        assertThat (results.getNumberOfPredicates(), greaterThanOrEqualTo(1));
    }

    @Test
    public void testGetMappingSetInfo() throws BridgeDBException {
        report("GetMappingSetInfo");
        MappingSetInfo result = uriMapper.getMappingSetInfo(mappingSet2_3);
        assertEquals(DataSource2.getSystemCode(), result.getSource().getSysCode());
        assertEquals(DataSource3.getSystemCode(), result.getTarget().getSysCode());
        assertFalse(result.isSymmetric());
        result = uriMapper.getMappingSetInfo(mappingSet2_3+1);
        assertEquals(DataSource3.getSystemCode(), result.getSource().getSysCode());
        assertEquals(DataSource2.getSystemCode(), result.getTarget().getSysCode());
        assertTrue(result.isSymmetric());
    }

    @Test
    public void testGetMappingSetInfos() throws BridgeDBException {
        report("GetMappingSetInfo All");
        List<MappingSetInfo> results = uriMapper.getMappingSetInfos(null, null, Lens.getDefaultLens());
        assertThat (results.size(), greaterThanOrEqualTo(6));
    }

    @Test
    public void testGetMappingSetInfosBySourceAndTarget() throws BridgeDBException {
        report("GetMappingSetInfos source and target");
        List<MappingSetInfo> results = 
                uriMapper.getMappingSetInfos(DataSource2.getSystemCode(), DataSource1.getSystemCode(), Lens.getDefaultLens());
        assertThat (results.size(), greaterThanOrEqualTo(1));
        for (MappingSetInfo info:results){
            assertEquals(DataSource2.getSystemCode(), info.getSource().getSysCode());
            assertEquals(DataSource1.getSystemCode(), info.getTarget().getSysCode());
        }
    }

    @Test
    public void testGetMappingSetInfosByTarget() throws BridgeDBException {
        report("GetMappingSetInfos target");
        List<MappingSetInfo> results = 
                uriMapper.getMappingSetInfos(null, DataSource3.getSystemCode(), Lens.getDefaultLens());
        assertThat (results.size(), greaterThanOrEqualTo(2));
        for (MappingSetInfo info:results){
            assertEquals(DataSource3.getSystemCode(), info.getTarget().getSysCode());
        }
    }
    @Test
    public void testGetMappingSetInfosBySource() throws BridgeDBException {
        report("GetMappingSetInfos source");
        List<MappingSetInfo> results = 
                uriMapper.getMappingSetInfos(DataSource1.getSystemCode(), null, Lens.getDefaultLens());
        assertThat (results.size(), greaterThanOrEqualTo(2));
        for (MappingSetInfo info:results){
            assertEquals(DataSource1.getSystemCode(), info.getSource().getSysCode());
        }
    }
    
    @Test
    public void testGetUriSpaces() throws BridgeDBException {
        report("GetUriSpaces");
        Set<String> results = uriMapper.getUriPatterns(map2xref3.getDataSource().getSystemCode());
        assertTrue (results.contains(uriPattern3.toString()));
    }
    
    @Test
    public void testGetSqlCompatVersion() throws BridgeDBException {
        report("GetSqlCompatVersion");
        int result = uriMapper.getSqlCompatVersion();
        assertEquals(SQLListener.SQL_COMPAT_VERSION, result);
    }
    
    @Test
    public void testCheckUriPatterns() throws Exception {
        //Date start = new Date();
        report("getCheckUriPatterns");
        for (UriPattern pattern:UriPattern.getUriPatterns()){
            String uri = pattern.getPrefix() + "1234" + pattern.getPostfix();
            DataSource dataSource = pattern.getDataSource();
            if (dataSource.getSystemCode() != null && !uri.startsWith("http://www.example.com/")){
                Xref xref = uriMapper.toXref(uri);
                assertEquals(pattern.getDataSource(), xref.getDataSource());
            }
        }
        //Date end = new Date();
        //ystem.out.println(end.getTime()-start.getTime());
     }
    
    @Test
    public void testGetSampleMappings() throws Exception {
        //Date start = new Date();
        report("GetSampleMappings");
        if (uriMapper instanceof SQLUriMapper){
            List<Mapping> mappings = uriMapper.getSampleMapping();
        }
        //Date end = new Date();
        //ystem.out.println(end.getTime()-start.getTime());
        
    }
    
 /*       @Test
    public void testFrequency() throws IDMapperException{
        report("Frequency");
        String TEST_JUSTIFICATION1 = "http://www.bridgedb.org/test#testJustification1";
        DataSource DataSourceA = DataSource.register("testFrequencyA", "testFrequencyA").asDataSource();
        DataSource DataSourceB = DataSource.register("testFrequencyB", "testFrequencyB").asDataSource();
        SQLUriMapper sqlUriMapper = (SQLUriMapper)uriMapper;
        int mappingSet = sqlUriMapper.registerMappingSet(DataSourceA, TEST_PREDICATE, TEST_JUSTIFICATION1, "testFrequency()", DataSourceB, 
                SYMETRIC, NO_VIA, NO_CHAIN);
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
 */     

}
