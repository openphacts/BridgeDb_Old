/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

import java.util.HashMap;
import org.bridgedb.DataSource;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class DataSourceTypeMapper {
    
    private static HashMap<DataSource,String> mappings = new HashMap<DataSource,String>();
    private static String VARIOUS = BridgeDBConstants.VARIOUS;
    
    public static String put(DataSource ds, String type) throws BridgeDBException{
        if (mappings.containsKey(ds)){
            if (!mappings.get(ds).equals(type)){
                throw new BridgeDBException("Unable change type of " + ds + " from " + mappings.get(ds) + " to " + type);
            } else {
                return type;
            }
        } else {
            if (type != null){
                mappings.put(ds, type);
            }
            return null;
        }
    }
    
    public static void setVarious(DataSource ds) throws BridgeDBException{
        if (mappings.containsKey(ds)){
            if (!mappings.get(ds).equals(VARIOUS)){
                throw new BridgeDBException("Illegal call to setVarious on " + ds + " as it is already typed " + mappings.get(ds));
            }
        } else {
            mappings.put(ds, VARIOUS);
        }
    }
    
    public static String get(DataSource ds) throws BridgeDBException{
        return mappings.get(ds);
    }

    public static String getType(DataSource ds) throws BridgeDBException{
        String type = mappings.get(ds);
        if (type == null){
            throw new BridgeDBException("No type known for " + ds);
        }
        if (type.equals(VARIOUS)){
            throw new BridgeDBException("Mapping to a single type not possible for " + ds);
        }
        return type;
    }
}
