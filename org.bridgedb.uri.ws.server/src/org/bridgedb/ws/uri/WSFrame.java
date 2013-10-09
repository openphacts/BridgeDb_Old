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

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.Lens;
import org.bridgedb.uri.SetMappings;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WsUriConstants;
import org.bridgedb.ws.templates.WebTemplates;

/**
 * This class provides the Response Frame including Top and Sidebar 
 * 
 * @author Christian
 */
public class WSFrame extends WSUriInterfaceService {
    
    protected final NumberFormat formatter;
        
    static final Logger logger = Logger.getLogger(WSFrame.class);

    public WSFrame()  throws BridgeDBException   {
        super();
        URL resource = this.getClass().getClassLoader().getResource(""); 
        formatter = NumberFormat.getInstance();
        if (formatter instanceof DecimalFormat) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setGroupingSeparator(',');
            ((DecimalFormat) formatter).setDecimalFormatSymbols(dfs);
        }
    }
        
    protected String serviceName(){
        return "BridgeDb ";
    }
    
    public StringBuilder topAndSide(String header, HttpServletRequest httpServletRequest) {
        return topAndSide(header, "function loadAll(){}\n", httpServletRequest);
    }
    
    public StringBuilder topAndSide(String header, String scriptOther, HttpServletRequest httpServletRequest) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("TITLE", serviceName() + header);
        velocityContext.put("SCRIPT_OTHER", scriptOther);            
        StringBuilder sb = new StringBuilder(WebTemplates.getForm(velocityContext, WebTemplates.FRAME));
        sideBar(sb, httpServletRequest);
        sb.append("<div id=\"content\">");
        return sb;
    }
    
    protected void sideBar(StringBuilder sb, HttpServletRequest httpServletRequest) {
        sb.append("<div id=\"navBar\">");
        addSideBarMiddle(sb, httpServletRequest);
        addSideBarStatisitics(sb, httpServletRequest);
        sb.append("</div>\n");        
    }
    
    /**
     * Allows Super classes to add to the side bar
     */
    public void addSideBarMiddle(StringBuilder sb, HttpServletRequest httpServletRequest){
        addSideBarBridgeDb(sb, httpServletRequest);
    }
    
    /**
     * Allows Super classes to add to the side bar
     */
    protected void addSideBarBridgeDb(StringBuilder sb, HttpServletRequest httpServletRequest) {
        sb.append("<div class=\"menugroup\">BridgeDb Service</div>");
        addSideBarItem(sb, WsUriConstants.BRIDGEDB_HOME, "Home", httpServletRequest);
        try {
            String allMappingInfo = SetMappings.METHOD_NAME + "?" + WsUriConstants.LENS_URI + "=" + Lens.getAllLens();
            addSideBarItem(sb, allMappingInfo,"All Mappings Summary", httpServletRequest);
        } catch (BridgeDBException ex) {
            logger.error("Error getting getAllLens", ex);
        }
        addSideBarItem(sb,  SetMappings.METHOD_NAME, "Default Mappings Summary", httpServletRequest);
        try {
            String allGraphwiz = WsUriConstants.GRAPHVIZ + "?" + WsUriConstants.LENS_URI + "=" + Lens.getAllLens();
            addSideBarItem(sb, allGraphwiz, "All Mappings Graphviz",  httpServletRequest);
        } catch (BridgeDBException ex) {
            logger.error("Error getting getAllLens", ex);
        }
        addSideBarItem(sb, WsUriConstants.GRAPHVIZ, "Default Mappings Graphviz",  httpServletRequest);
        addSideBarItem(sb, Lens.METHOD_NAME, Lens.METHOD_NAME,  httpServletRequest);
        addSideBarItem(sb, WsUriConstants.BRIDGEDB_API, "Api", httpServletRequest);
    }

    /**
     * Allows Super classes to add to the side bar
     */
    protected void addSideBarStatisitics(StringBuilder sb, HttpServletRequest httpServletRequest) {
        try {
            OverallStatistics statistics = uriMapper.getOverallStatistics(Lens.getDefaultLens());
            //sb.append("\n<div class=\"menugroup\">Default Statisitics</div>");
            //addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfMappings()) + " Mappings", httpServletRequest);
            //addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfMappingSets()) + " Mapping Sets", httpServletRequest);
            //addSideBarItem(sb, "getSupportedSrcDataSources", formatter.format(statistics.getNumberOfSourceDataSources()) 
            //        + " Source Data Sources", httpServletRequest);
            //addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfPredicates()) + " Predicates", httpServletRequest);
            //addSideBarItem(sb, "getSupportedTgtDataSources", formatter.format(statistics.getNumberOfTargetDataSources()) 
             //       + " Target Data Sources ", httpServletRequest);
            statistics = uriMapper.getOverallStatistics(Lens.getAllLens());
            //sb.append("\n<div class=\"menugroup\">All Statisitics</div>");
            sb.append("\n<div class=\"menugroup\">Statisitics</div>");
            addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfMappings()) + " Mappings", httpServletRequest);
            addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfMappingSets()) + " Mapping Sets", httpServletRequest);
            addSideBarItem(sb, "getSupportedSrcDataSources", formatter.format(statistics.getNumberOfSourceDataSources()) 
                    + " Source Data Sources", httpServletRequest);
            addSideBarItem(sb, "getMappingInfo", formatter.format(statistics.getNumberOfPredicates()) + " Predicates", httpServletRequest);
            addSideBarItem(sb, "getSupportedTgtDataSources", formatter.format(statistics.getNumberOfTargetDataSources()) 
                    + " Target Data Sources ", httpServletRequest);
            addSideBarItem(sb, Lens.METHOD_NAME, formatter.format(statistics.getNumberOfLenses())
                    + " Lenses ", httpServletRequest);
        } catch (BridgeDBException ex) {
            sb.append("\nStatisitics currenlty unavailable.");
            logger.error("Error getting statistics.", ex);
        }
    }
    
    /**
     * Adds an item to the SideBar for this service
     */
    public void addSideBarItem(StringBuilder sb, String page, String name, HttpServletRequest httpServletRequest) {
        sb.append("\n<div id=\"menu");
        sb.append(page);
        sb.append("_text\" class=\"texthotlink\" ");
        sb.append("onmouseout=\"DHTML_TextRestore('menu");
        sb.append(page);
        sb.append("_text'); return true; \" ");
        sb.append("onmouseover=\"DHTML_TextHilight('menu");
        sb.append(page);
        sb.append("_text'); return true; \" ");
        sb.append("onclick=\"document.location = &quot;");
        sb.append(httpServletRequest.getContextPath());
        sb.append("/");
        sb.append(page);
        sb.append("&quot;;\">");
        sb.append(name);
        sb.append("</div>");
     }

    protected void footerAndEnd(StringBuilder sb) throws BridgeDBException{
        sb.append("</div>\n<div id=\"footer\">");
        sb.append("\n<div></body></html>");
    }

	public void generateLensSelector(StringBuilder sb, HttpServletRequest httpServletRequest) throws BridgeDBException {
		List<Lens> lenses = Lens.getLens();
        sb.append("<p>");
    	sb.append(WsUriConstants.LENS_URI);
        sb.append("<select name=\"");
    	sb.append(WsUriConstants.LENS_URI);
    	sb.append("\">");
		for (Lens lens : lenses) {
			sb.append("<option value=\"");
 			sb.append(lens.toUri(httpServletRequest.getContextPath()));
			sb.append("\">");
			sb.append(lens.getName());
			sb.append("</option>");
		}
    	sb.append("</select>\n");
	}


}


