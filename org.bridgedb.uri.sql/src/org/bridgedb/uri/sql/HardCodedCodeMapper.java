/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.uri.sql;

import java.util.HashMap;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.pairs.IdSysCodePair;
import org.bridgedb.pairs.SyscodeBasedCodeMapper;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class HardCodedCodeMapper extends SyscodeBasedCodeMapper implements  ExtendedCodeMapper{

    HashMap<String,String> xrefExtensions = new HashMap<String,String>();
                
    public HardCodedCodeMapper() throws BridgeDBException{
        xrefExtensions.put("Ce","CHEBI:");
        xrefExtensions.put("T","GO");
        xrefExtensions.put("M","MGI:");
    }
    
    @Override
    protected IdSysCodePair toIdSysCodePairNoNull(Xref xref) {
        String sysCode = toCodeNoNull(xref.getDataSource());
        String id;
        if (xrefExtensions.containsValue(sysCode)){
            id = xref.getId().substring(xrefExtensions.get(sysCode).length());
        } else {
            id = xref.getId();
        }
        return new IdSysCodePair(id, sysCode);
    }

    @Override
    public Xref toXref(IdSysCodePair pair) throws BridgeDBException{
        DataSource dataSource = findDataSource(pair.getSysCode());
        String id;
        if (xrefExtensions.containsValue(pair.getSysCode())){
            id = xrefExtensions.get(pair.getSysCode()) + pair.getId();
        } else {
            id = pair.getId();
        }
        return new Xref(id, dataSource);
    }
            
    @Override
    public String toCode(UriPattern pattern) throws BridgeDBException {
         return pattern.getCode();
    }
       
}
