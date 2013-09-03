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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperCapabilities;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.sql.SQLIdMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.ws.bean.CapabilitiesBean;
import org.bridgedb.ws.bean.DataSourcesBean;
import org.bridgedb.ws.bean.FreeSearchSupportedBean;
import org.bridgedb.ws.bean.MappingSupportedBean;
import org.bridgedb.ws.bean.PropertiesBean;
import org.bridgedb.ws.bean.PropertyBean;
import org.bridgedb.ws.bean.XrefExistsBean;
import org.bridgedb.ws.bean.XrefMapsBean;
import org.bridgedb.ws.bean.XrefsBean;

/**
 * Webservice server code, that uses the ws.core
 * functionality to expose BridgeDB data
 * @author Christian Y. A. Brenninkmeijer
 *
 */
@Path("/")
public class WSCoreService implements WSCoreInterface {

    static final String NO_CONTENT_ON_EMPTY = "no.content.on.empty";
    private final boolean noConentOnEmpty;
            
    static final Logger logger = Logger.getLogger(WSCoreService.class);
    
    protected IDMapper idMapper;

    /**
     * Default constructor for super classes.
     * 
     * Super classes will have the responsibilities of setting up the idMapper.
     */
    protected WSCoreService() throws BridgeDBException{
        this(new SQLIdMapper(false));
    }
    
    public WSCoreService(IDMapper idMapper) throws BridgeDBException {
        this.idMapper = idMapper;
        String property = ConfigReader.getProperty(NO_CONTENT_ON_EMPTY);
        noConentOnEmpty = Boolean.valueOf(property);
        logger.info("WS Service running using supplied idMapper");
    }
        
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES)
    @Override
    public Response getSupportedSrcDataSources() throws BridgeDBException {
        System.err.println(idMapper);
        IDMapperCapabilities capabilities = idMapper.getCapabilities();
        try {
            Set<DataSource> dataSources = capabilities.getSupportedSrcDataSources();
            DataSourcesBean bean = new DataSourcesBean (dataSources);
            if (noConentOnEmpty & bean.isEmpty()){
                return Response.noContent().build();
            }
            return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
        } catch (IDMapperException e){
            throw BridgeDBException.convertToBridgeDB(e);
        }
    } 

    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.FREE_SEARCH)
    @Override
    public Response freeSearch(
            @QueryParam(WsConstants.TEXT) String text,
            @QueryParam(WsConstants.LIMIT) String limitString) throws BridgeDBException {
        if (text == null) throw new BridgeDBException(WsConstants.TEXT + " parameter missing");
        Set<Xref> mappings;
        try {
            if (limitString == null || limitString.isEmpty()){
                mappings = idMapper.freeSearch(text, Integer.MAX_VALUE);
           } else {
                int limit = Integer.parseInt(limitString);
                mappings = idMapper.freeSearch(text,limit);
            }
        } catch (IDMapperException e){
            throw BridgeDBException.convertToBridgeDB(e);
        }
        XrefsBean bean = new XrefsBean(mappings);
        if (noConentOnEmpty & bean.isEmpty()){
            return Response.noContent().build();
        }
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    } 

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.MAP_ID)
    @Override
    public Response mapID(
            @QueryParam(WsConstants.ID) List<String> id,
            @QueryParam(WsConstants.DATASOURCE_SYSTEM_CODE) List<String> scrCode,
            @QueryParam(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE) List<String> targetCodes) throws BridgeDBException {
        if (id == null) throw new BridgeDBException(WsConstants.ID + " parameter missing");
        if (id.isEmpty()) throw new BridgeDBException(WsConstants.ID + " parameter missing");
        if (scrCode == null) throw new BridgeDBException(WsConstants.DATASOURCE_SYSTEM_CODE + " parameter missing");
        if (scrCode.isEmpty()) throw new BridgeDBException(WsConstants.DATASOURCE_SYSTEM_CODE + " parameter missing");
        if (id.size() != scrCode.size()) throw new BridgeDBException("Must have same number of " + WsConstants.ID + 
                " and " + WsConstants.DATASOURCE_SYSTEM_CODE + " parameters");
        ArrayList<Xref> srcXrefs = new ArrayList<Xref>();
        for (int i = 0; i < id.size() ;i++){
            try {
                DataSource dataSource = DataSource.getBySystemCode(scrCode.get(i));
                Xref source = new Xref(id.get(i), dataSource);
                srcXrefs.add(source);
            } catch (IllegalArgumentException ex){
                logger.error(ex.getMessage());
            }
        }
        DataSource[] targetDataSources = new DataSource[targetCodes.size()];
        for (int i=0; i< targetCodes.size(); i++){
             targetDataSources[i] = DataSource.getBySystemCode(targetCodes.get(i));
        }
        
        try {
            Map<Xref, Set<Xref>>  mappings = idMapper.mapID(srcXrefs, targetDataSources);
            XrefMapsBean bean = new XrefMapsBean(mappings);
            if (noConentOnEmpty & bean.isEmpty()){
                return Response.noContent().build();
            }
            return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
        } catch (IDMapperException e){
            throw BridgeDBException.convertToBridgeDB(e);
        }
    } 

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.XREF_EXISTS)
    @Override
    public Response xrefExists( 
            @QueryParam(WsConstants.ID) String id,
            @QueryParam(WsConstants.DATASOURCE_SYSTEM_CODE) String scrCode) throws BridgeDBException {
        if (id == null) throw new BridgeDBException (WsConstants.ID + " parameter can not be null");
        if (scrCode == null) throw new BridgeDBException (WsConstants.DATASOURCE_SYSTEM_CODE + " parameter can not be null");  
        DataSource dataSource;
        try {
            dataSource = DataSource.getBySystemCode(scrCode);
        } catch (IllegalArgumentException ex){
             logger.error(ex.getMessage());
             XrefExistsBean bean = XrefExistsBean.asBean(id, scrCode, false);
             //XrefExists is never empty so never no context
             return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
            
        }
        Xref source = new Xref(id, dataSource);
        try {
            XrefExistsBean bean = XrefExistsBean.asBean(source, idMapper.xrefExists(source));
             //XrefExists is never empty so never no context
            return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
        } catch (IDMapperException e){
            throw BridgeDBException.convertToBridgeDB(e);
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES)
    @Override
    public Response getSupportedTgtDataSources() throws BridgeDBException {
        try {
            Set<DataSource> dataSources = idMapper.getCapabilities().getSupportedSrcDataSources();
            DataSourcesBean bean = new DataSourcesBean(dataSources);
            if (noConentOnEmpty & bean.isEmpty()){
                return Response.noContent().build();
            }
            return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
        } catch (IDMapperException e){
            throw BridgeDBException.convertToBridgeDB(e);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path(WsConstants.IS_FREE_SEARCH_SUPPORTED)
    @Override
    public Response isFreeSearchSupported() {
        FreeSearchSupportedBean bean = new FreeSearchSupportedBean(idMapper.getCapabilities().isFreeSearchSupported());
        //FreeSearchSupported is never empty so never no context
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.IS_MAPPING_SUPPORTED)
    @Override
    public Response isMappingSupported(
            @QueryParam(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String sourceCode, 
            @QueryParam(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode) throws BridgeDBException {
        if (sourceCode == null) throw new BridgeDBException (WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE + " parameter can not be null");
        if (targetCode == null) throw new BridgeDBException (WsConstants.TARGET_DATASOURCE_SYSTEM_CODE + " parameter can not be null");
        DataSource src = DataSource.getBySystemCode(sourceCode);
        DataSource tgt = DataSource.getBySystemCode(targetCode);
        try {
            MappingSupportedBean bean = MappingSupportedBean.asBean(src, tgt, idMapper.getCapabilities().isMappingSupported(src, tgt));
            //MappingSupported is never empty so never no content
            return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
        } catch (IDMapperException e){
            throw BridgeDBException.convertToBridgeDB(e);
        }
        
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.PROPERTY + "/{key}")
    @Override
    public Response getProperty(@PathParam("key")String key) {
        String property = idMapper.getCapabilities().getProperty(key);
        PropertyBean bean = new PropertyBean(key, property);
        if (noConentOnEmpty & bean.isEmpty()){
            return Response.noContent().build();
        }
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/" + WsConstants.GET_KEYS)
    @Override
    public Response getKeys() {
        PropertiesBean bean = new PropertiesBean();
        Set<String> keys = idMapper.getCapabilities().getKeys();
        IDMapperCapabilities idMapperCapabilities = idMapper.getCapabilities();
        for (String key:keys){
            bean.addProperty(key, idMapperCapabilities.getProperty(key));
        }
        if (noConentOnEmpty & bean.isEmpty()){
            return Response.noContent().build();
        }
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsConstants.GET_CAPABILITIES)
    @Override
    public Response getCapabilities()  {
        CapabilitiesBean bean = new CapabilitiesBean(idMapper.getCapabilities());
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }


}
