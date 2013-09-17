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
package org.bridgedb;

import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * Base which sets up the static variables used by Tests such as IDMapperTest and IDMapperCapabilitiesTest
 * @author Christian
 */
@Ignore
public abstract class IDMapperTestBase {
	
    protected static String dataSource1Code;
    protected static String dataSource2Code;
    protected static String dataSource3Code;
    //DataSource that MUST be supported.
    protected static DataSource DataSource1;
    protected static DataSource DataSource2;
    protected static DataSource DataSource3;
    //This DataSource MUST not be supported
    protected static DataSource DataSourceBad;
      
    protected static String ds1Id1;
    protected static String ds2Id1;
    protected static String ds3Id1;
    protected static String ds2Id2;
    protected static String ds2Id3;
    //Set of Xrefs that are expected to map together.
    protected static Xref map1xref1;
    protected static Xref map1xref2;
    protected static Xref map1xref3;
    //Second set of Xrefs that are expected to map together.
    protected static Xref map2xref1;
    protected static Xref map2xref2;
    protected static Xref map2xref3;
    //Third Set of Xref which again should map to each other but not the above
    protected static Xref map3xref1;
    protected static Xref map3xref2;
    protected static Xref map3xref3;
    //Add an id that does not exist and can not be used in freesearch
    //Or null if all Strings can be used.
    protected static String badID;
    //And a few Xrefs also not used
    protected static Xref mapBadxref1;
    protected static Xref mapBadxref2;
    protected static Xref mapBadxref3;
    //Add some half null xrefs
    protected static Xref HALFNULL1;
	protected static Xref HALFNULL2;
    //Add a property key that will not be found
    protected static String badKey;
    
    @BeforeClass

    /**
     * Method to set up the variables.
     * 
     * Should be overrided to change all of the variables.
     * To change some over write it. Call super.setupVariables() and then change the few that need fixing.
     * <p>
     * Note: According to the Junit api 
     * "The @BeforeClass methods of superclasses will be run before those the current class."
     */
    public static void setupXref() throws IDMapperException{
        //If the actual source to be tested does not contain these please overwrite with ones that do exist.
        dataSource1Code = "ConceptWiki";
        DataSource1 = DataSource.register(dataSource1Code, "ConceptWiki"). urlPattern("http://www.conceptwiki.org/concept/$id")
                .idExample("33a28bb2-35ed-4d94-adfd-3c96053cbaaf").asDataSource();
        dataSource2Code = "Cs";
        DataSource2 = DataSource.register(dataSource2Code, "Chemspider").urlPattern("http://www.chemspider.com/Chemical-Structure.$id.html")
                .idExample("56586").asDataSource();
        dataSource3Code = "ChemblMolecule";
        DataSource3 = DataSource.register(dataSource3Code, "Chembl Molecule").urlPattern("http://data.kasabi.com/dataset/chembl-rdf/molecule/m$id")
                .idExample("1").asDataSource();
        //This DataSource MUST not be supported
        DataSourceBad = DataSource.register("TestDSBad", "TestDSBad")
                .urlPattern("www.NotInTheURlMapper.com#$id").asDataSource();

        //Set of Xrefs that are expected to map together.
        //Note: Ids intentionally equals for testing of DataCollection
        ds1Id1 = "38932552-111f-4a4e-a46a-4ed1d7bdf9d5";
        map1xref1 = new Xref(ds1Id1, DataSource1);
        ds2Id1 = "187440";
        map1xref2 = new Xref(ds2Id1, DataSource2);
        ds3Id1 = "276734";
        map1xref3 = new Xref(ds3Id1, DataSource3);
        //Second set of Xrefs that are expected to map together.
        //But these are not expected NOT to map to the first set
        map2xref1 = new Xref("bd42675d-9966-48f5-b42e-f6a0c5ec6794", DataSource1);
        ds2Id2 = "18";
        map2xref2 = new Xref(ds2Id2, DataSource2);
        map2xref3 = new Xref("370186", DataSource3);
        //Third Set of Xref which again should map to eachothe but not the above
        map3xref1 = new Xref("9d4a6a08-6757-4ff2-98c1-e3c8b3e095cc", DataSource1);
        ds2Id3 = "50";
        map3xref2 = new Xref(ds2Id3 , DataSource2);
        map3xref3 = new Xref("520018", DataSource3);
        //Add an id that does not exist and can not be used in freesearch
        //Or null if all Strings can be used.
        badID = "ThisIdIsNotinTheSystem";
        //And a few Xrefs also not used
        mapBadxref1 = new Xref("123", DataSourceBad);
        mapBadxref2 = new Xref(badID, DataSource2);
        mapBadxref3 = new Xref("789", DataSourceBad);        
        //Add some half null xrefs
        HALFNULL1 = new Xref("123", null);
        HALFNULL2 = new Xref(null, DataSource1);
        //Add a property key that will not be found
        badKey = "NoT A ProPertY keY";
    }
    
    //allows how all tests output to be changed at the same time.
    public void report(String message){
        System.out.println(message);
    }
}
