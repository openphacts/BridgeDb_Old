/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.sql;

import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;

/**
 *
 * @author Christian
 */
public interface CodeMapper {
   
    public DataSource findDataSource(String code);

    public String toCode (DataSource dataSource);

    public String[] toCodes(DataSource[] dataSources);
        
    public IdSysCodePair toIdSysCodePair(Xref xref);
    
    public Set<Xref> toXrefs(Set<IdSysCodePair> pairs);

    public Xref toXref(IdSysCodePair pair);

    
}
