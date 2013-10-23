/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.rdf;

import java.io.IOException;
import java.io.InputStream;
import org.bridgedb.bio.DataSourceTxt;

/**
 *
 * @author Christian
 */
public class DataSourceTxtReader extends DataSourceTxt {
    
    /** Call this to initialize the DataSourcs from org/bridgedb/bio/datasources.txt
	 * 	You should call this before using any of these constants, 
	 * 	or they may be undefined.
	 */
	public static void init() 
	{
        try{
    		InputStream is = DataSourceTxt.class.getClassLoader().getResourceAsStream("org/bridgedb/bio/datasources.txt");	
            new DataSourceTxtReader().loadAnInputStream(is);
		}
		catch (IOException ex)
		{
			throw new Error(ex);
		}
	}
      
    @Override
    protected void loadLine(String[] fields) throws IOException {
        super.loadLine(fields);
        System.out.println(fields[1]);
    }

}
