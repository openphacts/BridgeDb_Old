package uk.ac.manchester.cs.openphacts.bridgedb.webtemplates;

import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.bridgedb.uri.Lens;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WsUriConstants;

/**
 * Hello world!
 *
 */
public class WebTemplates 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
    private String contextPath;
    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    public String getUriMappingForm(VelocityContext context) {
        Properties props = new Properties();
    	props.put("resource.loader", "class");
    	props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    	VelocityEngine ve = new VelocityEngine();
    	ve.init(props);
        Template t = ve.getTemplate( "uriMappingForm.vm" );
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
        return writer.toString();
    }
    
    public String getBridgeDBHome(VelocityContext context) {
        Properties props = new Properties();
    	props.put("resource.loader", "class");
    	props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    	VelocityEngine ve = new VelocityEngine();
    	ve.init(props);
        Template t = ve.getTemplate( "bridgeDBHome.vm" );
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
        return writer.toString();    }
}
