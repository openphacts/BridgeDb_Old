/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf.pairs;

import java.util.HashMap;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.pairs.CodeMapper;
import org.bridgedb.pairs.IdSysCodePair;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class RdfBasedCodeMapper implements CodeMapper {

    private static HashMap<String,String> xrefExtensions = new HashMap<String,String>();
    
    public static void addMapping(String sysCode, String extension) throws BridgeDBException {
        if (sysCode == null || sysCode.isEmpty()){
            throw new BridgeDBException ("Illegal sysCode:" + sysCode);
        }
        if (extension == null || extension.isEmpty()){
            throw new BridgeDBException ("Extension should not be null or empty. "
                    + "Just don't call this method if no extension is needed." );
        }
        if (xrefExtensions.containsKey(sysCode)){
            if (!extension.equals(xrefExtensions.get(sysCode))){
                throw new BridgeDBException ("Illegal attempt to change extension for sysCode:" + sysCode 
                        + " was " + xrefExtensions.get(sysCode) + " so can not set it to " + sysCode);               
            } 
        } else {
            xrefExtensions.put(sysCode, extension);
        }
    }
    
    @Override
    public IdSysCodePair toIdSysCodePair(Xref xref) {
        String sysCode = xref.getDataSource().getSystemCode();
        String id;
        if (xrefExtensions.containsKey(sysCode)){
            id = xref.getId().substring(xrefExtensions.get(sysCode).length());
        } else {
            id = xref.getId();
        }
        return new IdSysCodePair(id, sysCode);
    }

    @Override
    public Xref toXref(IdSysCodePair pair) throws BridgeDBException{
        DataSource dataSource = DataSource.getExistingBySystemCode(pair.getSysCode());
        String id;
        if (xrefExtensions.containsKey(pair.getSysCode())){
            id = xrefExtensions.get(pair.getSysCode()) + pair.getId();
        } else {
            id = pair.getId();
        }
        return new Xref(id, dataSource);
    }
   
}
