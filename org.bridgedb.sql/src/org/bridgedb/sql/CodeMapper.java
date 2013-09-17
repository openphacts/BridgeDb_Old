/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.sql;

import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public interface CodeMapper {
   
    public DataSource findDataSource(String code) throws BridgeDBException;

    public String toCode (DataSource dataSource) throws BridgeDBException;

    public String[] toCodes(DataSource[] dataSources) throws BridgeDBException;
        
    public IdSysCodePair toIdSysCodePair(Xref xref) throws BridgeDBException;
    
    public Set<Xref> toXrefs(Set<IdSysCodePair> pairs) throws BridgeDBException;

    public Xref toXref(IdSysCodePair pair) throws BridgeDBException;

    
}
