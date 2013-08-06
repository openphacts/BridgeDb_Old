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
package org.bridgedb.uri.loader.transative;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bridgedb.uri.loader.transative.constant.ChemInf;
import org.bridgedb.uri.loader.transative.constant.OboConstants;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class JustificationMaker {

    public static Set<String> PARENT_CHILD = new HashSet<String>(Arrays.asList(
            ChemInf.hasStereoundefinedParent, 
            ChemInf.hasOPSNormalizedCounterpart, 
            ChemInf.hasIsotopicallyUnspecifiedParent,
            ChemInf.hasUnchargedCounterpart,
            ChemInf.hasComponentWithUnchargedCounterpart,
            ChemInf.hasMajorTautomerAtpH7point4,
            OboConstants.HAS_PART,
            OboConstants.IS_TAUTOMER_OF,
            OboConstants.HAS_FUNCTIONAL_PARENT
            ));

    public static String combine(String left, String right) throws BridgeDBException{
        String result = possibleCombine(left, right);
        if (result != null){
            return result;
        }
        throw new BridgeDBException("unable to combine " + left + " with " + right);
    }
    
    public static String possibleCombine(String left, String right) {
        if (left.equals(right)){
            return left;
        }
        if (left.equals(ChemInf.INCHI_KEY)) {
            if (right.equals(ChemInf.CHEMICAL_ENTITY)) {
                return ChemInf.CHEMICAL_ENTITY;
            }
            if (PARENT_CHILD.contains(right)){
                return right;
            }
        }
        if (left.equals(ChemInf.CHEMICAL_ENTITY)) {
            if (right.equals(ChemInf.INCHI_KEY)) {
                return ChemInf.CHEMICAL_ENTITY;
            }
            if (PARENT_CHILD.contains(right)){
                return right;
            }
        }
        if (PARENT_CHILD.contains(left)) {
         	if (right.equals(ChemInf.INCHI_KEY)) return left;
            if (right.equals(ChemInf.CHEMICAL_ENTITY)) return left;
        }
        return null;
    }
}
