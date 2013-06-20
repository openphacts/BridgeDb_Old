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
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.Lens;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WsUriConstants;

/**
 * This class provides the Reposnse Frame including Top and Sidebar 
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
        String title = serviceName() + header;
        StringBuilder sb = header(serviceName() + title);
        top(sb, title);      
        sideBar(sb, httpServletRequest);
        sb.append("<div id=\"content\">");
        return sb;
    }
    
    protected StringBuilder header(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        sb.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"/>");
        sb.append("<head>\n");
        sb.append("<title>");
        sb.append(title);
        sb.append("</title>\n");
        style(sb);
        toggler(sb);
        sb.append("</head><body>");
        return sb;
    }

    protected void style(StringBuilder sb) {
        sb.append("<style>\n");
        sb.append("#container { width: 100%; margin: 10px auto; background-color: #fff; color: #333; border: ");
            sb.append("1px solid gray; line-height: 130%; font-family: perpetua, garamond, serif; font-size: 110%; ");
            sb.append("min-width: 40em; }\n");
        sb.append("#top { padding: .5em; background-color: #808080; border-bottom: 1px solid gray; }\n");
        sb.append("#top h1 { padding: .25em .5em .25em .5em; margin-left: 200px; margin-bottom: 0; margin-right: 0; margin-top: 0 }\n");
        sb.append("#top a { text-decoration: none; color: #ffffff; }\n");
        sb.append("#navBar { float: left; width: 200px; margin: 0em; padding: 5px; min-width: 200px; border-right: 1px solid gray; min-height: 100%} \n");
        sb.append("#content { margin-left: 210px; border-left: 1px solid gray; padding: 1em; min-width: 20em; min-height: 500px; }\n");
        sb.append("#footer { clear:both; }\n");
        sb.append("fieldset {border: 1px solid #781351;width: 20em}\n");
        sb.append("legend { color: #fff; background: #ffa20c; border: 1px solid #781351; padding: 2px 6px }\n");
        sb.append("</style>\n");
        sb.append("<style type=\"text/css\">");
        sb.append("	.texthotlink, .texthotlink_hilight { width: 150px; font-size: 85%; padding: .25em; cursor: ");
            sb.append("pointer; color: black; font-family: Arial, sans-serif;	}\n");
        sb.append("	.texthotlink_hilight {background-color: #fff6ac;}\n");
        sb.append("		.menugroup { font-size: 150%; font-weight: bold; padding-top: .25em; }\n");
        sb.append("		input { background-color: #EEEEFF; } body, td { background-color: white; font-family: sans-serif; }\n");
        sb.append("	</style>\n");            
    }

    protected void toggler(StringBuilder sb) {
        sb.append("<script language=\"javascript\">\n");
        sb.append("		function getObj(id) {\n");
        sb.append("			return document.getElementById(id)\n");
        sb.append("		}\n");
        sb.append("		function DHTML_TextHilight(id) {\n");
        sb.append("			getObj(id).classNameOld = getObj(id).className;\n");
        sb.append("			getObj(id).className = getObj(id).className + \"_hilight\";\n");
        sb.append("		}\n");
        sb.append("		function DHTML_TextRestore(id) {\n");
        sb.append("			if (getObj(id).classNameOld != \"\")\n");
        sb.append("				getObj(id).className = getObj(id).classNameOld;\n");
        sb.append("		}\n");
        sb.append("     function getItem(id){\n");
        sb.append("         var itm = false;\n");
        sb.append("         if(document.getElementById)\n");
        sb.append("             itm = document.getElementById(id);\n");
        sb.append("         else if(document.all)\n");
        sb.append("             itm = document.all[id];\n");
        sb.append("         else if(document.layers)\n");
        sb.append("             itm = document.layers[id];\n");
        sb.append("         return itm;\n");
        sb.append("    }\n\n");
        sb.append("    function toggleItem(id)\n");
        sb.append("{\n");
        sb.append("    itm = getItem(id);\n");
        sb.append("    if(!itm)\n");
        sb.append("        return false;\n");
        sb.append("    if(itm.style.display == 'none')\n");
        sb.append("        itm.style.display = '';\n");
        sb.append("    else\n");
        sb.append("        itm.style.display = 'none';\n");
        sb.append("    return false;\n");
        sb.append("}\n\n");
        sb.append("function hideDetails()\n");
        sb.append("{\n");
        sb.append("     toggleItem('ops')\n");
        sb.append("     toggleItem('sparql')\n");
        sb.append("     return true;\n");
        sb.append("}\n\n");
        sb.append("</script>\n");
    }

     protected void top(StringBuilder sb, String title) {
        sb.append("<div id=\"container\">");
        sb.append("<div id=\"top\">");
        sb.append("<h1>");
        sb.append(title);
        sb.append("</h1>");
        sb.append("</div>");   
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
            String allMappingInfo = WsUriConstants.MAPPING_SET + "?" + WsUriConstants.LENS_URI + "=" + Lens.getAllLens();
            addSideBarItem(sb, allMappingInfo,"All Mappings Summary", httpServletRequest);
        } catch (BridgeDBException ex) {
            logger.error("Error getting getAllLens", ex);
        }
        addSideBarItem(sb,  WsUriConstants.MAPPING_SET, "Default Mappings Summary", httpServletRequest);
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


