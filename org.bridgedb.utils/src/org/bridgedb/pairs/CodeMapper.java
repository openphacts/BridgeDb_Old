/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.pairs;

import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public interface CodeMapper {
   
    public IdSysCodePair toIdSysCodePair(Xref xref) throws BridgeDBException;
    
    public Xref toXref(IdSysCodePair pair) throws BridgeDBException;

    
}
