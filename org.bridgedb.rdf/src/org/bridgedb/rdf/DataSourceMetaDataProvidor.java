/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

import java.util.HashMap;

/**
 *
 * @author Christian
 */
public enum DataSourceMetaDataProvidor {
    
    BIO, RDF, MIRIAM_CHANGES, MIRIAM_ONLY;
    
    private static HashMap<String, DataSourceMetaDataProvidor> register = 
            new HashMap<String, DataSourceMetaDataProvidor>();
    
    public static void setProvidor (String sysCode, DataSourceMetaDataProvidor providor){
        DataSourceMetaDataProvidor old = register.get(sysCode);
        if (old!= null && old.compareTo(providor) <= 0){
            return;
        }
        register.put(sysCode, providor);
    }
}
