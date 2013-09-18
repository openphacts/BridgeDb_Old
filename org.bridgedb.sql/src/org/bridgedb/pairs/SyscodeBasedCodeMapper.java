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
public class SyscodeBasedCodeMapper extends AbstractCodeMapper implements  CodeMapper{

    @Override
    public DataSource findDataSource(String code) throws BridgeDBException {
        if (code.startsWith("_")){
            String fullName = code.substring(1);
            try {
                return DataSource.getExistingByFullName(fullName);
            } catch (IllegalArgumentException ex){
                throw new BridgeDBException ("No DataSource known for " + code + " using fullName " + fullName);
            }
        } else {
            try {
                return DataSource.getExistingBySystemCode(code);
            } catch (IllegalArgumentException ex){
                throw new BridgeDBException ("No DataSource known for " + code);
            }
        }
    }

   @Override
   protected String toCodeNoNull(DataSource dataSource) {
        if (dataSource.getSystemCode() == null || dataSource.getSystemCode().isEmpty()){
            return "_" + dataSource.getFullName();
        }
        return dataSource.getSystemCode();
    }

    @Override
    protected IdSysCodePair toIdSysCodePairNoNull(Xref xref) {
        String id = xref.getId();
        String sysCode = toCodeNoNull(xref.getDataSource());
        return new IdSysCodePair(id, sysCode);
    }

    @Override
    public Xref toXref(IdSysCodePair pair) throws BridgeDBException{
        DataSource dataSource = findDataSource(pair.getSysCode());
        String id = pair.getId();
        return new Xref(id, dataSource);
    }
            
}
