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

import org.bridgedb.rdf.RdfConfig;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * This is just a Utils class to provide any default lens as well as a single Javadocs point.
 * <p>
 * 
 * @author Christian
 */
public class LensHelper1 {
    
    public static String METHOD_MIDDLE = "lens";
    public static String ID_PREFIX = "l";
    
    public static String getLensBaseURI() throws BridgeDBException{
        return RdfConfig.getTheBaseURI() + METHOD_MIDDLE + "/" ;  
    }

    public static String getLensId(int lensId) throws BridgeDBException{
        return ID_PREFIX + lensId;
    }
    
    public static String getLensURI(String lensId) throws BridgeDBException{
        return getLensBaseURI() + lensId;  
    }
  
    public static String getDefaultJustifictaionString() throws BridgeDBException{
       return "http://www.w3.org/2000/01/rdf-schema#isDefinedBy"; 
    }
    
    public static URI[] getDefaultJustifictaions() throws BridgeDBException{
        URI[] result = new URI[3];
        result[0] = new URIImpl(getDefaultJustifictaionString());
        result[1] = new URIImpl("http://semanticscience.org/resource/CHEMINF_000059");
        result[2] = new URIImpl("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Accession_Number");
        return result;
    }

    public static String getTestJustifictaion() throws BridgeDBException{
        return "http://www.bridgedb.org/test#testJustification";
    }

}
