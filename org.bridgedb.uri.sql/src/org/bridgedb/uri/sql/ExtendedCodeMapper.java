/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.uri.sql;

import org.bridgedb.pairs.CodeMapper;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public interface ExtendedCodeMapper extends CodeMapper{
    
    public String toCode (UriPattern pattern) throws BridgeDBException;
}
