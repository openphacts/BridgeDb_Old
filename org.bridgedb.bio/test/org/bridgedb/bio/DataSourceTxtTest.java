/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.bio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.bridgedb.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christian
 */
public class DataSourceTxtTest {
    
    /**
     * Test of init method, of class DataSourceTxt.
     */
    @Test
    public void testWriteRead() throws IOException {
        System.out.println("WriteRead");
        DataSourceTxt.init();
        File generated = new File("test-data/generatedDatasources.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(generated));
        DataSourceTxt.writeToBuffer(writer);
        InputStream is = new FileInputStream(generated);
        DataSourceTxt.loadInputStream(is);
    }

}
