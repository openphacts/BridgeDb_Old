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
public class SyscodeBasedCodeMapper implements  CodeMapper{

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
    public String toCode(DataSource dataSource) {
        if (dataSource == null){
            return null;
        }
        if (dataSource.getSystemCode() == null || dataSource.getSystemCode().isEmpty()){
            return "_" + dataSource.getFullName();
        }
        return dataSource.getSystemCode();
    }

    public String[] toCodes(DataSource[] dataSources) {
        if (dataSources == null){
            return null;
        }
        String[] codes = new String[dataSources.length];
        for (int i = 0; i < dataSources.length; i++){
            codes[i] = toCode(dataSources[i]);
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
        String id = xref.getId();
        String sysCode = toCode(xref.getDataSource());
        return new IdSysCodePair(id, sysCode);
    }

    @Override
    public Xref toXref(IdSysCodePair pair) {
        DataSource dataSource = findDataSource(pair.getSysCode());
        String id = pair.getId();
        return new Xref(id, dataSource);
    }
    
    public Set<Xref> toXrefs(Set<IdSysCodePair> pairs){
        HashSet<Xref> refs = new HashSet<Xref>();
        for (IdSysCodePair pair:pairs){
            refs.add(toXref(pair));
        }
        return refs;
    }
        
}
