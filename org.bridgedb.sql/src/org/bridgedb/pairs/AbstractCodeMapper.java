/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.pairs;

import java.util.HashSet;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public abstract class AbstractCodeMapper implements  CodeMapper{

    protected abstract String toCodeNoNull(DataSource dataSource) throws BridgeDBException;
    
    protected abstract IdSysCodePair toIdSysCodePairNoNull(Xref xref) throws BridgeDBException;

    @Override
    public abstract DataSource findDataSource(String code) throws BridgeDBException;
    
    @Override
    public abstract Xref toXref(IdSysCodePair pair) throws BridgeDBException;
    
    @Override
    public String toCode(DataSource dataSource) throws BridgeDBException {
        if (dataSource == null){
            return null;
        }
        return toCodeNoNull(dataSource);
    }

    @Override
    public String[] toCodes(DataSource[] dataSources) throws BridgeDBException {
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
    public IdSysCodePair toIdSysCodePair(Xref xref) throws BridgeDBException {
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

    public Set<Xref> toXrefs(Set<IdSysCodePair> pairs) throws BridgeDBException{
        HashSet<Xref> refs = new HashSet<Xref>();
        for (IdSysCodePair pair:pairs){
            refs.add(toXref(pair));
        }
        return refs;
    }
        
}
