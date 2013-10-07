package uk.ac.manchester.cs.openphacts.bridgedb.webtemplates;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Hello world!
 *
 */
public class WebTemplates 
{

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
