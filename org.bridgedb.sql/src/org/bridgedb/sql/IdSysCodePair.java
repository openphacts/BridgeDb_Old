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
package org.bridgedb.sql;

import java.util.HashSet;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;

/**
 * A thin wrapper around two String which represent the Id and dataSourceCode parts of a protential Xref.
 * 
 * @author Christian
 */
public class IdSysCodePair {
    
    private final String id;
    private final String dataSourceCode;
    
    public IdSysCodePair(String id, String dataSourceCode){
        this.id = id;
        this.dataSourceCode = dataSourceCode;
    }
    
    public Xref toXref(){
        DataSource dataSource = findDataSource(dataSourceCode);
        return new Xref(id, dataSource);
    }
    
    public static Set<Xref> toXrefs(Set<IdSysCodePair> pairs){
        HashSet<Xref> refs = new HashSet<Xref>();
        for (IdSysCodePair pair:pairs){
            refs.add(pair.toXref());
        }
        return refs;
    }
    
    public static DataSource findDataSource(String code){
        if (code.startsWith("_")){
            String fullName = code.substring(1);
            try {
                return DataSource.getByFullName(fullName);
            } catch (IllegalArgumentException ex){
                return DataSource.register(null, fullName).asDataSource();
            }
        } else {
            try {
                return DataSource.getBySystemCode(code);
            } catch (IllegalArgumentException ex){
                return DataSource.register(code, code).asDataSource();
            }
        }
    }

}
