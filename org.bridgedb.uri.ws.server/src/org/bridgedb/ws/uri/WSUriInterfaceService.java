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
package org.bridgedb.ws.uri;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.Lens;
import org.bridgedb.uri.Mapping;
import org.bridgedb.uri.MappingsBySet;
import org.bridgedb.uri.SetMappings;
import org.bridgedb.uri.UriListener;
import org.bridgedb.uri.UriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WSCoreService;
import org.bridgedb.ws.WSUriInterface;
import org.bridgedb.ws.WsConstants;
import org.bridgedb.ws.WsUriConstants;
import org.bridgedb.ws.bean.DataSourceUriPatternBean;
import org.bridgedb.ws.bean.LensBean;
import org.bridgedb.ws.bean.LensesBean;
import org.bridgedb.ws.bean.MappingSetInfoBean;
import org.bridgedb.ws.bean.MappingSetInfosBean;
import org.bridgedb.ws.bean.MappingsBean;
import org.bridgedb.ws.bean.MappingsBySetBean;
import org.bridgedb.ws.bean.OverallStatisticsBean;
import org.bridgedb.ws.bean.UriExistsBean;
import org.bridgedb.ws.bean.UriMappings;
import org.bridgedb.ws.bean.UriSearchBean;
import org.bridgedb.ws.bean.XrefBean;

@Path("/")
public class WSUriInterfaceService extends WSCoreService implements WSUriInterface {

    protected UriMapper uriMapper;
    protected UriListener uriListener;
//    protected LinksetInterfaceMinimal linksetInterface;
//    private String validationTypeString;
    public final String MIME_TYPE = "mimeType";
    public final String STORE_TYPE = "storeType";
    public final String VALIDATION_TYPE = "validationType";
    public final String INFO = "info"; 
    public final String FILE = "file";     
    public final String NO_RESULT = null;
    
    static final Logger logger = Logger.getLogger(WSUriInterfaceService.class);

    /**
     * Defuault constuctor for super classes.
     * 
     * Super classes will have the responsibilites of setting up the idMapper.
     */
    protected WSUriInterfaceService() throws BridgeDBException {
        super();
//        this.linksetInterface = new LinksetLoader();
        uriMapper = SQLUriMapper.getExisting();
        uriListener = SQLUriMapper.getExisting();
        idMapper = uriMapper;
    }

    public WSUriInterfaceService(UriMapper uriMapper) throws BridgeDBException {
        super(uriMapper);
        this.uriMapper = uriMapper;
///        this.linksetInterface = new LinksetLoader();
        logger.info("WS Service running using supplied uriMapper");
    }

    private MappingsBean mapInner(String id, String scrCode, String uri, String lensUri, List<String> targetCodes,
            String graph, List<String> targetUriPatterns) throws BridgeDBException {
        if (logger.isDebugEnabled()){
            logger.debug("map called! ");
            if (id != null){
                logger.debug("id = " + id);
            }
            if (scrCode != null){
                logger.debug("   scrCode = " + scrCode);             
            }
            if (uri != null){
                logger.debug("   uri = " + uri);             
            }
            logger.debug("   lensUri = " + lensUri);
            if (targetCodes!= null && !targetCodes.isEmpty()){
                logger.debug("   targetCodes = " + targetCodes);
            }
            if (targetUriPatterns!= null && !targetUriPatterns.isEmpty()){
                logger.debug("   targetUriPatterns = " + targetUriPatterns);
            }
        }
        DataSource[] targetDataSources = getDataSources(targetCodes);
        UriPattern[] targetPatterns = getUriPatterns(targetUriPatterns);
        if (id == null){
            if (scrCode != null) {
                throw new BridgeDBException (WsConstants.DATASOURCE_SYSTEM_CODE + " parameter " + scrCode 
                        + " should only be used together with " + WsConstants.ID + " parameter "); 
            }
            if (uri == null){
                throw new BridgeDBException ("Please provide either a " + WsConstants.ID + " or a "
                        + WsUriConstants.URI + " parameter.");                 
            }  
            return map(uri, lensUri, targetDataSources, graph, targetPatterns);
        } else {
            if (uri != null){
                throw new BridgeDBException ("Please provide either a " + WsConstants.ID + " or a "
                        + WsConstants.DATASOURCE_SYSTEM_CODE + " parameter, but not both.");                 
            }
            if (scrCode == null) {
                throw new BridgeDBException (WsConstants.ID + " parameter must come with a " 
                        + WsConstants.DATASOURCE_SYSTEM_CODE + " parameter."); 
            }
        }
        return map(id, scrCode, lensUri, targetDataSources, graph, targetPatterns);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.MAP)
    @Override
    public Response map(
            @QueryParam(WsConstants.ID) String id,
            @QueryParam(WsConstants.DATASOURCE_SYSTEM_CODE) String scrCode,
    		@QueryParam(WsUriConstants.URI) String uri,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE) List<String> targetCodes,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns) throws BridgeDBException {
        MappingsBean result = mapInner (id, scrCode, uri, lensUri, targetCodes, graph, targetUriPatterns);
        if (result.asMappings().isEmpty()){
            return Response.status(Response.Status.NO_CONTENT).build();
        } 
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.MAP)
    public Response mapJson(
            @QueryParam(WsConstants.ID) String id,
            @QueryParam(WsConstants.DATASOURCE_SYSTEM_CODE) String scrCode,
    		@QueryParam(WsUriConstants.URI) String uri,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE) List<String> targetCodes,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns) throws BridgeDBException {
        MappingsBean result = mapInner (id, scrCode, uri, lensUri, targetCodes, graph, targetUriPatterns);
        if (result.asMappings().isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }
 
    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.MAP)
    public Response mapHtml(
            @QueryParam(WsConstants.ID) String id,
            @QueryParam(WsConstants.DATASOURCE_SYSTEM_CODE) String scrCode,
    		@QueryParam(WsUriConstants.URI) String uri,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE) List<String> targetCodes,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        MappingsBean result = mapInner (id, scrCode, uri, lensUri, targetCodes, graph, targetUriPatterns);
        if (result.asMappings().isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    private UriMappings mapUriInner(List<String> uris, String lensUri, String graph, List<String> targetUriPatterns) 
            throws BridgeDBException {
       if (logger.isDebugEnabled()){
            logger.debug("map called! ");
            logger.debug("   uri = " + uris);             
            logger.debug("   lensUri = " + lensUri);
            if (targetUriPatterns!= null && !targetUriPatterns.isEmpty()){
                logger.debug("   targetUriPatterns = " + targetUriPatterns);
            }
       }
       Set<String> results = new HashSet<String>();
       UriPattern[] targetPatterns = getUriPatterns(targetUriPatterns);
       for(String single:uris){
           results.addAll(uriMapper.mapUri(single, lensUri, graph, targetPatterns));
       }
       return UriMappings.asBean(results);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.MAP_URI)
    @Override
    public Response mapUri(
    		@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns) throws BridgeDBException {
        UriMappings result = mapUriInner(uris, lensUri, graph, targetUriPatterns);
        if (result.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.MAP_URI)
    public Response mapUriJson(
    		@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns) throws BridgeDBException {
        UriMappings result = mapUriInner(uris, lensUri, graph, targetUriPatterns);
        if (result.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.MAP_URI)
    public Response mapUriHtml(
    		@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        UriMappings result = mapUriInner(uris, lensUri, graph, targetUriPatterns);
        if (result.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }

    protected final MappingsBySet mapBySetInner(List<String> uris, String lensUri, String graph, List<String> targetUriPatterns) throws BridgeDBException {
        HashSet<String> uriSet = new HashSet<String>(uris);
        UriPattern[] targetPatterns = getUriPatterns(targetUriPatterns);
        return uriMapper.mapBySet(uriSet, lensUri, graph, targetPatterns);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.MAP_BY_SET)
    public Response mapBySet(@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns) throws BridgeDBException {
        MappingsBySet mappingsBySet = mapBySetInner(uris, lensUri, graph, targetUriPatterns);
        if (mappingsBySet.isEmpty()){
            return Response.noContent().build();
        } 
        MappingsBySetBean result = new MappingsBySetBean(mappingsBySet);
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.MAP_BY_SET)
    public Response mapBySetJson(@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns) throws BridgeDBException {
        MappingsBySet mappingsBySet = mapBySetInner(uris, lensUri, graph, targetUriPatterns);
        if (mappingsBySet.isEmpty()){
            return Response.noContent().build();
        } 
        MappingsBySetBean result = new MappingsBySetBean(mappingsBySet);
        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.MAP_BY_SET)
    public Response mapBySetHtml(@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        MappingsBySet mappingsBySet = mapBySetInner(uris, lensUri, graph, targetUriPatterns);
        if (mappingsBySet.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        MappingsBySetBean result = new MappingsBySetBean(mappingsBySet);
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }
 
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.URI_EXISTS)
    @Override
    public Response UriExists(@QueryParam(WsUriConstants.URI) String URI) throws BridgeDBException {
        if (URI == null) throw new BridgeDBException(WsUriConstants.URI + " parameter missing.");
        if (URI.isEmpty()) throw new BridgeDBException(WsUriConstants.URI + " parameter may not be null.");
        boolean exists = uriMapper.uriExists(URI);
        UriExistsBean bean = new UriExistsBean(URI, exists);
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.URI_EXISTS)
    public Response UriExistsJson(@QueryParam(WsUriConstants.URI) String URI) throws BridgeDBException {
        if (URI == null) throw new BridgeDBException(WsUriConstants.URI + " parameter missing.");
        if (URI.isEmpty()) throw new BridgeDBException(WsUriConstants.URI + " parameter may not be null.");
        boolean exists = uriMapper.uriExists(URI);
        UriExistsBean bean = new UriExistsBean(URI, exists);
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }
    //No html as never no context

    private UriSearchBean UriSearchInner(String text,String limitString) throws BridgeDBException {
        if (text == null) throw new BridgeDBException(WsUriConstants.TEXT + " parameter missing.");
        if (text.isEmpty()) throw new BridgeDBException(WsUriConstants.TEXT + " parameter may not be null.");
        UriSearchBean bean;
        if (limitString == null || limitString.isEmpty()){
            Set<String> uris = uriMapper.uriSearch(text, Integer.MAX_VALUE);
            return new UriSearchBean(text, uris);
        } else {
            int limit = Integer.parseInt(limitString);
            Set<String> uris = uriMapper.uriSearch(text, limit);
            return new UriSearchBean(text, uris);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.URI_SEARCH)
    @Override
    public Response UriSearch(@QueryParam(WsUriConstants.TEXT) String text,
            @QueryParam(WsUriConstants.LIMIT) String limitString) throws BridgeDBException {
        UriSearchBean bean = UriSearchInner(text, limitString);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.URI_SEARCH)
    public Response UriSearchJson(@QueryParam(WsUriConstants.TEXT) String text,
            @QueryParam(WsUriConstants.LIMIT) String limitString) throws BridgeDBException {
        UriSearchBean bean = UriSearchInner(text, limitString);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.URI_SEARCH)
    public Response UriSearchHtml(@QueryParam(WsUriConstants.TEXT) String text,
            @QueryParam(WsUriConstants.LIMIT) String limitString,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        UriSearchBean bean = UriSearchInner(text, limitString);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    public XrefBean toXrefInner(String URI) throws BridgeDBException {
        if (URI == null) throw new BridgeDBException(WsUriConstants.URI + " parameter missing.");
        if (URI.isEmpty()) throw new BridgeDBException(WsUriConstants.URI + " parameter may not be null.");
        Xref xref = uriMapper.toXref(URI);
        if (xref == null){
            return new XrefBean();  //Returns an empty bean
        } else {
            return new XrefBean(xref);
        }
    }
   
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.TO_XREF)
    @Override
    public Response toXref(@QueryParam(WsUriConstants.URI) String URI) throws BridgeDBException {     
        XrefBean bean = toXrefInner(URI);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.TO_XREF)
    public Response toXrefJson(@QueryParam(WsUriConstants.URI) String URI) throws BridgeDBException {     
        XrefBean bean = toXrefInner(URI);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.TO_XREF)
    public Response toXrefHtml(@QueryParam(WsUriConstants.URI) String URI,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {     
        XrefBean bean = toXrefInner(URI);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + WsUriConstants.GET_OVERALL_STATISTICS) 
    @Override
    public Response getOverallStatistics(@QueryParam(WsUriConstants.LENS_URI) String lensUri) 
            throws BridgeDBException {
        OverallStatistics overallStatistics = uriMapper.getOverallStatistics(lensUri);
        OverallStatisticsBean bean = OverallStatisticsBean.asBean(overallStatistics);
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.GET_OVERALL_STATISTICS) 
    public Response getOverallStatisticsJson(@QueryParam(WsUriConstants.LENS_URI) String lensUri) 
            throws BridgeDBException {
        OverallStatistics overallStatistics = uriMapper.getOverallStatistics(lensUri);
        OverallStatisticsBean bean = OverallStatisticsBean.asBean(overallStatistics);
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + SetMappings.METHOD_NAME + WsUriConstants.XML) 
    public Response getMappingSetInfosXML(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
            @QueryParam(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri) throws BridgeDBException {
        return getMappingSetInfos(scrCode, targetCode, lensUri);
    }
    
    private MappingSetInfosBean getMappingSetInfosInner(String scrCode, String targetCode, String lensUri) throws BridgeDBException {
        List<MappingSetInfo> infos = uriMapper.getMappingSetInfos(scrCode, targetCode, lensUri);
        MappingSetInfosBean bean = new MappingSetInfosBean();
        for (MappingSetInfo info:infos){
            bean.addMappingSetInfo(info);
        }
        return bean;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + SetMappings.METHOD_NAME) 
    public Response getMappingSetInfos(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
            @QueryParam(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri) throws BridgeDBException {
        MappingSetInfosBean bean = getMappingSetInfosInner(scrCode, targetCode, lensUri);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + SetMappings.METHOD_NAME) 
    public Response getMappingSetInfosJson(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
            @QueryParam(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri) throws BridgeDBException {
        MappingSetInfosBean bean = getMappingSetInfosInner(scrCode, targetCode, lensUri);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + SetMappings.METHOD_NAME) 
    public Response getMappingSetInfosHtml(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
            @QueryParam(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        MappingSetInfosBean bean = getMappingSetInfosInner(scrCode, targetCode, lensUri);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

	private LensBean getLensInner(String id) throws BridgeDBException {
 		Lens lens = Lens.byId(id);
		return new LensBean(lens, null);
  	}

    @GET
	@Produces({MediaType.APPLICATION_XML})
	@Path(Lens.URI_PREFIX + "{id}")
    @Override
	public Response getLens(@PathParam("id") String id) throws BridgeDBException {
		LensBean bean = getLensInner(id);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
	}
    
    @GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path(Lens.URI_PREFIX + "{id}")
	public Response getLensJson(@PathParam("id") String id) throws BridgeDBException {
		LensBean bean = getLensInner(id);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
	}
    
    @GET
	@Produces({MediaType.TEXT_HTML})
	@Path(Lens.URI_PREFIX + "{id}")
	public Response getLensHtml(@PathParam("id") String id,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
		LensBean bean = getLensInner(id);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
	}
    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + Lens.METHOD_NAME + WsUriConstants.XML) 
    public Response getLensesXML(@QueryParam(WsUriConstants.LENS_URI) String lensUri) throws BridgeDBException {
        return getLenses(lensUri);
    }

    protected List<Lens> getTheLens(String lensUri) throws BridgeDBException{
        if (lensUri == null || lensUri.isEmpty()){
            return  Lens.getLens();
        } else {
            Lens lens = Lens.byId(lensUri);
            List<Lens> lenses = new ArrayList<Lens>();
            lenses.add(lens);  
            return lenses;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + Lens.METHOD_NAME) 
    @Override
	public Response getLenses(@QueryParam(WsUriConstants.LENS_URI) String lensUri) throws BridgeDBException {
        List<Lens> lenses = getTheLens(lensUri);
		LensesBean bean = new LensesBean(lenses, null);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
	}
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + Lens.METHOD_NAME) 
 	public Response getLensesJson(@QueryParam(WsUriConstants.LENS_URI) String lensUri) throws BridgeDBException {
        List<Lens> lenses = getTheLens(lensUri);
		LensesBean bean = new LensesBean(lenses, null);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
	}
    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + Lens.METHOD_NAME) 
 	public Response getLensesHtml(@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        List<Lens> lenses = getTheLens(lensUri);
		LensesBean bean = new LensesBean(lenses, null);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
	}
    
    private MappingSetInfoBean getMappingSetInfoInner(String idString) throws BridgeDBException {
        if (idString == null) {
            throw new BridgeDBException("Path parameter missing.");
        }
        if (idString.isEmpty()) {
            throw new BridgeDBException("Path parameter may not be null.");
        }
        int id = Integer.parseInt(idString);
        MappingSetInfo info = uriMapper.getMappingSetInfo(id);
        return new MappingSetInfoBean(info);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/" + SetMappings.METHOD_NAME + "/{id}")
    @Override
    public Response getMappingSetInfo(@PathParam("id") String idString) throws BridgeDBException {  
        MappingSetInfoBean bean = getMappingSetInfoInner(idString);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + SetMappings.METHOD_NAME + "/{id}")
    public Response getMappingSetInfoJson(@PathParam("id") String idString) throws BridgeDBException {  
        MappingSetInfoBean bean = getMappingSetInfoInner(idString);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + SetMappings.METHOD_NAME + "/{id}")
    public Response getMappingSetInfo(@PathParam("id") String idString,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {  
        MappingSetInfoBean bean = getMappingSetInfoInner(idString);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.DATA_SOURCE)
    public Response getDataSource() throws BridgeDBException {
        throw new BridgeDBException("id path parameter missing.");
    }

    private DataSourceUriPatternBean getDataSourceInner(String id) throws BridgeDBException {
        if (id == null) {
            throw new BridgeDBException("Path parameter missing.");
        }
        if (id.isEmpty()) {
            throw new BridgeDBException("Path parameter may not be null.");
        }
        DataSource ds = DataSource.getBySystemCode(id);
        return new DataSourceUriPatternBean(ds, uriMapper.getUriPatterns(id));
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Override
    @Path("/" + WsUriConstants.DATA_SOURCE + "/{id}")
    public Response getDataSource(@PathParam("id") String id) throws BridgeDBException {
        DataSourceUriPatternBean bean = getDataSourceInner(id);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/" + WsUriConstants.DATA_SOURCE + "/{id}")
    public Response getDataSourceJson(@PathParam("id") String id) throws BridgeDBException {
        DataSourceUriPatternBean bean = getDataSourceInner(id);
        if (bean.isEmpty()){
            return Response.noContent().build();
        } 
        return Response.ok(bean, MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.DATA_SOURCE + "/{id}")
    public Response getDataSourceHtml(@PathParam("id") String id,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        DataSourceUriPatternBean bean = getDataSourceInner(id);
        if (bean.isEmpty()){
            return noContectWrapper(httpServletRequest);
        } 
        return Response.ok(bean, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    @Override
    @Path("/" + WsUriConstants.SQL_COMPAT_VERSION)
    public Response getSqlCompatVersion() throws BridgeDBException {
        return Response.ok(uriMapper.getSqlCompatVersion(), MediaType.TEXT_PLAIN).build();
    }

    //****** Support functions *****
   
    private MappingsBean map(String uri, String lensUri, DataSource[] targetDataSources, 
            String graph, UriPattern[] targetPatterns) throws BridgeDBException {
        Set<Mapping> mappings;
        if (targetDataSources == null){
            if (targetPatterns == null){
                mappings = uriMapper.mapFull(uri, lensUri, graph);
            } else {
                mappings = mapByTargetUriPattern(uri, lensUri, graph, targetPatterns);
            }
        } else {
            mappings = mapByTargetDataSource (uri, lensUri, targetDataSources);
            if (targetPatterns != null){
                mappings.addAll(mapByTargetUriPattern(uri, lensUri, graph, targetPatterns));                
            } 
        }
        return new MappingsBean(mappings); 
    }
    
    private MappingsBean map(String id, String scrCode, String lensUri, DataSource[] targetDataSources, 
            String graph, UriPattern[] targetPatterns) throws BridgeDBException {
        DataSource dataSource = DataSource.getBySystemCode(scrCode);
        Xref sourceXref = new Xref(id, dataSource);
        Set<Mapping> mappings;
        if (targetDataSources == null){
            if (targetPatterns == null){
                mappings = uriMapper.mapFull(sourceXref, lensUri, graph);
            } else {
                mappings = mapByTargetUriPattern(sourceXref, lensUri, graph, targetPatterns);
            }
        } else {
            mappings = mapByTargetDataSource (sourceXref, lensUri, targetDataSources);
            if (targetPatterns != null){
                mappings.addAll(mapByTargetUriPattern(sourceXref, lensUri, graph, targetPatterns));                
            } 
        }
        return new MappingsBean(mappings); 
    }

    private DataSource[] getDataSources(List<String> targetCodes){
        if (targetCodes == null || targetCodes.isEmpty()){
            return null;
        }
        HashSet<DataSource> targets = new HashSet<DataSource>();
        for (String targetCode:targetCodes){
            if (targetCode != null && !targetCode.isEmpty()){
                targets.add(DataSource.getBySystemCode(targetCode));
            }
        }
        if (targets.isEmpty()){
            return null; 
        }
        return targets.toArray(new DataSource[0]);        
    }
            
    final UriPattern[] getUriPatterns(List<String> targetUriPatterns) throws BridgeDBException{
        if (targetUriPatterns == null || targetUriPatterns.isEmpty()){
            return null;
        }
        HashSet<UriPattern> targets = new HashSet<UriPattern>();
        for (String targetUriPattern:targetUriPatterns){
            UriPattern pattern = UriPattern.alreadyExistingByPattern(targetUriPattern);
            if (pattern != null){
                targets.add(pattern);
            } else {
                throw new BridgeDBException ("No pattern knwo for " +targetUriPattern);
            }
        }
        if (targets.isEmpty()){
            return new UriPattern[0]; 
        }
        return targets.toArray(new UriPattern[0]);        
    }
    
    private Set<Mapping> mapByTargetDataSource(String sourceUri, String lensUri, DataSource[] targetDataSources) throws BridgeDBException{
        if (targetDataSources.length > 0){
            return uriMapper.mapFull(sourceUri, lensUri, targetDataSources);
        } else {
            return  new HashSet<Mapping>();
        }    
    }
    
    private Set<Mapping> mapByTargetDataSource(Xref sourceXref, String lensUri, DataSource[] targetDataSources) throws BridgeDBException{
        if (targetDataSources.length > 0){
            return uriMapper.mapFull(sourceXref, lensUri, targetDataSources);
        } else {
            return  new HashSet<Mapping>();
        }    
    }
    
    private Set<Mapping> mapByTargetUriPattern(String sourceUri, String lensUri, String graph, UriPattern[] targetUriPattern) throws BridgeDBException{
        if (targetUriPattern.length > 0){
            return uriMapper.mapFull(sourceUri, lensUri, graph, targetUriPattern);
        } else {
            return  new HashSet<Mapping>();
        }    
    }
    
    private Set<Mapping> mapByTargetUriPattern(Xref sourceXref, String lensUri, String graph, UriPattern[] targetUriPattern) throws BridgeDBException{
        if (targetUriPattern.length > 0){
            return uriMapper.mapFull(sourceXref, lensUri, graph, targetUriPattern);
        } else {
            return  new HashSet<Mapping>();
        }    
    }

    private String trim(String original){
        String result = original.trim();
        while (result.startsWith("\"")){
            result = result.substring(1);
        }
        while (result.endsWith("\"")){
            result = result.substring(0,result.length()-1);
        }
        return result.trim();
    }
    
    protected final void validateInfo(String info) throws BridgeDBException{
        if (info == null){
            throw new BridgeDBException (INFO + " parameter may not be null");
        }
        if (info.trim().isEmpty()){
            throw new BridgeDBException (INFO + " parameter may not be empty");
        }        
    }
    
    void validateInputStream(InputStream inputStream) throws BridgeDBException {
        if (inputStream == null){
            throw new BridgeDBException (FILE + " parameter may not be null");
        }
    }

}
