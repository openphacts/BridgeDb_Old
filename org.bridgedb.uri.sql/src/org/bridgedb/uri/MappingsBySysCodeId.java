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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bridgedb.Xref;
import org.bridgedb.pairs.IdSysCodePair;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class MappingsBySysCodeId {
    
    private Map<String,Map<String, Set<String>>> mappings = new HashMap<String,Map<String, Set<String>>>();

    public final void addMapping(IdSysCodePair pair, String uri) {
        Map<String, Set<String>> byCode = mappings.get(pair.getSysCode());
        if (byCode == null){
            byCode = new HashMap<String, Set<String>>();
        }
        Set<String> byId = byCode.get(pair.getId());
        if (byId == null){
            byId = new HashSet<String>();
        }
        byId.add(uri);
        byCode.put(pair.getId(), byId);
        mappings.put(pair.getSysCode(), byCode);
    }

    public final void addMappings(IdSysCodePair pair, Set<String> URIs) {
        addMappings(pair.getSysCode(), pair.getId(), URIs);
    }
    
    public final void addMappings(String sysCode, String id, Set<String> URIs) {
        Map<String, Set<String>> byCode = mappings.get(sysCode);
        if (byCode == null){
            byCode = new HashMap<String, Set<String>>();
        }
        Set<String> byId = byCode.get(id);
        if (byId == null){
            byId = new HashSet<String>();
        }
        byId.addAll(URIs);
        byCode.put(id, byId);
        mappings.put(sysCode, byCode);
    }
    
    public final void addMappings(Xref xref, Set<String> URIs) {
        addMappings(xref.getId(), xref.getDataSource().getSystemCode(), URIs);
    }
    
    public Set<String> getSysCodes(){
        return mappings.keySet();
    }

    Set<String> getIds(String sysCode) throws BridgeDBException {
        Map<String, Set<String>> byCode = mappings.get(sysCode);
        if (byCode == null){
            throw new BridgeDBException ("No mappings known for sysCode " + sysCode);
        }
        return byCode.keySet();
    }

    Set<String> getUri(String sysCode, String id) throws BridgeDBException {
        Map<String, Set<String>> byCode = mappings.get(sysCode);
        if (byCode == null){
            throw new BridgeDBException ("No mappings known for sysCode " + sysCode);
        }
        Set<String> byId = byCode.get(id);
        if (byId == null){
            throw new BridgeDBException ("No mappings known for sysCode " + sysCode + " and id " + id);
        }
        return byId;
    }
}
