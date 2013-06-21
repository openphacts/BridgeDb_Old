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
import java.net.URLEncoder;
import java.util.ArrayList;
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
import org.bridgedb.Xref;
import org.bridgedb.rdf.RdfConfig;
import org.bridgedb.statistics.DataSetInfo;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.Lens;
import org.bridgedb.uri.Mapping;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WsConstants;
import org.bridgedb.ws.WsUriConstants;
import org.bridgedb.ws.bean.MappingBean;

/**
 * This class provides the Reposnse Frame including Top and Sidebar 
 * 
 * @author Christian
 */
public class WSOtherservices extends WSFrame {
            
    private static final String ID_CODE = "id_code";
    private static final String FIRST_ID_PARAMETER = "?" + WsConstants.ID + "=";
    private static final String ID_PARAMETER = "&" + WsConstants.ID + "=";
    private static final String DATASOURCE_SYSTEM_CODE_PARAMETER = "&" + WsConstants.DATASOURCE_SYSTEM_CODE + "=";
    private final static String FIRST_SOURCE_PARAMETER = "?" + WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE + "=";
    private final static String TARGET_PARAMETER = "&" + WsConstants.TARGET_DATASOURCE_SYSTEM_CODE + "=";
    private final static String FIRST_TEXT_PARAMETER = "?" + WsConstants.TEXT + "=";
    private final static String LIMIT5_PARAMETER = "&" + WsConstants.LIMIT + "=5";
    private final static String FIRST_URI_PARAMETER = "?" + WsUriConstants.URI + "=";
    private final static String TARGET_DATASOURCE_SYSTEM_CODE_PARAMETER = "&" + WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE + "=";
    private final static String TARGET_URI_PATTERN_PARAMETER = "&" + WsUriConstants.TARGET_URI_PATTERN + "=";
    
    private String apiString = null;
    
    static final Logger logger = Logger.getLogger(WSOtherservices.class);

    public WSOtherservices()  throws BridgeDBException   {
        super();
    }
        
    /**
     * API page for the IMS methods.
     * 
     * Warning may not be completely up to date.
     * 
     * @param httpServletRequest
     * @return
     * @throws BridgeDBException
     * @throws UnsupportedEncodingException 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + WsUriConstants.BRIDGEDB_API)
    public Response imsApiPage(@Context HttpServletRequest httpServletRequest) throws BridgeDBException, UnsupportedEncodingException {
        if (apiString == null){
            StringBuilder sb = new StringBuilder();
            showSummary(sb);
            showParameters(sb);
            showMethods(sb);
            sb.append("</body></html>");
            apiString = sb.toString();
        }
        StringBuilder sb = topAndSide("Api",  httpServletRequest);
        return Response.ok(sb.toString() + apiString, MediaType.TEXT_HTML).build();
    }
    
    private void showSummary(StringBuilder sb) {
        sb.append("<h2>Support services include:<h2>");
        sb.append("<dl>");      
        introduce(sb);
        sb.append("</dl>");
        sb.append("</p>");
    }

    private void introduce(StringBuilder sb) {
        boolean freeSearchSupported = idMapper.getCapabilities().isFreeSearchSupported(); 
        Set<String> keys = idMapper.getCapabilities().getKeys();

        introduce_IDMapper(sb, freeSearchSupported);
        introduce_IDMapperCapabilities(sb, keys, freeSearchSupported);     
        introduce_URIMapper(sb, freeSearchSupported);
        introduce_Info(sb);
    }

    private void showParameters(StringBuilder sb) throws BridgeDBException {
        sb.append("<h2>Parameters </h2>");
        sb.append("The following parametes may be applicable to the methods. ");
        sb.append("See the indiviual method description for which are required and which are optional.");
        sb.append("Their behaviour is consitant across all methods.");
        describeParameter(sb);
    }

    private void showMethods(StringBuilder sb) throws BridgeDBException, UnsupportedEncodingException {
        Mapping mapping1 = uriMapper.getMapping(1);
       // DataSource dataSource1 = DataSource.getBySystemCode(mapping1.getSourceSysCode());
        Xref sourceXref1 = mapping1.getSource();
        String sourceSysCode1 = sourceXref1.getDataSource().getSystemCode();
        String sourceUri1 = mapping1.getSourceUri().iterator().next();
        String tragetSysCode1 = mapping1.getTarget().getDataSource().getSystemCode();
        String text1 = sourceXref1.getId();

        Mapping mapping2 = uriMapper.getMapping(2);
        Xref sourceXref2 =  mapping2.getSource();
        String sourceUri2 = mapping2.getSourceUri().iterator().next();
        String targetUri2 = mapping2.getTargetUri().iterator().next();    
        String targetUriSpace2 = targetUri2.substring(0, targetUri2.length()-sourceXref2.getId().length());
                
        boolean freeSearchSupported = idMapper.getCapabilities().isFreeSearchSupported(); 
        Set<String> keys = idMapper.getCapabilities().getKeys();

        describe_IDMapper(sb, sourceXref1, tragetSysCode1, sourceXref2, freeSearchSupported);
        describe_IDMapperCapabilities(sb, sourceXref1, tragetSysCode1, keys, freeSearchSupported);
        describe_UriMapper(sb, sourceXref1, sourceUri1, sourceXref2, sourceUri2, targetUriSpace2, 
                text1, 1, sourceSysCode1, freeSearchSupported);
        describe_Info(sb, sourceXref1, sourceSysCode1, tragetSysCode1);
        describe_Graphviz(sb);      
    }
    
    protected void describeParameter(StringBuilder sb) throws BridgeDBException  {
        describeCoreParameter(sb);
        describeUriParameter(sb);
    }
    
    private void describeCoreParameter(StringBuilder sb) {
        sb.append("<h3>BridgeDB Parameters</h3>");
        sb.append("<ul>");
        sb.append("<dt><a name=\"id_code\">");
                sb.append(WsConstants.ID);
                sb.append(" and ");
                sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Limits the results to ones with this/these Xrefs.</li>");
            sb.append("<li>");
                    sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                    sb.append(" is the SystemCode of the Xref's DataSource)</li>");
            sb.append("<li>");
                    sb.append(WsConstants.ID);
                    sb.append(" is the identifier part of the Xref</li>");
            sb.append("<li>Typically There can be multiple \"");
                    sb.append(WsConstants.ID);
                    sb.append("\" and \"");
                    sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                    sb.append("\" values</li>");
                sb.append("<ul>");
                sb.append("<li>There must be at least one of each.</li>");                
                sb.append("<li>There must be the same number of each.</li>");                
                sb.append("<li>They will be paired by order.</li>");                
                sb.append("<li>If multiple Xref's have the same DataSource their code must be repeated.</li>");                
                sb.append("</ul>");
            sb.append("</ul>");           
            sb.append("<li>Note: Other methods may obtain a different ");           
                    sb.append(WsConstants.ID);           
                    sb.append(" by following the method name with a slash // ");
                    sb.append(WsConstants.ID);
                    sb.append(". These do not require a \"");
                    sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                    sb.append("\"</li>");
        sb.append("<dt><a name=\"key\">key</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Selects which property to return.</li>");
            sb.append("<li>Only one key parameter is supported.</li>");
            sb.append("</ul>");
        sb.append("<dt><a name=\"");
                sb.append(WsConstants.LIMIT);
                sb.append("\">");
                sb.append(WsConstants.LIMIT);
                sb.append("</a></dt>");
                sb.append("<ul>");
            sb.append("<li>Limits the number of results.</li>");
            sb.append("<li>Must be a positive Integer in String Format</li>");
            sb.append("<li>If less than ");
                    sb.append(WsConstants.LIMIT);
                    sb.append("results are availabe ");
                    sb.append(WsConstants.LIMIT);
                    sb.append(" will have no effect.</li>");
            sb.append("<li>Only one ");
                    sb.append(WsConstants.LIMIT);
                    sb.append(" parameter is supported.</li>");
            sb.append("<li>If no ");
                    sb.append(WsConstants.LIMIT);
                    sb.append(" is set a default ");
                    sb.append(WsConstants.LIMIT);
                    sb.append(" will be used.</li>");
            sb.append("<li>If too high a ");
                    sb.append(WsConstants.LIMIT);
                    sb.append(" is set the default ");
                    sb.append(WsConstants.LIMIT);
                    sb.append(" will be used.</li>");
            sb.append("<li>To obtain a full data dump please contact the admins.</li>");
            sb.append("</ul>");
        sb.append("<dt><a name=\"");
                sb.append(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                sb.append("\">");
                sb.append(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Limits the results to ones with those Source Xref's DataSource has this sysCode.</li>");
            sb.append("<li>String Format</li>");
            sb.append("<li>Typically there must be exactly one ");
                    sb.append(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                    sb.append(" when used..</li>");
            sb.append("</ul>");
        sb.append("<dt><a name=\"");
                sb.append(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append("\">");
                sb.append(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Limits the results to ones with those Target Xref's DataSource has this sysCode.</li>");
            sb.append("<li>String Format</li>");
            sb.append("<li>Typically there can but need not be more than one.</li>");
            sb.append("</ul>");
         sb.append("<dt><a name=\"");
                    sb.append(WsConstants.TEXT);
                    sb.append("\">");
                    sb.append(WsConstants.TEXT);
                    sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>A bit of text that will be searched for.</li>");
            sb.append("<li>String Format</li>");
            sb.append("<li>Only one text parameter is supported.</li>");
            sb.append("<li>Note this is for searching for text in Identifiers not for mapping between text and Identifiers.</li>");
            sb.append("</ul>");      
        sb.append("</ul>");
   }

   private void describeUriParameter(StringBuilder sb) throws BridgeDBException {
        sb.append("<h3>Ops Exstension Parameters</h3>");
        sb.append("<ul>");
        sb.append("<dt><a name=\"");
                sb.append(WsUriConstants.URI);
                sb.append("\">");
                sb.append(WsUriConstants.URI);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Limits the results to ones with this URI.</li>");
            sb.append("<li>String Format</li>");
            sb.append("<li>Do NOT include the @gt and @lt seen arround URIs in RDF</li>");
            sb.append("<li>Only one ");
                    sb.append(WsUriConstants.URI);
                    sb.append(" parameters is supported.</li>");
            sb.append("</ul>");
        sb.append("<dt><a name=\"");
                sb.append(WsUriConstants.LENS_URI);
                sb.append("\">");
                sb.append(WsUriConstants.LENS_URI);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>If not provided the default lens is used.</li>");
            sb.append("<li>To See a list and description of all lenses visit.</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(Lens.METHOD_NAME);
                    sb.append("\">");
                    sb.append(Lens.METHOD_NAME);
                    sb.append("</a></li>");    
                sb.append("</ul>");
            sb.append("<li>While the current API includes this parameter there is not yet any lens based data.</li>");
            sb.append("<li>It it not recommended to use this parameter except for testing until farther notice.</li>");
            sb.append("</ul>");        
        sb.append("<dt><a name=\"");
                sb.append(WsUriConstants.TARGET_URI_PATTERN);
                sb.append("\">");
                sb.append(WsUriConstants.TARGET_URI_PATTERN);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Limits the results to ones with URIs with this pattern.</li>");
            sb.append("<li>The URISpace of a URI is one defined when the mapping is loaded, not any with which the URI startWith.</li>");
            sb.append("<li>String Format</li>");
            sb.append("<li>Do NOT include the @gt and @lt seen arround URIs in RDF</li>");
            sb.append("<li>Typically there can but need not be more than one.</li>");
            sb.append("</ul>");
         sb.append("<dt><a name=\"");
                sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append("\">");
                sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append("</a></dt>");
            sb.append("<ul>");
            sb.append("<li>Acts in exactly the same way as non URI based methods.</li>");
            sb.append("<li>Note: If both ");
                sb.append(WsUriConstants.TARGET_URI_PATTERN);
                sb.append(" and  ");
                sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append(" are specified the result is the union of results of running this method twice with each paramteter individually.");
            sb.append("<li>String Format</li>");
            sb.append("<li>Do NOT include the @gt and @lt seen arround URIs in RDF</li>");
            sb.append("<li>Typically there can but need not be more than one.</li>");
            sb.append("</ul>");
         sb.append("</ul>");
   }

   protected final void introduce_IDMapper(StringBuilder sb, boolean freeSearchSupported) {
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.MAP_ID);
                sb.append("\">");
                sb.append(WsConstants.MAP_ID);
                sb.append("</a></dt>");
        sb.append("<dd>List the Xrefs that map to these Xrefs</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.XREF_EXISTS);
                sb.append("\">");
                sb.append(WsConstants.XREF_EXISTS);
                sb.append("</a></dt>");
        sb.append("<dd>State if the Xref is know to the Mapping Service or not</dd>");   
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.FREE_SEARCH);
                sb.append("\">");
                sb.append(WsConstants.FREE_SEARCH);
                sb.append("</a></dt>");
        if (freeSearchSupported){
            sb.append("<dd>Searches for Xrefs that have this id.</dd>");
        } else {
            sb.append("<dd>This is currently not supported.</dd>");      
        }
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.GET_CAPABILITIES);
                sb.append("\">");
                sb.append(WsConstants.GET_CAPABILITIES);
                sb.append("</a></dt>");
        sb.append("<dd>Gives the Capabilitles as defined by BridgeDB.</dd>");
        sb.append("<dt>Close()</a></dt>");
        sb.append("<dd>Not supported as clients should not be able to close the server.</dd>");
        sb.append("<dt>isConnected</dt>");
        sb.append("<dd>Not supported as Close() is not allowed</dd>");
    }

    protected void describe_IDMapper(StringBuilder sb, Xref sourceXref1, String tragetSysCode1, Xref sourceXref2,
            boolean freeSearchSupported) throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h2>Implementations of BridgeDB's IDMapper methods</h2>");

        describe_mapID(sb, sourceXref1, tragetSysCode1, sourceXref2);    
        describe_xrefExists(sb, sourceXref1);
        if (freeSearchSupported){
            describe_freeSearch(sb, sourceXref1);
        }
        describe_getCapabilities(sb); 
        sb.append("<h3>Other IDMapper Functions</h3>");
        sb.append("<dl>");
        sb.append("<dt>Close()</a></dt>");
        sb.append("<dd>Not supported as clients should not be able to close the server.</dd>");
        sb.append("<dt>isConnected</dt>");
        sb.append("<dd>Not supported as Close() is not allowed</dd>");
        sb.append("</dl>");
    }
    
    private void describe_mapID(StringBuilder sb, Xref sourceXref1, String tragetSysCode1, Xref sourceXref2) 
            throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h3><a name=\"");
                    sb.append(WsConstants.MAP_ID);
                    sb.append("\">");
                    sb.append(WsConstants.MAP_ID);
                    sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>List the Xrefs that map to these Xrefs</li>");
            sb.append("<li>Implements:  Map&ltXref, Set&ltXref&gt&gt mapID(Collection&ltXref&gt srcXrefs, DataSource... tgtDataSources)</li>");
            sb.append("<li>Implements:  Set&ltXref&gt mapID(Xref srcXrefs, DataSource... tgtDataSources)</li>");
            sb.append("<li>Required arguements: (Only Source Xref considered)</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(ID_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.ID);
                        sb.append("</a></li>");
                sb.append("<li><a href=\"#");
                        sb.append(ID_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li>");
                sb.append("</ul>");
            sb.append("<li>Optional arguments</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li> ");
                sb.append("</ul>");        
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                    StringBuilder front = new StringBuilder(WsConstants.MAP_ID);
                    StringBuilder sbInnerPure = new StringBuilder(WsConstants.MAP_ID);
                    StringBuilder sbInnerEncoded = new StringBuilder(WsConstants.MAP_ID);
                    sbInnerPure.append(FIRST_ID_PARAMETER);
                    sbInnerEncoded.append(FIRST_ID_PARAMETER);
                    sbInnerPure.append(sourceXref1.getId());
                    sbInnerEncoded.append(sourceXref1.getId());
                    sbInnerPure.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerEncoded.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerPure.append(sourceXref1.getDataSource().getSystemCode());
                    sbInnerEncoded.append(URLEncoder.encode(sourceXref1.getDataSource().getSystemCode(), "UTF-8"));
                    sbInnerPure.append(ID_PARAMETER);
                    sbInnerEncoded.append(ID_PARAMETER);
                    sbInnerPure.append(sourceXref2.getId());
                    sbInnerEncoded.append(URLEncoder.encode(sourceXref2.getId(), "UTF-8"));
                    sbInnerPure.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerEncoded.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerPure.append(sourceXref2.getDataSource().getSystemCode());
                    sbInnerEncoded.append(URLEncoder.encode(sourceXref2.getDataSource().getSystemCode(), "UTF-8"));
                    sb.append(sbInnerEncoded.toString());
                    sb.append("\">");
                    sb.append(sbInnerPure.toString());
                    sb.append("</a></li>");    
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                    String targetPart = "&" + WsConstants.TARGET_DATASOURCE_SYSTEM_CODE + "=";
                    sbInnerPure.append(targetPart);
                    sbInnerEncoded.append(targetPart);
                    sbInnerPure.append(tragetSysCode1);
                    sbInnerEncoded.append(URLEncoder.encode(tragetSysCode1, "UTF-8"));
                    sb.append(sbInnerEncoded.toString());
                    sb.append("\">");
                    sb.append(sbInnerPure.toString());
                    sb.append("</a></li>");                    
            sb.append("</ul>");
    }
    
    private void describe_xrefExists(StringBuilder sb, Xref xref1) throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h3><a name=\"");
                sb.append(WsConstants.XREF_EXISTS);
                sb.append("\">");
                sb.append(WsConstants.XREF_EXISTS);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  boolean xrefExists(Xref xref)</li>");
            sb.append("<li>State if the Xref is know to the Mapping Service or not</li>");
            sb.append("<li>Required arguements: (Considers both Source and target Xrefs</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(ID_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.ID);
                        sb.append("</a></li>");
                sb.append("<li><a href=\"#");
                        sb.append(ID_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li>");
                sb.append("<li>Currently only a single ");
                        sb.append(WsConstants.ID);
                        sb.append(" and single ");
                        sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                        sb.append(" supported.</li>");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.XREF_EXISTS);
                    sb.append(FIRST_ID_PARAMETER);
                    sb.append(URLEncoder.encode(xref1.getId(), "UTF-8"));
                    sb.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sb.append(URLEncoder.encode(xref1.getDataSource().getSystemCode(), "UTF-8"));
                    sb.append("\">");
                    sb.append(WsConstants.XREF_EXISTS);
                    sb.append(FIRST_ID_PARAMETER);
                    sb.append(xref1.getId());
                    sb.append(FIRST_ID_PARAMETER);
                    sb.append(xref1.getDataSource().getSystemCode());
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
    private void describe_freeSearch(StringBuilder sb, Xref xref1) throws UnsupportedEncodingException, BridgeDBException{
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.FREE_SEARCH);
                sb.append("\">");
                sb.append(WsConstants.FREE_SEARCH);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Searches for Xrefs that have this id.</li>");
            sb.append("<li>Implements:  Set@ltXref@gt freeSearch (String text, int limit)</li>");
            sb.append("<li>Required arguements:</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsConstants.TEXT);
                        sb.append("\">");
                        sb.append(WsConstants.TEXT);
                        sb.append("</a></li>");
                sb.append("<li><a href=\"#");
                        sb.append(WsConstants.LIMIT);
                        sb.append("\">");
                        sb.append(WsConstants.LIMIT);
                        sb.append("</a> (default available)</li>");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.FREE_SEARCH);
                    sb.append(FIRST_TEXT_PARAMETER);
                    sb.append(URLEncoder.encode(xref1.getId(), "UTF-8"));
                    sb.append(LIMIT5_PARAMETER);
                    sb.append("\">");
                    sb.append(WsConstants.FREE_SEARCH);
                    sb.append(FIRST_TEXT_PARAMETER);
                    sb.append(xref1.getId());
                    sb.append(LIMIT5_PARAMETER);
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
  protected final void introduce_IDMapperCapabilities(StringBuilder sb, Set<String> keys, boolean freeSearchSupported) {
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.IS_FREE_SEARCH_SUPPORTED);
                sb.append("\">");
                sb.append(WsConstants.IS_FREE_SEARCH_SUPPORTED);
                sb.append("</a></dt>");
        if (freeSearchSupported){
            sb.append("<dd>Returns True as freeSearch and URLSearch are supported.</dd>");
        } else {
            sb.append("<dd>Returns False because underlying IDMappper does not support freeSearch or URLSearch.</dd>");                
        }        
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES);
                sb.append("\">");
                sb.append(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES);
                sb.append("</a></dt>");
        sb.append("<dd>Returns Supported Source (BridgeDB)DataSource(s).</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES);
                sb.append("\">");
                sb.append(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES);
                sb.append("</a></dt>");
        sb.append("<dd>Returns Supported Target (BridgeDB)DataSource(s).</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.IS_MAPPING_SUPPORTED);
                sb.append("\">");
                sb.append(WsConstants.IS_MAPPING_SUPPORTED);
                sb.append("</a></dt>");
        sb.append("<dd>States if two DataSources are mapped at least once.</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.PROPERTY);
                sb.append("\">");
                sb.append(WsConstants.PROPERTY);
                sb.append("/key</a></dt>");
        if (keys.isEmpty()){
            sb.append("<dd>There are currently no properties supported.</dd>");
        } else {
            sb.append("<dd>Returns The property value for this key.</dd>");
        }
        sb.append("<dt><a href=\"#");
                sb.append(WsConstants.GET_KEYS);
                sb.append("\">");
                sb.append(WsConstants.GET_KEYS);
                sb.append("</a></dt>");
        if (keys.isEmpty()){
            sb.append("<dd>There are currently no properties supported.</dd>");
        } else {
            sb.append("<dd>Returns The keys and their property value.</dd>");
        }
    }
  
    private void describe_getCapabilities(StringBuilder sb) throws BridgeDBException {
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.GET_CAPABILITIES);
                sb.append("\">");
                sb.append(WsConstants.GET_CAPABILITIES);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  IDMapperCapabilities getCapabilities()</li>");
            sb.append("<li>Gives the Capabilitles as defined by BridgeDB.</li>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.GET_CAPABILITIES);
                    sb.append("\">");
                    sb.append(WsConstants.GET_CAPABILITIES);
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
    protected void describe_IDMapperCapabilities(StringBuilder sb, Xref xref1, String tragetSysCode1, Set<String> keys, 
            boolean freeSearchSupported) throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h2>Implementations of BridgeDB's IDMapperCapabilities methods</h2>");
        describe_isFreeSearchSupported(sb, freeSearchSupported);
        describe_getSupportedDataSources(sb);
        describe_isMappingSupported(sb, xref1, tragetSysCode1); 
        describe_getProperty(sb, keys);            
        describe_getKeys(sb, keys);
    }
    
    private void describe_isFreeSearchSupported(StringBuilder sb, boolean freeSearchSupported) throws BridgeDBException {
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.IS_FREE_SEARCH_SUPPORTED);
                sb.append("\">");
                sb.append(WsConstants.IS_FREE_SEARCH_SUPPORTED);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  boolean isFreeSearchSupported()</li>");
            if (freeSearchSupported){
                sb.append("<li>Returns True as freeSearch and URISearch are supported.</li>");
            } else {
                sb.append("<li>Returns False because underlying IDMappper does not support freeSearch or URISearch.</li>");                
            }
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.IS_FREE_SEARCH_SUPPORTED);
                    sb.append("\">");
                    sb.append(WsConstants.IS_FREE_SEARCH_SUPPORTED);
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
    private void describe_getSupportedDataSources(StringBuilder sb) throws BridgeDBException {
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES);
                sb.append("\">");
                sb.append(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  Set&ltDataSource&gt  getSupportedSrcDataSources()</li>");
            sb.append("<li>Returns Supported Source (BridgeDB)DataSource(s).</li>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES);
                    sb.append("\">");
                    sb.append(WsConstants.GET_SUPPORTED_SOURCE_DATA_SOURCES);
                    sb.append("</a></li>");    
            sb.append("</ul>");
          
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES);
                sb.append("\">");
                sb.append(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  Set&ltDataSource&gt  getSupportedTgtDataSources()</li>");
            sb.append("<li>Returns Supported Target (BridgeDB)DataSource(s).</li>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES);
                    sb.append("\">");
                    sb.append(WsConstants.GET_SUPPORTED_TARGET_DATA_SOURCES);
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
    private void describe_isMappingSupported(StringBuilder sb, Xref sourceXref1, String targetSysCode) 
            throws UnsupportedEncodingException, BridgeDBException{
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.IS_MAPPING_SUPPORTED);
                sb.append("\">");
                sb.append(WsConstants.IS_MAPPING_SUPPORTED);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>States if two DataSources are mapped at least once.</li>");
            sb.append("<li>Implements:  boolean isMappingSupported(DataSource src, DataSource tgt)</li>");
            sb.append("<li>Required arguements: (One of each)</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li> ");
                sb.append("<li><a href=\"#");
                        sb.append(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li> ");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsConstants.IS_MAPPING_SUPPORTED);
                    sb.append(FIRST_SOURCE_PARAMETER);
                    sb.append(sourceXref1.getDataSource().getSystemCode());
                    sb.append(TARGET_PARAMETER);
                    sb.append(targetSysCode);
                    sb.append("\">");
                    sb.append(WsConstants.IS_MAPPING_SUPPORTED);
                    sb.append(FIRST_SOURCE_PARAMETER);
                    sb.append(URLEncoder.encode(sourceXref1.getDataSource().getSystemCode(), "UTF-8"));
                    sb.append(TARGET_PARAMETER);
                    sb.append(URLEncoder.encode(targetSysCode, "UTF-8"));
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }

    private void describe_getProperty(StringBuilder sb, Set<String> keys) 
            throws UnsupportedEncodingException, BridgeDBException{
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.PROPERTY);
                sb.append("\">");
                sb.append(WsConstants.PROPERTY);
                sb.append("/key</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  String getProperty(String key)</li>");
            sb.append("<li>Returns The property value for this key.</li>");
            sb.append("<li>Required arguements:</li>");
                sb.append("<ul>");
                sb.append("<li>Place the actual key after the /</li> ");
                sb.append("</ul>");
            if (keys.isEmpty()){
                sb.append("<li>There are currently no properties supported</li>");
            } else {
                sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                        sb.append(WsConstants.PROPERTY);
                        sb.append("/");
                        sb.append(keys.iterator().next());
                        sb.append("\">");
                        sb.append(WsConstants.PROPERTY);
                        sb.append("/");
                        sb.append(URLEncoder.encode(keys.iterator().next(), "UTF-8"));
                        sb.append("</a></li>");    
            }
            sb.append("</ul>");
    }
    
    private void describe_getKeys(StringBuilder sb, Set<String> keys) throws BridgeDBException{
         sb.append("<h3><a name=\"");
                sb.append(WsConstants.GET_KEYS);
                sb.append("\">");
                sb.append(WsConstants.GET_KEYS);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Implements:  Set<String> getKeys()</li>");
            sb.append("<li>Returns The keys and their property value.</li>");
            if (keys.isEmpty()){
                sb.append("<li>There are currently no properties supported</li>");
            } else {
                sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                        sb.append(WsConstants.GET_KEYS);
                        sb.append("\">");
                        sb.append(WsConstants.GET_KEYS);
                        sb.append("</a></li>");    
            }
            sb.append("</ul>");
    }

        protected final void introduce_URIMapper(StringBuilder sb, boolean freeSearchSupported) {
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.MAP);
                sb.append("\">");
                sb.append(WsUriConstants.MAP);
                sb.append("</a></dt>");
        sb.append("<dd>List the full mappings to this URI (or Xref)</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.MAP_URI);
                sb.append("\">");
                sb.append(WsUriConstants.MAP_URI);
                sb.append("</a></dt>");
        sb.append("<dd>List the URIs that map to this/these URIs</dd>");
         sb.append("<dt>");
                sb.append(WsUriConstants.MAP_URL);
                sb.append("</a></dt>");
        sb.append("<dd>DEPRICATED: Forwards call to");
        sb.append(WsUriConstants.MAP);
        sb.append("</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.URI_EXISTS);
                sb.append("\">");
                sb.append(WsUriConstants.URI_EXISTS);
                sb.append("</a></dt>");
        sb.append("<dd>State if the URI is know to the Mapping Service or not</dd>");
        if (freeSearchSupported){
            sb.append("<dt><a href=\"#");
                    sb.append(WsUriConstants.URI_SEARCH);
                    sb.append("\">");
                    sb.append(WsUriConstants.URI_SEARCH);
                    sb.append("</a></dt>");
            sb.append("<dd>Searches for URIs that have this ending.</dd>");    
        } else {
            sb.append("<dt>");
                    sb.append(WsUriConstants.URI_SEARCH);
                    sb.append("</a></dt>");

            sb.append("<dd>This is currently not supported.</dd>");            
        }
        ///toXref
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.MAPPING);
                sb.append("\">");
                sb.append(WsUriConstants.MAPPING);
                sb.append("</a></dt>");
        sb.append("<dd>Returns the mapping for with the specific id</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.DATA_SOURCE);
                sb.append("\">");
                sb.append(WsUriConstants.DATA_SOURCE);
                sb.append("</a></dt>");
        sb.append("<dd>Returns the DataSource and associated UriSpace(s) with a specific id</dd>");
    }
    
    protected final void describe_UriMapper(StringBuilder sb, Xref sourceXref1, String sourceUri1, Xref sourceXref2, 
            String sourceUri2, String targetUriSpace2, String text, int mappingId, String sysCode, boolean freeSearchSupported) 
            throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h2>URI based methods</h2>");
        describe_map(sb, sourceXref1, sourceUri1, sourceXref2, sourceUri2, targetUriSpace2);
        describe_mapUri(sb, sourceUri1, sourceUri2, targetUriSpace2);
        describe_uriExists(sb, sourceUri1);
        if (freeSearchSupported) {
            describe_uriSearch(sb, text); 
        }
        describe_mapping(sb, mappingId);
        describe_dataSource(sb, sysCode);
    }
        
    private void describe_map(StringBuilder sb, Xref sourceXref1, String sourceUri1, Xref sourceXref2, 
            String sourceUri2, String targetUriSpace2) throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.MAP);
                sb.append("\">");
                sb.append(WsUriConstants.MAP);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>List the full mappings to this URI or Xref</li>");
            sb.append("<li>WARNING: Providing both URI and Xref parameters always causes an Exception. Even if they match!</li>");
            sb.append("<li>Note: it is not recommened to use both <a href=\"#");
                sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append("\">");
                sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                sb.append("</a> and <a href=\"#");
                sb.append(WsUriConstants.TARGET_URI_PATTERN);
                sb.append("\">");
                sb.append(WsUriConstants.TARGET_URI_PATTERN);
                sb.append("</a>. If both are supplied the result is the union of the calls with each individually.</li> ");
            sb.append("<li>Required arguements:</li>");
                sb.append("<ul>");
                sb.append("<li>URI based</li>");
                sb.append("<ul>");
                    sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.URI);
                        sb.append("\">");
                        sb.append(WsUriConstants.URI);
                        sb.append("</a></li>");
                    sb.append("</ul>");
                sb.append("<li>Xref based</li>");
                sb.append("<ul>");
                    sb.append("<li><a href=\"#");
                        sb.append(ID_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.ID);
                        sb.append("</a></li>");
                    sb.append("<li><a href=\"#");
                        sb.append(ID_CODE);
                        sb.append("\">");
                        sb.append(WsConstants.DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li>");
                   sb.append("</ul>");
                sb.append("</ul>");
            sb.append("<li>Optional arguments</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.LENS_URI);
                        sb.append("\">");
                        sb.append(WsUriConstants.LENS_URI);
                        sb.append("</a></li> ");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.TARGET_URI_PATTERN);
                        sb.append("\">");
                        sb.append(WsUriConstants.TARGET_URI_PATTERN);
                        sb.append("</a></li> ");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("\">");
                        sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li> ");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri1, "UTF-8"));
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri1);
                sb.append("</a></li>");    
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri2, "UTF-8"));
                sb.append(TARGET_URI_PATTERN_PARAMETER);
                sb.append(URLEncoder.encode(targetUriSpace2, "UTF-8"));
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri2);
                sb.append(TARGET_URI_PATTERN_PARAMETER);
                sb.append(targetUriSpace2);
                sb.append("</a></li>");    
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                    StringBuilder front = new StringBuilder(WsConstants.MAP_ID);
                    StringBuilder sbInnerPure = new StringBuilder(WsConstants.MAP_ID);
                    StringBuilder sbInnerEncoded = new StringBuilder(WsConstants.MAP_ID);
                    sbInnerPure.append(FIRST_ID_PARAMETER);
                    sbInnerEncoded.append(FIRST_ID_PARAMETER);
                    sbInnerPure.append(sourceXref1.getId());
                    sbInnerEncoded.append(sourceXref1.getId());
                    sbInnerPure.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerEncoded.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerPure.append(sourceXref1.getDataSource().getSystemCode());
                    sbInnerEncoded.append(URLEncoder.encode(sourceXref1.getDataSource().getSystemCode(), "UTF-8"));
                    sbInnerPure.append(ID_PARAMETER);
                    sbInnerEncoded.append(ID_PARAMETER);
                    sbInnerPure.append(sourceXref2.getId());
                    sbInnerEncoded.append(URLEncoder.encode(sourceXref2.getId(), "UTF-8"));
                    sbInnerPure.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerEncoded.append(DATASOURCE_SYSTEM_CODE_PARAMETER);
                    sbInnerPure.append(sourceXref2.getDataSource().getSystemCode());
                    sbInnerEncoded.append(URLEncoder.encode(sourceXref2.getDataSource().getSystemCode(), "UTF-8"));
                    sb.append(sbInnerEncoded.toString());
                    sb.append("\">");
                    sb.append(sbInnerPure.toString());
                    sb.append("</a></li>");    
            sb.append("<li>Default Lens: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri1, "UTF-8"));
                sb.append("&");
                sb.append(WsUriConstants.LENS_URI);
                sb.append("=");
                sb.append(Lens.getDefaultLens());
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri1);
                sb.append("&");
                sb.append(WsUriConstants.LENS_URI);
                sb.append("=");
                sb.append(Lens.getDefaultLens());
                sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
    private void describe_mapUri(StringBuilder sb, String sourceUri1, String sourceUri2, String targetUriSpace2) 
            throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.MAP_URI);
                sb.append("\">");
                sb.append(WsUriConstants.MAP_URI);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>List the URIs that map to this URI(s)</li>");
            sb.append("<li>Required arguements:</li>");
                sb.append("<ul>");
                 sb.append("<ul>");
                    sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.URI);
                        sb.append("\">");
                        sb.append(WsUriConstants.URI);
                        sb.append("</a></li>");
                    sb.append("<ul><li>In Contrast to other methods multiple values may be provided</li></ul>");  
                    sb.append("</ul>");
            sb.append("<li>Optional arguments</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.LENS_URI);
                        sb.append("\">");
                        sb.append(WsUriConstants.LENS_URI);
                        sb.append("</a></li> ");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.TARGET_URI_PATTERN);
                        sb.append("\">");
                        sb.append(WsUriConstants.TARGET_URI_PATTERN);
                        sb.append("</a></li> ");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri1, "UTF-8"));
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri1);
                sb.append("</a></li>");    
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri1, "UTF-8"));
                sb.append("&");
                sb.append(WsUriConstants.URI);
                sb.append("=");
                sb.append(URLEncoder.encode(sourceUri2, "UTF-8"));
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri1);
                sb.append("&");
                sb.append(WsUriConstants.URI);
                sb.append("=");
                sb.append(sourceUri2);
                sb.append("</a></li>");    
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri2, "UTF-8"));
                sb.append(TARGET_URI_PATTERN_PARAMETER);
                sb.append(URLEncoder.encode(targetUriSpace2, "UTF-8"));
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri2);
                sb.append(TARGET_URI_PATTERN_PARAMETER);
                sb.append(targetUriSpace2);
                sb.append("</a></li>");    
            sb.append("<li>Default Lens: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(URLEncoder.encode(sourceUri1, "UTF-8"));
                sb.append("&");
                sb.append(WsUriConstants.LENS_URI);
                sb.append("=");
                sb.append(Lens.getDefaultLens());
                sb.append("\">");
                sb.append("\">");
                sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.MAP_URI);
                sb.append(FIRST_URI_PARAMETER);
                sb.append(sourceUri1);
                sb.append("&");
                sb.append(WsUriConstants.LENS_URI);
                sb.append("=");
                sb.append(Lens.getDefaultLens());
                sb.append("\">");
                sb.append("</a></li>");    
            sb.append("</ul>");
    }           

    private void describe_uriExists(StringBuilder sb, String uri) throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.URI_EXISTS);
                sb.append("\">");
                sb.append(WsUriConstants.URI_EXISTS);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>State if the URI is know to the Mapping Service or not</li>");
            sb.append("<li>Required arguements:</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.URI);
                        sb.append("\">");
                        sb.append(WsUriConstants.URI);
                        sb.append("</a></li>");
                sb.append("<ul>");
                sb.append("<li>Currently limited to single URI</li>");
                sb.append("</ul>");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.URI_EXISTS);
                    sb.append(FIRST_URI_PARAMETER);
                    sb.append(URLEncoder.encode(uri, "UTF-8"));
                    sb.append("\">");
                    sb.append(WsUriConstants.URI_EXISTS);
                    sb.append(FIRST_URI_PARAMETER);
                    sb.append(uri);
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }
    
    private void describe_uriSearch(StringBuilder sb, String text) throws UnsupportedEncodingException, BridgeDBException{
        sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.URI_SEARCH);
                sb.append("\">");
                sb.append(WsUriConstants.URI_SEARCH);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Searches for URIs that have this ending.</li>");
            sb.append("<li>Required arguements:</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.TEXT);
                        sb.append("\">");
                        sb.append(WsUriConstants.TEXT);
                        sb.append("</a></li>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.LIMIT);
                        sb.append("\">");
                        sb.append(WsUriConstants.LIMIT);
                        sb.append("</a> (default available)</li>");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.URI_SEARCH);
                    sb.append(FIRST_TEXT_PARAMETER);
                    sb.append(URLEncoder.encode(text, "UTF-8"));
                    sb.append(LIMIT5_PARAMETER);
                    sb.append("\">");
                    sb.append(WsUriConstants.URI_SEARCH);
                    sb.append(FIRST_TEXT_PARAMETER);
                    sb.append(text);
                    sb.append(LIMIT5_PARAMETER);
                    sb.append("</a></li>");    
            sb.append("</ul>");        
    }
       
    protected final void introduce_Info(StringBuilder sb) {
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.MAPPING_SET);
                sb.append("\">");
                sb.append(WsUriConstants.MAPPING_SET);
                sb.append("</a></dt>");
        sb.append("<dd>Brings up a table of all the mappings in the system by URISpace</dd>");
        sb.append("<dt><a href=\"#");
                sb.append(WsUriConstants.GRAPHVIZ);
                sb.append("\">");
                sb.append(WsUriConstants.GRAPHVIZ);
                sb.append("</a></dt>");
        sb.append("<dd>Brings up the getMappingInfo as graphviz input</dd>");           
    }

    protected final void describe_Info(StringBuilder sb, Xref first, String sourceSysCode, String targetSysCode) 
            throws BridgeDBException, UnsupportedEncodingException {
        sb.append("<h2>Support methods");
        sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.MAPPING_SET);
                sb.append("\">");
                sb.append(WsUriConstants.MAPPING_SET);
                sb.append("</h3>");
                sb.append("<ul>");
            sb.append("<li>Brings up a table/List of mappings in the system by URISpaces</li>");
            sb.append("<li>Optional arguments</li>");
                sb.append("<ul>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                        sb.append("\">");
                        sb.append(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE);
                        sb.append("</a></li>");
                sb.append("<li><a href=\"#");
                        sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("\">");
                        sb.append(WsUriConstants.TARGET_DATASOURCE_SYSTEM_CODE);
                        sb.append("</a> (default available)</li>");
                sb.append("</ul>");           
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.MAPPING_SET);
                    sb.append("\">");
                    sb.append(WsUriConstants.MAPPING_SET);
                    sb.append("</a></li>");    
            sb.append("<li>XML Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.MAPPING_SET);
                    sb.append(WsUriConstants.XML);
                    sb.append("\">");
                    sb.append(WsUriConstants.MAPPING_SET);
                    sb.append(WsUriConstants.XML);
                    sb.append("</a></li>");    
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.MAPPING_SET);
                    sb.append(FIRST_SOURCE_PARAMETER);
                    sb.append(sourceSysCode);
                    sb.append(TARGET_PARAMETER);
                    sb.append(targetSysCode);
                    sb.append("\">");
                    sb.append(WsUriConstants.MAPPING_SET);
                    sb.append(FIRST_SOURCE_PARAMETER);
                    sb.append(URLEncoder.encode(sourceSysCode, "UTF-8"));
                    sb.append(TARGET_PARAMETER);
                    sb.append(URLEncoder.encode(targetSysCode, "UTF-8"));
                    sb.append("</a></li>");    
            sb.append("</ul>");
    }  
            
    protected final void describe_Graphviz(StringBuilder sb) throws BridgeDBException, UnsupportedEncodingException {
        sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.GRAPHVIZ);
                sb.append("\">");
                sb.append(WsUriConstants.GRAPHVIZ);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Brings up the getMappingInfo as graphviz input.</li>");
            sb.append("<li>This method is underdevelopment. Formatting suggestions from Graphviz exports highly welcome.</li>");
            sb.append("<li>This output can then used to create an image of the URISpaces mapped.</li>");
                sb.append("<ul>");
                sb.append("<li>Requires graphviz to be installed on your machine</li>");
                sb.append("<li>Save the output in a file. (ex imsMappings.dot)</li>");
                sb.append("<li>Call graphviz (ex dot -Tgif imsMappings.dot -o imsMappings.gif)</li>");
                sb.append("<li>Open output in your favourite viewer</li>");
                sb.append("</ul>");
            sb.append("<li>No arguements</li>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                sb.append(WsUriConstants.GRAPHVIZ);
                sb.append("\">");
                sb.append(WsUriConstants.GRAPHVIZ);
                sb.append("</a></li>");    
            sb.append("</ul>");        
    }

   private void describe_mapping(StringBuilder sb, int mappingId) throws BridgeDBException {
         sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.MAPPING);
                sb.append("\">");
                sb.append(WsUriConstants.MAPPING);
                sb.append("/id</h3>");
            sb.append("<ul>");
            sb.append("<li>Obtian a mapping</li>");
            sb.append("<li>Required arguements: </li>");
                sb.append("<ul>");
                sb.append("<li>Place the mapping's ID after the /</li> ");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.MAPPING);
                    sb.append("/");
                    sb.append(mappingId);
                    sb.append("\">");
                    sb.append(WsUriConstants.MAPPING);
                    sb.append("/");
                    sb.append(mappingId);
                    sb.append("</a></li>");    
            sb.append("</ul>");        
    }
   
   private void describe_dataSource(StringBuilder sb, String sysCode) 
           throws UnsupportedEncodingException, BridgeDBException {
         sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.DATA_SOURCE);
                sb.append("\">");
                sb.append(WsUriConstants.DATA_SOURCE);
                sb.append("/id</h3>");
            sb.append("<ul>");
            sb.append("<li>Obtian a dataSource</li>");
            sb.append("<li>Required arguements: </li>");
                sb.append("<ul>");
                sb.append("<li>Returns the DataSource and associated UriSpace(s) with a specific id.</li> ");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                    sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.DATA_SOURCE);
                    sb.append("/");
                    sb.append(URLEncoder.encode(sysCode, "UTF-8"));
                    sb.append("\">");
                    sb.append(WsUriConstants.DATA_SOURCE);
                    sb.append("/");
                    sb.append(sysCode);
                    sb.append("</a></li>");    
            sb.append("</ul>");        
   }

   private void describe_getOverallStatistics(StringBuilder sb) 
            throws UnsupportedEncodingException, BridgeDBException{
         sb.append("<h3><a name=\"");
                sb.append(WsUriConstants.GET_OVERALL_STATISTICS);
                sb.append("\">");
                sb.append(WsUriConstants.GET_OVERALL_STATISTICS);
                sb.append("</h3>");
            sb.append("<ul>");
            sb.append("<li>Returns some high level statistics. </li>");
                sb.append("<ul>");
                sb.append("<li>Same as shown on homepage.</li> ");
                sb.append("</ul>");
            sb.append("<li>Example: <a href=\"");
                sb.append(RdfConfig.getTheBaseURI());
                    sb.append(WsUriConstants.GET_OVERALL_STATISTICS);
                    sb.append("\">");
                    sb.append(WsUriConstants.GET_OVERALL_STATISTICS);
                    sb.append("</a></li>");    
            sb.append("</ul>");        
   }
 
       private final void uriMappingForm(StringBuilder sb, HttpServletRequest httpServletRequest) throws BridgeDBException {
    	sb.append("<form method=\"get\" action=\"");
        sb.append(httpServletRequest.getContextPath());
    	sb.append("/");
    	sb.append(WsUriConstants.MAP_URI);
    	sb.append("\">");
    	sb.append("<fieldset>");
    	sb.append("<legend>Mapper</legend>");
    	sb.append("<p><label for=\"");
    	sb.append(WsUriConstants.URI);
    	sb.append("\">Input URI</label>");
    	sb.append("<input type=\"text\" id=\"");
    	sb.append(WsUriConstants.URI);
    	sb.append("\" name=\"");
    	sb.append(WsUriConstants.URI);
    	sb.append("\" style=\"width:80%\"/></p>");
    	generateLensSelector(sb, httpServletRequest);
    	sb.append("<p><input type=\"submit\" value=\"Submit\"/></p>");
    	sb.append("<p>Note: If the new page does not open click on the address bar and press enter</p>");
    	sb.append("</fieldset></form>\n");
    }

    /*private void uriMappingForm(StringBuilder sb, HttpServletRequest httpServletRequest) throws BridgeDBException {
    	sb.append("<form method=\"get\" action=\"");
        sb.append(httpServletRequest.getContextPath());
    	sb.append("/");
    	sb.append(WsUriConstants.MAP_URI);
    	sb.append("\">");
    	sb.append("<fieldset>");
    	sb.append("<legend>Mapper</legend>");
    	sb.append("<p><label for=\"");
    	sb.append(WsUriConstants.URI);
    	sb.append("\">Input URI</label>");
    	sb.append("<input type=\"text\" id=\"");
    	sb.append(WsUriConstants.URI);
    	sb.append("\" name=\"");
    	sb.append(WsUriConstants.URI);
    	sb.append("\" style=\"width:80%\"/></p>");
    	generateLensSelector(sb);
    	sb.append("<p><input type=\"submit\" value=\"Submit\"/></p>");
    	sb.append("<p>Note: If the new page does not open click on the address bar and press enter</p>");
    	sb.append("</fieldset></form>\n");
    }*/
           
    /**
     * Welcome page for the Serivce.
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
        
        sb.append("<p>Welcome to the Identity Mapping Service. </p>");        
                
        sb.append("\n<p>A List of which mappings we current have can be found at ");
        sb.append("<a href=\"/");
        sb.append(httpServletRequest.getContextPath());
        sb.append("/getMappingInfo\">Mapping Info Page</a></p>");
        
        uriMappingForm(sb, httpServletRequest);
        
        sb.append("<h2>Usage Information</h2>");
        sb.append("\n<p>The Main OPS method are: <ul>");
        sb.append("\n<dt><a href=\"/");
        sb.append(httpServletRequest.getContextPath());
        sb.append("/api/#");
        sb.append(WsUriConstants.MAP_URI);
        sb.append("\">");
        sb.append(WsUriConstants.MAP_URI);
        sb.append("<dt><dd>List the URIs that map to this/these URI(s)</dd>");
        sb.append("\n<dt><a href=\"/");
        sb.append(httpServletRequest.getContextPath());
        sb.append("/api/#");
        sb.append(WsUriConstants.MAP);
        sb.append("\">");
        sb.append(WsUriConstants.MAP);
        sb.append("<dt><dd>List the full Mappings to this URI/Xref</dd>");
        sb.append("</ul>");
        sb.append("\n<p><a href=\"/");
        sb.append(httpServletRequest.getContextPath());
        sb.append("/api\">API Page</a></p>");
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
    @Path("/" + WsUriConstants.MAPPING_SET)
    public Response getMappingInfo(@QueryParam(WsUriConstants.SOURCE_DATASOURCE_SYSTEM_CODE) String scrCode,
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
            sb.append("\n<p>Click on the arrows in the first column to expand or contract the table.</p>");
            MappingSetTableMaker.addTable(sb, mappingSetInfos, httpServletRequest);
        }
        footerAndEnd(sb);
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/" + Lens.METHOD_NAME) 
	public Response getLensesHtml(@QueryParam(WsUriConstants.LENS_URI)  String lensUri,
            @Context HttpServletRequest httpServletRequest) throws BridgeDBException {
        List<Lens> lenses = getTheLens(lensUri);
        StringBuilder sb = topAndSide("Lens Summary",  httpServletRequest);
        sb.append("lensUri=").append(lensUri).append("<br>");
        sb.append("ContextPath=").append(httpServletRequest.getContextPath()).append("<br>");
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

}


