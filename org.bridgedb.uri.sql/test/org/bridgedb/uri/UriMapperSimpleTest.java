/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.uri;

import java.util.HashSet;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.rdf.UriPattern;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openrdf.model.Statement;

/**
 *
 * @author Christian
 */
public abstract class UriMapperSimpleTest extends UriListenerTest{

    private static  final String NULL_GRAPH = null;

    /**
     * Test of mapID method, of class UriMapper.
     */
    @Test
    public void testMapID_sourceXref_lensId_tgtDataSources() throws Exception {
        report("MapID_sourceXref_lensId_tgtDataSources");
        Xref sourceXref = map2xref2;
        String lensId = Lens.getDefaultLens();
        Set<Xref> results = uriMapper.mapID(sourceXref, lensId, DataSource2, DataSource3);
        assertFalse(results.contains(map2xref1));
        assertTrue(results.contains(map2xref2));
        assertTrue(results.contains(map2xref3));
        assertFalse(results.contains(map1xref2));
        assertFalse(results.contains(map1xref1));
        assertFalse(results.contains(map3xref2));
        checkForNoOtherLensXrefs(results);
    }

    /**
     * Test of mapID method, of class UriMapper.
     */
    @Test
    public void testMapID_sourceXref_lensId_tgtDataSource() throws Exception {
        report("MapID_sourceXref_lensId_tgtDataSource");
        Xref sourceXref = map2xref2;
        String lensId = Lens.getDefaultLens();
        DataSource tgtDataSource = DataSource3;
        Set results = uriMapper.mapID(sourceXref, lensId, tgtDataSource);
        assertFalse(results.contains(map2xref1));
        assertFalse(results.contains(map2xref2));
        assertTrue(results.contains(map2xref3));
        assertFalse(results.contains(map1xref2));
        assertFalse(results.contains(map1xref1));
        assertFalse(results.contains(map3xref2));
        checkForNoOtherLensXrefs(results);
    }

    /**
     * Test of mapID method, of class UriMapper.
     */
    @Test
    public void testMapID_sourceXref_lensId() throws Exception {
        report("MapID_sourceXref_lensId");
        Xref sourceXref = map2xref2;
        String lensId = Lens.getDefaultLens();
        Set results = uriMapper.mapID(sourceXref, lensId);
        assertTrue(results.contains(map2xref1));
        assertTrue(results.contains(map2xref2));
        assertTrue(results.contains(map2xref3));
        assertFalse(results.contains(map1xref2));
        assertFalse(results.contains(map1xref1));
        assertFalse(results.contains(map3xref2));
        checkForNoOtherLensXrefs(results);
    }
    
    

    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapUri_sourceUri_lensId_tgtUriPatterns() throws Exception {
        report("MapUri_sourceUri_lensId_tgtUriPatterns");
        String sourceUri = map3Uri3;
        String lensId = Lens.getDefaultLens();
        Set results = uriMapper.mapUri(sourceUri, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        assertFalse(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapBySet_sourceUri_lensId_tgtUriPatterns() throws Exception {
        report("MapUri_sourceUri_lensId_tgtUriPatterns");
        String sourceUri = map3Uri3;
        String lensId = Lens.getDefaultLens();
        MappingsBySet lensMapping = uriMapper.mapBySet(sourceUri, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        Set<String> results = lensMapping.getTargetUris();
        assertFalse(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    @Test
    public void testMapBySet_sourceUris_lensId_tgtUriPatterns() throws Exception {
        report("MapUri_sourceUris_lensId_tgtUriPatterns");
        Set<String> sourceUris = new HashSet<String>();
        sourceUris.add(map3Uri3);
        sourceUris.add(map1Uri1);
        String lensId = Lens.getDefaultLens();
        MappingsBySet lensMapping = uriMapper.mapBySet(sourceUris, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        Set<String> results = lensMapping.getTargetUris();
        assertFalse(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertTrue(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }
    
    @Test
    public void testMapBySet_sourceUrisB_lensId_tgtUriPatterns() throws Exception {
        report("MapUri_sourceUrisB_lensId_tgtUriPatterns");
        Set<String> sourceUris = new HashSet<String>();
        sourceUris.add(map3Uri3);
        sourceUris.add(map3Uri1);
        String lensId = Lens.getDefaultLens();
        MappingsBySet lensMapping = uriMapper.mapBySet(sourceUris, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        Set<String> results = lensMapping.getTargetUris();
        assertFalse(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapUri_sourceXref_lensId_tgtUriPattern() throws Exception {
        report("MapUri_sourceXref_lensId_tgtUriPattern");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        UriPattern tgtUriPattern = uriPattern3;
        Set results = uriMapper.mapUri(sourceXref, lensId, NULL_GRAPH, tgtUriPattern);
        assertFalse(results.contains(map3Uri1));
        assertFalse(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapUri_sourceXref_lensId() throws Exception {
        report("MapUri_sourceXref_lensId");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        Set results = uriMapper.mapUri(sourceXref, lensId, NULL_GRAPH);
        assertTrue(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertTrue(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapUri_sourceXref_lensId_tgtUriPatterns() throws Exception {
        report("MapUri_sourceXref_lensId_tgtUriPatterns");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        Set results = uriMapper.mapUri(sourceXref, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        assertFalse(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapUri_sourceUri_lensId_tgtUriPattern() throws Exception {
        report("MapUri_sourceUri_lensId_tgtUriPattern");
        String sourceUri = map3Uri2;
        String lensId = Lens.getDefaultLens();
        UriPattern tgtUriPattern = uriPattern3;
        Set results = uriMapper.mapUri(sourceUri, lensId, NULL_GRAPH, tgtUriPattern);
        assertFalse(results.contains(map3Uri1));
        assertFalse(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    @Test
    public void testMapBySet_sourceUri_lensId_tgtUriPattern() throws Exception {
        report("MapBySet_sourceUri_lensId_tgtUriPattern");
        String sourceUri = map3Uri2;
        String lensId = Lens.getDefaultLens();
        UriPattern tgtUriPattern = uriPattern3;
        MappingsBySet lensMapping = uriMapper.mapBySet(sourceUri, lensId, tgtUriPattern);
        Set<String> results = lensMapping.getTargetUris();
        assertFalse(results.contains(map3Uri1));
        assertFalse(results.contains(map3Uri2));
        assertFalse(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }
    
    /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapUri_sourceUri_lensId() throws Exception {
        report("MapUri_sourceUri_lensId");
        String sourceUri = map3Uri2;
        String lensId = Lens.getDefaultLens();
        Set results = uriMapper.mapUri(sourceUri, lensId, NULL_GRAPH);
        assertTrue(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertTrue(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

   /**
     * Test of mapUri method, of class UriMapper.
     */
    @Test
    public void testMapBySet_sourceUri_lensId() throws Exception {
        report("MapBySeti_sourceUri_lensId");
        String sourceUri = map3Uri2;
        String lensId = Lens.getDefaultLens();
        MappingsBySet lensMapping = uriMapper.mapBySet(sourceUri, lensId);
        Set<String> results = lensMapping.getTargetUris();
        assertTrue(results.contains(map3Uri1));
        assertTrue(results.contains(map3Uri2));
        assertTrue(results.contains(map3Uri2a));
        assertTrue(results.contains(map3Uri3));
        assertFalse(results.contains(map2Uri2));
        assertFalse(results.contains(map1Uri3));
        checkForNoOtherlensId(results);
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceXref_lensId_tgtDataSources() throws Exception {
        report("MapFull_sourceXref_lensId_tgtDataSources");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        DataSource[] tgtDataSources = null;
        Set<Mapping> results = uriMapper.mapFull(sourceXref, lensId, DataSource2, DataSource3);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertTrue(targetUris.contains(map3Uri2));
        assertTrue(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertTrue(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceXref_lensId_tgtDataSource() throws Exception {
        report("MapFull_sourceXref_lensId_tgtDataSources");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        DataSource tgtDataSource = DataSource3;
        Set<Mapping> results = uriMapper.mapFull(sourceXref, lensId, tgtDataSource);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
            assertEquals(tgtDataSource, mapping.getTarget().getDataSource());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertFalse(targetUris.contains(map3Uri2));
        assertFalse(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertFalse(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceXref_lensId() throws Exception {
        report("MapFull_sourceXref_lensId_tgtDataSources");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        Set<Mapping> results = uriMapper.mapFull(sourceXref, lensId);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertTrue(targetUris.contains(map3Uri1));
        assertTrue(targetUris.contains(map3Uri2));
        assertTrue(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertTrue(targetXrefs.contains(map3xref1));
        assertTrue(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceXref_lensId_tgtUriPatterns() throws Exception {
        report("MapFull_sourceXref_lensId_tgtUriPatterns");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        UriPattern[] tgtUriPatterns = null;
        Set<Mapping> results = uriMapper.mapFull(sourceXref, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertTrue(targetUris.contains(map3Uri2));
        assertFalse(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertTrue(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceXref_lensId_tgtUriPattern() throws Exception {
        report("MapFull_sourceXref_lensId_tgtUriPattern");
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        UriPattern tgtUriPattern = uriPattern3;
        Set<Mapping> results = uriMapper.mapFull(sourceXref, lensId, NULL_GRAPH, tgtUriPattern);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertFalse(targetUris.contains(map3Uri2));
        assertFalse(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertFalse(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceUri_lensId_tgtDataSources() throws Exception {
        report("MapFull_sourceUri_lensId_tgtDataSources");
        String sourceUri = map3Uri2;
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        DataSource[] tgtDataSources = null;
        Set expResult = null;
        Set<Mapping> results = uriMapper.mapFull(sourceUri, lensId, DataSource2, DataSource3);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            assertTrue(mapping.getSourceUri().contains(sourceUri));
            assertTrue(mapping.getSourceUri().size() == 1);
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertTrue(targetUris.contains(map3Uri2));
        assertTrue(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertTrue(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceUri_lensId_tgtDataSource() throws Exception {
        report("MapFull_sourceUri_lensId_tgtDataSource");
        String sourceUri = map3Uri2;
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        DataSource tgtDataSource = DataSource3;
        Set<Mapping> results = uriMapper.mapFull(sourceUri, lensId, tgtDataSource);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            assertTrue(mapping.getSourceUri().contains(sourceUri));
            assertTrue(mapping.getSourceUri().size() == 1);
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertFalse(targetUris.contains(map3Uri2));
        assertFalse(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertFalse(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_MapFull_sourceUri_lensId() throws Exception {
        report("MapFull_sourceUri_lensId");
        String sourceUri = map3Uri2;
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        Set<Mapping> results = uriMapper.mapFull(sourceUri, lensId);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            assertTrue(mapping.getSourceUri().contains(sourceUri));
            assertTrue(mapping.getSourceUri().size() == 1);
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertTrue(targetUris.contains(map3Uri1));
        assertTrue(targetUris.contains(map3Uri2));
        assertTrue(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertTrue(targetXrefs.contains(map3xref1));
        assertTrue(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceUri_lensId_tgtUriPattern() throws Exception {
        report("MapFull_sourceUri_lensId_tgtUriPattern");
        String sourceUri = map3Uri2;
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        UriPattern tgtUriPattern = uriPattern3;
        Set<Mapping> results = uriMapper.mapFull(sourceUri, lensId, tgtUriPattern);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            assertTrue(mapping.getSourceUri().contains(sourceUri));
            assertTrue(mapping.getSourceUri().size() == 1);
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertFalse(targetUris.contains(map3Uri2));
        assertFalse(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);
        
        assertFalse(targetXrefs.contains(map3xref1));
        assertFalse(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

    /**
     * Test of mapFull method, of class UriMapper.
     */
    @Test
    public void testMapFull_sourceUri_lensId_tgtUriPatterns() throws Exception {
        report("MapFull_sourceUri_lensId_tgtUriPatterns");
        String sourceUri = map3Uri2;
        Xref sourceXref = map3xref2;
        String lensId = Lens.getDefaultLens();
        UriPattern[] tgtUriPatterns = null;
        Set<Mapping> results = uriMapper.mapFull(sourceUri, lensId, NULL_GRAPH, uriPattern2, uriPattern3);
        Set<String> targetUris = new HashSet<String>();
        Set<Xref> targetXrefs = new HashSet<Xref>();
        for (Mapping mapping:results){
            assertEquals(sourceXref, mapping.getSource());
            assertTrue(mapping.getSourceUri().contains(sourceUri));
            assertTrue(mapping.getSourceUri().size() == 1);
            if (!mapping.getTarget().equals(sourceXref)){
                assertThat(mapping.getPredicate(), not(equalTo(null)));            
                assertThat(mapping.getMappingSetId(), not(equalTo(null)));
            }
            targetUris.addAll(mapping.getTargetUri());
            targetXrefs.add(mapping.getTarget());
        }
        assertFalse(targetUris.contains(map3Uri1));
        assertTrue(targetUris.contains(map3Uri2));
        assertFalse(targetUris.contains(map3Uri2a));
        assertTrue(targetUris.contains(map3Uri3));
        assertFalse(targetUris.contains(map2Uri2));
        assertFalse(targetUris.contains(map1Uri3));
        checkForNoOtherlensId(targetUris);

        assertFalse(targetXrefs.contains(map3xref1));
        assertTrue(targetXrefs.contains(map3xref2));
        assertTrue(targetXrefs.contains(map3xref3));
        assertFalse(targetXrefs.contains(map1xref2));
        assertFalse(targetXrefs.contains(map1xref1));
        assertFalse(targetXrefs.contains(map2xref2));   
    }

}
