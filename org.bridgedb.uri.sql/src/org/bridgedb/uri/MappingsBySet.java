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
public class MappingsBySet {
    private final String lens;
    private final Set<SetMappings> setMappings;
    /*
     * These are the direct mappings based on namespace substitution
     */
    private final Set<UriMapping> mappings;
    
    public MappingsBySet(String lens){
        this.lens = lens;
        this.setMappings = new HashSet<SetMappings>();
        this.mappings = new HashSet<UriMapping>();
    }
    
    public void addMapping (int mappingSetId, String predicate, String justification, String mappingSource, 
            String sourceUri, Set<String> targetUris){
        SetMappings setMapping = setMappingById(mappingSetId);
        if (setMapping == null){
            setMapping = new SetMappings(mappingSetId, predicate, justification, mappingSource);
            setMappings.add(setMapping);
        }
        for (String targetUri: targetUris){
            setMapping.addMapping(new UriMapping(sourceUri, targetUri));
        }
    }

    public void addSetMapping(SetMappings setMapping){
        setMappings.add(setMapping);
    }
    
    public void addMapping (int mappingSetId, String predicate, String justification, String mappingSource, 
            String sourceUri, String targetUri){
        SetMappings setMapping = setMappingById(mappingSetId);
        if (setMapping == null){
            setMapping = new SetMappings(mappingSetId, predicate, justification, mappingSource);
            getSetMappings().add(setMapping);
        }
        setMapping.addMapping(new UriMapping(sourceUri, targetUri));
    }
    
    public final void addMapping (String sourceUri, String targetUri){
        mappings.add(new UriMapping(sourceUri, targetUri));
    }
    
    public final void addMapping (UriMapping uriMapping){
        mappings.add(uriMapping);
    }
     public void addMapping (String sourceUri, Set<String> targetUris){
       for (String targetUri:targetUris){
           addMapping(sourceUri, targetUri);
       }
    }

    private SetMappings setMappingById(int id) {
        for (SetMappings setMapping: getSetMappings()){
            if (setMapping.getId() == id){
                return setMapping;
            }
        }
        return null;
    }
    
    public Set<String> getTargetUris(){
        HashSet<String> targetUris = new HashSet<String>();
        for (SetMappings setMapping: getSetMappings()){
            targetUris.addAll(setMapping.getTargetUris());           
        }
        for (UriMapping mapping:getMappings()){
            targetUris.add(mapping.getTargetUri());
        }

        return targetUris;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("Lens: ");
        sb.append(getLens());
        for (SetMappings setMapping: getSetMappings()){
            setMapping.append(sb);           
        }
        sb.append("\n\tUriSpace based mappings");
        for (UriMapping mapping:getMappings()){
            mapping.append(sb);
        }
        return sb.toString();
    }

    /**
     * @return the lens
     */
    public String getLens() {
        return lens;
    }

    /**
     * @return the setMappings
     */
    public Set<SetMappings> getSetMappings() {
        return setMappings;
    }

    /**
     * @return the mappings
     */
    public Set<UriMapping> getMappings() {
        return mappings;
    }
}
