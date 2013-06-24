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

import java.util.HashSet;
import java.util.Set;

/**
 * Holder class for the main Meta Data of MappingSet.
 *
 * Does not include everything in the void header but only what is captured in the SQL.
 * @author Christian
 */
public class LensMapping {
    private final String lens;
    private final Set<MappingInfo> mappingInfos;
    /*
     * These are the direct mappings based on namespace substitution
     */
    private final Set<UriMapping> mappings;
    
    public LensMapping(String lens){
        this.lens = lens;
        this.mappingInfos = new HashSet<MappingInfo>();
        this.mappings = new HashSet<UriMapping>();
    }
    
    public void addMapping (int mappingSetId, String predicate, String justification, String mappingSource, 
            String sourceUri, Set<String> targetUris){
        MappingInfo info = infoById(mappingSetId);
        if (info == null){
            info = new MappingInfo(mappingSetId, predicate, justification, mappingSource);
            mappingInfos.add(info);
        }
        for (String targetUri: targetUris){
            info.addMapping(new UriMapping(sourceUri, targetUri));
        }
    }

    public final void addMapping (String sourceUri, String targetUri){
        mappings.add(new UriMapping(sourceUri, targetUri));
    }
    
    public void addMapping (String sourceUri, Set<String> targetUris){
       for (String targetUri:targetUris){
           addMapping(sourceUri, targetUri);
       }
    }

    private MappingInfo infoById(int id) {
        for (MappingInfo info: mappingInfos){
            if (info.getId() == id){
                return info;
            }
        }
        return null;
    }
    
    public Set<String> getTargetUris(){
        HashSet<String> targetUris = new HashSet<String>();
        for (MappingInfo info: mappingInfos){
            targetUris.addAll(info.getTargetUris());           
        }
        for (UriMapping mapping:mappings){
            targetUris.add(mapping.getTargetUri());
        }

        return targetUris;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("Lens: ");
        sb.append(lens);
        for (MappingInfo info: mappingInfos){
            info.append(sb);           
        }
        sb.append("\n\tUriSpace based mappings");
        for (UriMapping mapping:mappings){
            mapping.append(sb);
        }
        return sb.toString();
    }
}
