// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright      2012  Christian Y. A. Brenninkmeijer
// Copyright      2012  OpenPhacts
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


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.metadata.validator.ValidationType;
import org.bridgedb.sql.SQLUrlMapper;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.url.URLMapping;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.IpConfig;
import org.openrdf.rio.RDFFormat;

/**
 * This class provides the Reposnse Frame including Top and Sidebar 
 * 
 * @author Christian
 */
public class WSFame extends WSOpsInterfaceService {
    
    protected final NumberFormat formatter;
        
    static final Logger logger = Logger.getLogger(WSFame.class);

    String serviceName;
    
    public WSFame()  throws IDMapperException   {
        super();
        URL resource = this.getClass().getClassLoader().getResource(""); 
        serviceName = getResourceName();
        formatter = NumberFormat.getInstance();
        if (formatter instanceof DecimalFormat) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setGroupingSeparator(',');
            ((DecimalFormat) formatter).setDecimalFormatSymbols(dfs);
        }
    }
        
    private String getResourceName(){
        URL resource = this.getClass().getClassLoader().getResource(""); 
        String path = resource.toString();
        if (path.contains("/webapps/") && path.contains("/WEB-INF/")){
            int start = path.lastIndexOf("/webapps/") + 9;
            String name = path.substring(start, path.lastIndexOf("/WEB-INF/"));
            logger.info("ResourceName = " + name);
            return name;
        }
        if (!path.endsWith("/test-classes/")){
            logger.warn("Unable to get resource name from " + path);
        }
        return getDefaultResourceName();
    }
    
    /**
     * Backup in case getResourceName fails.
     * 
     * Super classes will need to insert their own war name.
     * @return war name.
     */
    public String getDefaultResourceName(){
        return "OPS-IMS";
    }
    
    /**
     * API page for the IMS methods.
     * 
     * Warning may not be completely up to date.
     * 
     * @param httpServletRequest
     * @return
     * @throws IDMapperException
     * @throws UnsupportedEncodingException 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/ims-api")
    public Response imsApiPage(@Context HttpServletRequest httpServletRequest) throws IDMapperException, UnsupportedEncodingException {
        //Long start = new Date().getTime();
        StringBuilder sb = topAndSide("IMS API",  httpServletRequest);
 
        Set<String> urls = urlMapper.getSampleSourceURLs();  
        Iterator<String> urlsIt = urls.iterator();
        Xref first = urlMapper.toXref(urlsIt.next());
        String sysCode = first.getDataSource().getSystemCode();
        Xref second =  urlMapper.toXref(urlsIt.next());
        Set<Xref> firstMaps = idMapper.mapID(first);
        Set<String> keys = idMapper.getCapabilities().getKeys();
        String URL1 = urlsIt.next();
        String text = SQLUrlMapper.getId(URL1);
        String URL2 = urlsIt.next();
        Set<URLMapping> mappings2 = urlMapper.mapURLFull(URL2);
        HashSet<String> URI2Spaces = new HashSet<String>();
        int mappingId = 0;
        for (URLMapping mapping:mappings2){
            if (mapping.getId() != null){
                mappingId = mapping.getId();
            }
            String targetURL = mapping.getTargetURLs().iterator().next();
            URI2Spaces.add(SQLUrlMapper.getUriSpace(targetURL));            
        }
        boolean freeSearchSupported = idMapper.getCapabilities().isFreeSearchSupported(); 

        sb.append("\n<p><a href=\"/OPS-IMS\">Home Page</a></p>");
                
        sb.append("\n<p>");
        WSOpsApi api = new WSOpsApi();

        sb.append("<h2>Support services include:<h2>");
        sb.append("<dl>");      
        api.introduce_IDMapper(sb, freeSearchSupported);
        api.introduce_IDMapperCapabilities(sb, keys, freeSearchSupported);     
        api.introduce_URLMapper(sb, freeSearchSupported);
        api.introduce_Info(sb);
        sb.append("</dl>");
        sb.append("</p>");
        
        api.describeParameter(sb);        
        
        api.describe_IDMapper(sb, first, firstMaps, second, freeSearchSupported);
        api.describe_IDMapperCapabilities(sb, first, firstMaps, keys, freeSearchSupported);
        api.describe_URLMapper(sb, URL1, URL2, URI2Spaces, text, mappingId, sysCode, freeSearchSupported);
        api.describe_Info(sb);
        
        sb.append("</body></html>");
        //ystem.out.println("Done "+ (new Date().getTime() - start));
        return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    }
    
    protected StringBuilder topAndSide(String header, HttpServletRequest httpServletRequest) throws IDMapperException{
        StringBuilder sb = new StringBuilder(HEADER_TO_TITLE);
        sb.append(header);
        sb.append(HEADER_AFTER_TITLE);
        sb.append(TOGGLER);
        sb.append(HEADER_END);
        sb.append(BODY);
        sb.append(TOP_LEFT);
        sb.append(header);
        sb.append(TOP_RIGHT);
        sb.append(SIDE_BAR_BEGIN);
        addSideBarMiddle(sb, httpServletRequest);
        sb.append(SIDE_BAR_END);
        return sb;
    }
    
    /**
     * Allows Super classes to add to the side bar
     */
    protected void addSideBarMiddle(StringBuilder sb, HttpServletRequest httpServletRequest) throws IDMapperException{
        addSideBarIMS(sb);
        addSideBarStatisitics(sb);
    }
    
    /**
     * Allows Super classes to add to the side bar
     */
    protected void addSideBarIMS(StringBuilder sb) throws IDMapperException{
        sb.append("<div class=\"menugroup\">OPS Identity Mapping Service</div>");
        addSideBarItem(sb, "", "Home");
        addSideBarItem(sb, "getMappingInfo", "Mappings Summary");
        addSideBarItem(sb, "graphviz", "Mappings Summary in Graphviz format");
        addSideBarItem(sb, "ims-api", "IMS API");
    }

    /**
     * Allows Super classes to add to the side bar
     */
    protected void addSideBarStatisitics(StringBuilder sb) throws IDMapperException{
        OverallStatistics statistics = urlMapper.getOverallStatistics();
        sb.append("\n<div class=\"menugroup\">Statisitics</div>");
        addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfMappings()) + " Mappings");
        addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfMappingSets()) + " Mapping Sets");
        addSideBarItem(sb, "getSupportedSrcDataSources", formatter.format(statistics.getNumberOfSourceDataSources()) 
                + " Source Data Sources");
        addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfPredicates()) + " Predicates");
        addSideBarItem(sb, "getSupportedTgtDataSources", formatter.format(statistics.getNumberOfTargetDataSources()) 
                + " Target Data Sources ");
    }
    
    /**
     * Adds an item to the SideBar for this service
     */
    protected void addSideBarItem(StringBuilder sb, String page, String name) throws IDMapperException{
        sb.append("\n<div id=\"menu");
        sb.append(page);
        sb.append("_text\" class=\"texthotlink\" ");
        sb.append("onmouseout=\"DHTML_TextRestore('menu");
        sb.append(page);
        sb.append("_text'); return true; \" ");
        sb.append("onmouseover=\"DHTML_TextHilight('menu");
        sb.append(page);
        sb.append("_text'); return true; \" ");
        sb.append("onclick=\"document.location = &quot;/");
        sb.append(serviceName);
        sb.append("/");
        sb.append(page);
        sb.append("&quot;;\">");
        sb.append(name);
        sb.append("</div>");
     }

    private final String HEADER_TO_TITLE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
            + "<html xmlns:v=\"urn:schemas-microsoft-com:vml\">\n"
            + "<head>\n"
            + " <title>"
            + "     Manchester University OpenPhacts ";
    private final String HEADER_AFTER_TITLE = "	</title>\n"
            + "	<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"></meta>\n"
            + "	<script>"
            + "		function getObj(id) {"
            + "			return document.getElementById(id)"
            + "		}"
            + "		function DHTML_TextHilight(id) {"
            + "			getObj(id).classNameOld = getObj(id).className;"
            + "			getObj(id).className = getObj(id).className + \"_hilight\";"
            + "		}"
            + "		function DHTML_TextRestore(id) {"
            + "			if (getObj(id).classNameOld != \"\")"
            + "				getObj(id).className = getObj(id).classNameOld;"
            + "		}"
            + "	</script>\n";
    private final String TOGGLER ="<script language=\"javascript\">\n"
            + "function getItem(id)\n"
            + "{\n"
            + "    var itm = false;\n"
            + "    if(document.getElementById)\n"
            + "        itm = document.getElementById(id);\n"
            + "    else if(document.all)\n"
            + "        itm = document.all[id];\n"
            + "     else if(document.layers)\n"
            + "        itm = document.layers[id];\n"
            + "    return itm;\n"
            + "}\n\n"
            + "function toggleItem(id)\n"
            + "{\n"
            + "    itm = getItem(id);\n"
            + "    if(!itm)\n"
            + "        return false;\n"
            + "    if(itm.style.display == 'none')\n"
            + "        itm.style.display = '';\n"
            + "    else\n"
            + "        itm.style.display = 'none';\n"
            + "    return false;\n"
            + "}\n\n"
            + "function hideDetails()\n"
            + "{\n"
            + "     toggleItem('ops')\n"
            + "     toggleItem('sparql')\n"
            + "     return true;\n"
            + "}\n\n"
            + "</script>\n";
    private final String HEADER_END = "	<style type=\"text/css\">"
            + "		.texthotlink, .texthotlink_hilight {"
            + "			width: 150px;"
            + "			font-size: 85%;"
            + "			padding: .25em;"
            + "			cursor: pointer;"
            + "			color: black;"
            + "			font-family: Arial, sans-serif;"
            + "		}"
            + "		.texthotlink_hilight {"
            + "			background-color: #fff6ac;"
            + "		}"
            + "		.menugroup {"
            + "			font-size: 90%;"
            + "			font-weight: bold;"
            + "			padding-top: .25em;"
            + "		}"
            + "		input { background-color: #EEEEFF; }"
            + "		body, td {"
            + "			background-color: white;"
            + "			font-family: sans-serif;"
            + "		}"
            + "	</style>\n"
            + "</head>\n";            
    private final String BODY ="<body style=\"margin: 0px\">";
    private final String TOP_LEFT ="	<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n"
            + "		<tr valign=\"top\">\n"
            + "			<td style=\"background-color: white;\">"
            + "				<a href=\"http://www.openphacts.org/\">"
            + "                 <img style=\"border: none; padding: 0px; margin: 0px;\" "
            + "                     src=\"http://www.openphacts.org/images/stories/banner.jpg\" "
            + "                     alt=\"Open PHACTS\" height=\"50\">"
            + "                 </img>"
            + "             </a>"
            + "			</td>\n"
            + "			<td style=\"font-size: 200%; font-weight: bold; font-family: Arial;\">\n";
    private final String TOP_RIGHT = "         </td>"
            + "			<td style=\"background-color: white;\">"
            + "				<a href=\"http://www.cs.manchester.ac.uk//\">"
            + "                 <img style=\"border: none; padding: 0px; margin: 0px;\" align=\"right\" "
            + "                     src=\"http://www.manchester.ac.uk/media/corporate/theuniversityofmanchester/assets/images/logomanchester.gif\" "
            + "                    alt=\"The University of Manchester\" height=\"50\">"
            + "                 </img>"
            + "             </a>"
            + "			</td>"
            + "		</tr>"
            + "	</table>";
    private final String SIDE_BAR_BEGIN = "	<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">"
            + "		<tr valign=\"top\">"
            + "			<td style=\"border-top: 1px solid #D5D5FF\">";
    private final String SIDE_BAR_QUERY_EXPANDER = "<div class=\"menugroup\">Query Expander</div>"
            + "				<div id=\"menuQueryExpanderHome_text\" class=\"texthotlink\" "
            + "                   onmouseout=\"DHTML_TextRestore('menuQueryExpanderHome_text'); return true; \" "
            + "                   onmouseover=\"DHTML_TextHilight('menuQueryExpanderHome_text'); return true; \" "
            + "                   onclick=\"document.location = &quot;/QueryExpander&quot;;\">Home</div>"
            + "				<div id=\"menuQueryExpanderAPI_text\" class=\"texthotlink\" "
            + "                   onmouseout=\"DHTML_TextRestore('menuQueryExpanderAPI_text'); return true; \" "
            + "                   onmouseover=\"DHTML_TextHilight('menuQueryExpanderAPI_text'); return true; \" "
            + "                   onclick=\"document.location = &quot;/QueryExpander/ims-api&quot;;\">API</div>"
            + "				<div id=\"menuQueryExpanderExamples_text\" class=\"texthotlink\" "
            + "                   onmouseout=\"DHTML_TextRestore('menuQueryExpanderExamples_text'); return true; \" "
            + "                   onmouseover=\"DHTML_TextHilight('menuQueryExpanderExamples_text'); return true; \" "
            + "                   onclick=\"document.location = &quot;/QueryExpander/examples&quot;;\">Examples</div>"
            + "				<div id=\"menuQueryExpanderURISpacesPerGraph_text\" class=\"texthotlink\" "
            + "                   onmouseout=\"DHTML_TextRestore('menuQueryExpanderURISpacesPerGraph_text'); return true; \" "
            + "                   onmouseover=\"DHTML_TextHilight('menuQueryExpanderURISpacesPerGraph_text'); return true; \" "
            + "                   onclick=\"document.location = &quot;/QueryExpander/URISpacesPerGraph&quot;;\">"
            + "                   URISpaces per Graph</div>"
            + "				<div id=\"menuQueryExpanderMapURI_text\" class=\"texthotlink\" "
            + "                   onmouseout=\"DHTML_TextRestore('menuQueryExpanderMapURI_text'); return true; \" "
            + "                   onmouseover=\"DHTML_TextHilight('menuQueryExpanderMapURI_text'); return true; \" "
            + "                   onclick=\"document.location = &quot;/QueryExpander/mapURI&quot;;\">"
            + "                   Check Mapping for an URI</div>";          
    
    private final String SIDE_BAR_END =
              "			</td>"
            + "			<td width=\"5\" style=\"border-right: 1px solid #D5D5FF\"></td>"
            + "			<td style=\"border-top: 1px solid #D5D5FF; width:100%\">";
    
    final String FORM_OUTPUT_FORMAT = " \n<p>Output Format:"
            + "     <select size=\"1\" name=\"format\">"
            + "         <option value=\"html\">HTML page</option>"
            + "         <option value=\"xml\">XML/JASON</option>"
            + " 	</select>"
            + " </p>";
    private final String MAIN_END = "			</td>"
            + "		</tr>"
            + "	</table>"
            + "	<div style=\"border-top: 1px solid #D5D5FF; padding: .5em; font-size: 80%;\">"
            + "		This site is run by <a href=\"https://wiki.openphacts.org/index.php/User:Christian\">Christian Brenninkmeijer</a>."
            + "	</div>";
    private final String BODY_END = "</body>"
            + "</html>";
    final String END = MAIN_END + BODY_END;


}


