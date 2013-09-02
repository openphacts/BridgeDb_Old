// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  OpenPhacts
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.bridgedb.ws;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.bean.CapabilitiesBean;
import org.bridgedb.ws.bean.DataSourceBean;
import org.bridgedb.ws.bean.DataSourcesBean;
import org.bridgedb.ws.bean.FreeSearchSupportedBean;
import org.bridgedb.ws.bean.MappingSupportedBean;
import org.bridgedb.ws.bean.PropertyBean;
import org.bridgedb.ws.bean.XrefBean;
import org.bridgedb.ws.bean.XrefExistsBean;
import org.bridgedb.ws.bean.XrefMapBean;
import org.bridgedb.ws.bean.XrefMapsBean;
import org.bridgedb.ws.bean.XrefsBean;

/**
 *
 * @author Christian
 */
public class WSCoreClient implements WSCoreInterface{

    protected final String serviceAddress;

    protected final WebResource webResource;

    @Override
    public XrefMapsBean mapID(List<String> id, List<String> scrCode, List<String> targetCodes) throws BridgeDBException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        for (String one:id){
            params.add(WsConstants.ID, one);
        }
        for (String one:scrCode){
            params.add(WsConstants.DATASOURCE_SYSTEM_CODE, one);
        }
        for (String target:targetCodes){
            params.add(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE, target);
        }
        //Make service call
        XrefMapsBean result = 
                webResource.path(WsConstants.MAP_ID)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<XrefMapsBean>() {});
         return result;
    }

    public WSCoreClient(String serviceAddress) {
        this.serviceAddress = serviceAddress;
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        webResource = client.resource(serviceAddress);        
    }
        
    @Override
    public XrefsBean freeSearch(String text, String limit) throws BridgeDBException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add(WsConstants.TEXT, text);
        params.add(WsConstants.LIMIT, limit);
        //Make service call
        XrefsBean result = 
                webResource.path(WsConstants.FREE_SEARCH)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<XrefsBean>() {});
        return result;
    }

    @Override
    public List<PropertyBean> getKeys() {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        //Make service call
        List<PropertyBean> result = 
                webResource.path(WsConstants.GET_KEYS)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<List<PropertyBean>>() {});
        return result;
    }

    @Override
    public PropertyBean getProperty(String key) {
        //Make service call
        PropertyBean result = 
                webResource.path(WsConstants.PROPERTY + "/" + key)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<PropertyBean>() {});
        return result;
    }

    @Override
    public DataSourcesBean getSupportedSrcDataSources() throws BridgeDBException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        //Make service call
        DataSourcesBean result = 
                webResource.path(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<DataSourcesBean>() {});
        return result;
    }

    @Override
    public DataSourcesBean getSupportedTgtDataSources() throws BridgeDBException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        //Make service call
        DataSourcesBean result = 
                webResource.path(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<DataSourcesBean>() {});
        return result;
    }

    @Override
    public FreeSearchSupportedBean isFreeSearchSupported() {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        //Make service call
        FreeSearchSupportedBean result = 
                webResource.path(WsConstants.IS_FREE_SEARCH_SUPPORTED)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<FreeSearchSupportedBean>() {});
        return result;
    }

    @Override
    public MappingSupportedBean isMappingSupported(String sourceCode, String targetCode) throws BridgeDBException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE, sourceCode);
        params.add(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE, targetCode);
        //Make service call
        MappingSupportedBean result = 
                webResource.path(WsConstants.IS_MAPPING_SUPPORTED)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<MappingSupportedBean>() {});
        return result;
    }

    @Override
    public XrefExistsBean xrefExists(String id, String scrCode) throws BridgeDBException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add(WsConstants.ID, id);
        params.add(WsConstants.DATASOURCE_SYSTEM_CODE, scrCode);
        //Make service call
        XrefExistsBean result = 
                webResource.path(WsConstants.XREF_EXISTS)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<XrefExistsBean>() {});
        return result;
    }

    @Override
    public CapabilitiesBean getCapabilities() {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        //Make service call
        CapabilitiesBean result = 
                webResource.path(WsConstants.GET_CAPABILITIES)
                .queryParams(params)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<CapabilitiesBean>() {});
        return result;
    }

}
