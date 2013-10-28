/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.ws.uri.client;

import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.uri.UriMapper;
import org.bridgedb.ws.WSCoreClient;
import org.bridgedb.ws.WSCoreInterface;
import org.bridgedb.ws.WSCoreMapper;
import org.bridgedb.ws.WSUriInterface;
import org.bridgedb.ws.WSUriMapper;

/**
 *
 * @author Christian
 */
public class ClientDemo {
  
    public static void main(String[] args) throws Exception {
        WSCoreInterface webService = new WSCoreClient("http://openphacts.cs.man.ac.uk:9093/QueryExpander");
        IDMapper idmapper = new WSCoreMapper(webService);
        WSUriInterface webService2 = new WSUriClient("http://openphacts.cs.man.ac.uk:9093/QueryExpander");
        UriMapper uriMapper = new WSUriMapper(webService2);
        DataSourceTxt.init();
        DataSource enhs = DataSource.getExistingBySystemCode("Cs");
        Xref xref = new Xref("56586", enhs);
        Set<Xref> answer = idmapper.mapID(xref);
        System.out.println(answer);
        Set<Xref> answer2 = uriMapper.mapID(xref);
        System.out.println(answer2);
    }
}
