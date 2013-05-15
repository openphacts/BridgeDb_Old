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
package org.bridgedb.linkset.transative;

import org.bridgedb.linkset.ChemInf;
import org.bridgedb.linkset.OboConstants;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author Christian
 */
public class JustificationMaker {

    public static Value combine(Value left, Value right) throws RDFHandlerException{
        String result = possibleCombine(left.stringValue(), right.stringValue());
        if (result != null){
            if (result.equals(left.stringValue())){
                return left;
            }
            if (result.equals(right.stringValue())){
                return right;
            }
            return new URIImpl(result);
        }
        throw new RDFHandlerException("unable to combine " + left + " with " + right);
    }
    
    public static String possibleCombine(String left, String right) {
        if (left.equals(right)){
            return left;
        }
        if (left.equals(ChemInf.INCHI_KEY)) {
            if (right.startsWith(OboConstants.PREFIX)) return right;
        }
        if (left.startsWith(OboConstants.PREFIX)) {
         	if (right.equals(ChemInf.INCHI_KEY)) return left;
            if (right.startsWith(OboConstants.PREFIX)) return OboConstants.PREFIX + "combined";
        }
        return null;
    }
}
