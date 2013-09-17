/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.sql;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;

/**
 *
 * @author Christian
 */
public class SyscodeBasedCodeMapper extends AbstractCodeMapper implements  CodeMapper{

    @Override
    public DataSource findDataSource(String code) {
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

   @Override
   String toCodeNoNull(DataSource dataSource) {
        if (dataSource.getSystemCode() == null || dataSource.getSystemCode().isEmpty()){
            return "_" + dataSource.getFullName();
        }
        return dataSource.getSystemCode();
    }

    @Override
    IdSysCodePair toIdSysCodePairNoNull(Xref xref) {
        String id = xref.getId();
        String sysCode = toCodeNoNull(xref.getDataSource());
        return new IdSysCodePair(id, sysCode);
    }

    @Override
    public Xref toXref(IdSysCodePair pair) {
        DataSource dataSource = findDataSource(pair.getSysCode());
        String id = pair.getId();
        return new Xref(id, dataSource);
    }
            
}
