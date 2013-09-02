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

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.bridgedb.statistics.DataSetInfo;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.Lens;
import org.bridgedb.uri.MappingsBySet;
import org.bridgedb.uri.SetMappings;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WsUriConstants;

import uk.ac.manchester.cs.openphacts.bridgedb.webtemplates.WebTemplates;

/**
 * This class adds the extra services not part of WSUriInterface
 * 
 * Except for the API methods
 * 
 * @author Christian
 */
public class WSOtherservices extends WSAPI implements ServletContextListener {
            
    static final Logger logger = Logger.getLogger(WSOtherservices.class);
	private ServletContext context;

    public WSOtherservices()  throws BridgeDBException   {
        super();
    }
        
    private void uriMappingForm(StringBuilder sb, HttpServletRequest httpServletRequest) throws BridgeDBException {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("contextPath", httpServletRequest.getContextPath());
        velocityContext.put("mapURI", WsUriConstants.MAP_URI);
        velocityContext.put("URI", WsUriConstants.URI);
        velocityContext.put("lensURI", WsUriConstants.LENS_URI);
        velocityContext.put("lenses", Lens.getLens());

        WebTemplates webTemplates = new WebTemplates();
        sb.append(webTemplates.getUriMappingForm(velocityContext));
    }

    /**
     * Welcome page for the Service.
     * 
     * Expected to be overridden by the QueryExpander
     * 
     * @param httpServletRequest
     * @return
     * @throws BridgeDBException
     * @throws UnsupportedEncodingException 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + WsUriConstants.BRIDGEDB_HOME)
    public Response bridgeDbHome(@Context HttpServletRequest httpServletRequest) throws BridgeDBException, UnsupportedEncodingException {
        if (logger.isDebugEnabled()){
            logger.debug("bridgeDbHome called");
        }
        StringBuilder sb = topAndSide ("Identity Mapping Service", httpServletRequest);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("contextPath", httpServletRequest.getContextPath());
        velocityContext.put("mapURI", WsUriConstants.MAP_URI);
        velocityContext.put("URI", WsUriConstants.URI);
        velocityContext.put("lensURI", WsUriConstants.LENS_URI);
        velocityContext.put("lenses", Lens.getLens());
        velocityContext.put("map",WsUriConstants.MAP);

        WebTemplates webTemplates = new WebTemplates();
        sb.append(webTemplates.getBridgeDBHome(velocityContext));

        footerAndEnd(sb);
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    }

     /**
     * Forwarding page for "/api".
     * 
     * This is expected to be overwirriten by the QueryExpander
     * @param httpServletRequest
     * @return
     * @throws BridgeDBException
     * @throws UnsupportedEncodingException 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/api")
    public Response apiPage(@Context HttpServletRequest httpServletRequest) throws BridgeDBException, UnsupportedEncodingException {
        return imsApiPage(httpServletRequest);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + SetMappings.METHOD_NAME)
    public Response getSetMapping(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
            @QueryParam(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode,
            @QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @Context HttpServletRequest httpServletRequest) 
            throws BridgeDBException, UnsupportedEncodingException {
        List<MappingSetInfo> mappingSetInfos = uriMapper.getMappingSetInfos(scrCode, targetCode, lensUri);
        String lensName;
        if (lensUri != null && !lensUri.isEmpty()){
            Lens lensInfo = Lens.byId(lensUri);
            lensName = lensInfo.getName();
        } else {
            lensName = "Default";
        }
        StringBuilder sb = topAndSide("Mapping Summary for " + lensName + " Lens",  httpServletRequest);
        if (mappingSetInfos.isEmpty()){
            sb.append("\n<h1> No mapping found between ");
            MappingSetTableMaker.addDataSourceLink(sb, new DataSetInfo(scrCode,scrCode), httpServletRequest);
            sb.append(" and ");
            MappingSetTableMaker.addDataSourceLink(sb, new DataSetInfo(targetCode,targetCode), httpServletRequest);
            sb.append(" using lens ");
            sb.append(lensUri);
            sb.append("</h1>");
        } else {
            sb.append("\n<p>Warning summary lines are just a sum of the mappings from all mapping files.");
            sb.append("So if various sources include the same mapping it will be counted multiple times. </p>");
            sb.append("\n<p>Click on the plus in the firsts column to expand or minus to contract the table.</p>");
            MappingSetTableMaker.addTable(sb, mappingSetInfos, httpServletRequest);
        }
        footerAndEnd(sb);
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    }
    
     /**
     * @deprecated 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + WsUriConstants.GET_MAPPING_INFO)
     public Response getMappingInfo(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
            @QueryParam(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE) String targetCode, 
            @Context HttpServletRequest httpServletRequest) 
            throws BridgeDBException, UnsupportedEncodingException {
        return getSetMapping(scrCode, targetCode, null, httpServletRequest);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + Lens.METHOD_NAME) 
	public Response getLensesHtml(@QueryParam(WsUriConstants.LENS_URI)  String lensUri,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        List<Lens> lenses = getTheLens(lensUri);
        StringBuilder sb = topAndSide("Lens Summary",  httpServletRequest);
        sb.append("\n<table border=\"1\">");
        sb.append("<tr>");
        sb.append("<th>Name</th>");
        sb.append("<th>URI</th>");
        sb.append("<th>Description</th></tr>\n");
		for (Lens lens:lenses) {
            sb.append("<tr><td>");
            sb.append(lens.getName());
            sb.append("</td><td><a href=\"");
            sb.append(lens.toUri(httpServletRequest.getContextPath()));
            sb.append("\">");
            sb.append(lens.toUri(httpServletRequest.getContextPath()));
            sb.append("</a></td><td>").append(lens.getDescription()).append("</td></tr>\n");        
		}
        sb.append("</table>");
        sb.append("<p><a href=\"");
        sb.append(httpServletRequest.getContextPath());
        sb.append("/");
        sb.append(Lens.METHOD_NAME);
        sb.append(WsUriConstants.XML);
        sb.append("\">");
        sb.append("XML Format");
        sb.append("</a></p>\n");        
        footerAndEnd(sb);
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
	}
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + WsUriConstants.GRAPHVIZ)
    public Response graphvizDot(@QueryParam(WsUriConstants.LENS_URI) String lensUri) 
            throws BridgeDBException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        List<MappingSetInfo> rawProvenaceinfos = uriMapper.getMappingSetInfos(null, null, lensUri);
        SourceTargetCounter sourceTargetCounter = new SourceTargetCounter(rawProvenaceinfos);
        sb.append("digraph G {");
        for (MappingSetInfo info:sourceTargetCounter.getSummaryInfos()){
            if (info.getSource().compareTo(info.getTarget()) < 0 ){
                sb.append("\"");
                sb.append(info.getSource().getFullName());
                sb.append("\" -> \"");
                sb.append(info.getTarget().getFullName());
                sb.append("\" [dir = both, label=\"");
                sb.append(formatter.format(info.getNumberOfLinks()) + "(" + info.getStringId() + ")"); 
                sb.append("\"");
                if (info.isTransitive()){
                    sb.append(", style=dashed");
                }
                sb.append("];\n");
            }
        }
        sb.append("}"); 
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    }
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/" + WsUriConstants.MAP_BY_SET + WsUriConstants.RDF)
    public Response mapBySetRdfText(@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns,
            @QueryParam(WsUriConstants.RDF_FORMAT) String formatName
            ) throws BridgeDBException {
        MappingsBySet mappingsBySet = mapBySetInner(uris, lensUri, graph, targetUriPatterns);
        if (mappingsBySet.isEmpty()){
            return Response.noContent().build();
        } else {
            String rdf = mappingsBySet.toRDF(null, formatName);     
            return Response.ok(rdf, MediaType.TEXT_PLAIN_TYPE).build();
        }
    }
    
    private void generateTextarea(StringBuilder sb, String fieldName, String text) {
        sb.append("<p>").append(fieldName);
    	sb.append("<br/><textarea rows=\"40\" name=\"").append(fieldName)
                .append("\" style=\"width:100%; background-color: #EEEEFF;\">");
        if (text != null){
            sb.append(text);
        }
        sb.append("</textarea></p>\n");
    }
    
    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.MAP_BY_SET + WsUriConstants.RDF)
    public Response mapBySetRdfHtml(@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns,
            @QueryParam(WsUriConstants.RDF_FORMAT) String formatName,
            @Context HttpServletRequest httpServletRequest
            ) throws BridgeDBException {
        MappingsBySet mappingsBySet = mapBySetInner(uris, lensUri, graph, targetUriPatterns);
        StringBuilder sb = topAndSide("HTML friendly " + WsUriConstants.MAP_BY_SET + WsUriConstants.RDF + " Output",  httpServletRequest);
        sb.append("<h2>Warning unlike ");
        sb.append(WsUriConstants.MAP_BY_SET);
        sb.append(" this method does not include any protential mapping to self.</h2>");
        sb.append("<h4>Use MediaType.TEXT_PLAIN to remove HTML stuff</h4>");
        sb.append("<p>Warning MediaType.TEXT_PLAIN version returns status 204 if no mappings found.</p>");
        generateTextarea(sb, "RDF", mappingsBySet.toRDF(null, formatName));
        footerAndEnd(sb);
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    }

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// TODO Auto-generated method stub
		
	}
    /**
     * Listen for servlet initialization in web.xml and set the context for use in
     * the velocity templates
     */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		this.context = servletContextEvent.getServletContext();
	}

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/" + WsUriConstants.MAP_BY_SET)
    public Response mapBySetHtml(@QueryParam(WsUriConstants.URI) List<String> uris,
     		@QueryParam(WsUriConstants.LENS_URI) String lensUri,
            @QueryParam(WsUriConstants.GRAPH) String graph,
            @QueryParam(WsUriConstants.TARGET_URI_PATTERN) List<String> targetUriPatterns,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        Response result = mapBySet(uris, lensUri, graph, targetUriPatterns);
        if (result.getStatus() == Response.Status.NO_CONTENT.getStatusCode()){
            return noContectWrapper(httpServletRequest);
        }
        return result;
    }

    private Response noContectWrapper(HttpServletRequest httpServletRequest) throws BridgeDBException {
        StringBuilder sb = topAndSide ("Empty Reply", httpServletRequest);
        sb.append("<h1>Reply is an Empty Set or Empty Object</h1>\n");
        sb.append("<h2>Note: The XML and Json versions of this request simply return status 204 (No Context)</h2>");
        footerAndEnd(sb);
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
   }


}


