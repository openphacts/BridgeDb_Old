package org.bridgedb.ws.templates;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class WebTemplates 
{

    public static final String URI_MAPPING_FORM = "uriMappingForm.vm";
    public static final String BRIDGEDB_HOME = "bridgeDBHome.vm";
    private static VelocityEngine velocityEngine = initialize();
    
    private static VelocityEngine initialize(){
        Properties props = new Properties();
    	props.put("resource.loader", "class");
    	props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    	VelocityEngine ve = new VelocityEngine();
    	ve.init(props);  
        return ve;
    }
    
     public static String getForm (VelocityContext context, String formName){
        Template t = velocityEngine.getTemplate(formName);
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
        return writer.toString();       
    }
    
}