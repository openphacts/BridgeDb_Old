/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.pairs;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class SyscodeBasedCodeMapper implements  CodeMapper{

    @Override
    public IdSysCodePair toIdSysCodePair(Xref xref) {
        String id = xref.getId();
        String sysCode = xref.getDataSource().getSystemCode();
        return new IdSysCodePair(id, sysCode);
    }

    @Override
    public Xref toXref(IdSysCodePair pair) throws BridgeDBException{
        DataSource dataSource = DataSource.getExistingBySystemCode(pair.getSysCode());
        String id = pair.getId();
        return new Xref(id, dataSource);
    }
            
}
