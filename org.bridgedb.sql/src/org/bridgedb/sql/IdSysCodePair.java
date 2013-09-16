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
    private final String sysCode;
    private Xref original;
    
    public IdSysCodePair(String id, String dataSourceCode){
        this.id = id;
        this.sysCode = dataSourceCode;
        original = null;
    }
    
    public IdSysCodePair(Xref xref){
        original = xref;
        this.id = xref.getId();
        this.sysCode = toCode(xref.getDataSource());
    }
    
    public static IdSysCodePair toIdSysCodePair(Xref xref){
        if (xref == null) {
            return null;
        }
        if (xref.getId() == null || xref.getId().isEmpty()) {
            return null;
        }
        if (xref.getDataSource() == null ) {
            return null;
        }
        return new IdSysCodePair(xref);
    }
    
    public Xref toXref(){
        if (original == null){
            DataSource dataSource = findDataSource(getSysCode());
            original = new Xref(getId(), dataSource);
        }
        return original;
    }
    
    /*public static Xref toXref(IdSysCodePair pair) {
        if (pair == null){
            return null;
        }
        return pair.toXref();
    }*/

   public static Set<Xref> toXrefs(Set<IdSysCodePair> pairs){
        HashSet<Xref> refs = new HashSet<Xref>();
        for (IdSysCodePair pair:pairs){
            refs.add(pair.toXref());
        }
        return refs;
    }
    
    public static String toCode (DataSource dataSource){
        if (dataSource == null){
            return null;
        }
        if (dataSource.getSystemCode() == null || dataSource.getSystemCode().isEmpty()){
            return "_" + dataSource.getFullName();
        }
        return dataSource.getSystemCode();
    }

    public static String[] toCodes(DataSource[] dataSources) {
        if (dataSources == null){
            return null;
        }
        String[] codes = new String[dataSources.length];
        for (int i = 0; i < dataSources.length; i++){
            codes[i] = toCode(dataSources[i]);
        }
        return codes;
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

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the sysCode
     */
    public String getSysCode() {
        return sysCode;
    }

}
