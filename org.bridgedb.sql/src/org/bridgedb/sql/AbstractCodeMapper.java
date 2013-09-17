/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.sql;

import java.util.HashSet;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;

/**
 *
 * @author Christian
 */
public abstract class AbstractCodeMapper implements  CodeMapper{

    abstract String toCodeNoNull(DataSource dataSource);
    
    abstract IdSysCodePair toIdSysCodePairNoNull(Xref xref);

    @Override
    public abstract DataSource findDataSource(String code);
    
    @Override
    public abstract Xref toXref(IdSysCodePair pair);
    
    @Override
    public String toCode(DataSource dataSource) {
        if (dataSource == null){
            return null;
        }
        return toCodeNoNull(dataSource);
    }

    @Override
    public String[] toCodes(DataSource[] dataSources) {
        if (dataSources == null){
            return null;
        }
        String[] codes = new String[dataSources.length];
        for (int i = 0; i < dataSources.length; i++){
            if (dataSources[i] != null){
                codes[i] = toCodeNoNull(dataSources[i]);
            }
        }
        return codes;
    }
    
    @Override
    public IdSysCodePair toIdSysCodePair(Xref xref) {
        if (xref == null) {
            return null;
        }
        if (xref.getId() == null || xref.getId().isEmpty()) {
            return null;
        }
        if (xref.getDataSource() == null ) {
            return null;
        }
        return toIdSysCodePairNoNull(xref);
    }

    public Set<Xref> toXrefs(Set<IdSysCodePair> pairs){
        HashSet<Xref> refs = new HashSet<Xref>();
        for (IdSysCodePair pair:pairs){
            refs.add(toXref(pair));
        }
        return refs;
    }
        
}
