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
package org.bridgedb.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.virtuoso.VirtuosoAccess;

/**
 * Finds the SQL Configuration file and uses it to open the database with the correct database name, user name and password.
 * <p>
 * @See load() for where and in which order the file will be looked for.
 * @author Christian
 */
public class ConfigurationReader {
    /**
     * Name of the file assumed to hold the SQl configurations.
     */
    public static final String CONFIG_FILE_NAME = "sqlConfig.txt";

    /**
     * Name of the propetry that will be set by load() to say where the config file was found.
     */
    public static final String CONFIG_FILE_PATH_PROPERTY = "ConfigPath";
    
    /**
     * Name of the property that will be set by load() to say how the config file was found.
     */
    public static final String CONFIG_FILE_PATH_SOURCE_PROPERTY = "ConfigPathSource";
    
    //Name of the properties that will be looked for in the config file.
    public static final String SQL_PORT_PROPERTY = "SqlPort";
    public static final String SQL_USER_PROPERTY = "SqlUser";
    public static final String SQL_PASSWORD_PROPERTY = "SqlPassword";
    public static final String SQL_DATABASE_PROPERTY = "SqlDatabase";
    public static final String LOAD_SQL_DATABASE_PROPERTY = "LoadSqlDatabase";
    public static final String TEST_SQL_DATABASE_PROPERTY = "TestSqlDatabase";
    public static final String TEST_SQL_USER_PROPERTY = "TestSqlUser";
    public static final String TEST_SQL_PASSWORD_PROPERTY = "TestSqlPassword";
            
    private static final String NO_CONFIG_FILE = "No config file found";
    
    private static Properties properties;
    
    /**
     * Create a wrapper around the Test Virtuosos Database, 
     *     using the hardcoded database name, user name and password.
     * @return
     * @throws BridgeDBException 
     */
    public static SQLAccess createTestVirtuosoAccess() throws BridgeDBException {
        VirtuosoAccess virtuosoAccess = new VirtuosoAccess();
        virtuosoAccess.getConnection();
        return virtuosoAccess;
    }
    
    /**
     * Identified the path where the config file was found.
     * @return The absolutle path of the config file.
     */
    public static String configFilePath(){
        try {
            return getProperties().getProperty(CONFIG_FILE_PATH_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
    
    /**
     * Identifies how the config file was found.
     * @return String saying how the config file was found. 
     */
    public static String configSource(){
        try {
            return getProperties().getProperty(CONFIG_FILE_PATH_SOURCE_PROPERTY);
        } catch (IOException ex) {
           return ex.getMessage();
        }
    }

    /**
     * Identifies the port number the SQL services can be found at.
     * @return Port number is specified otherwise default of 3306
     */
    private static String sqlPort(){
        String result;
        try {
            result = getProperties().getProperty(SQL_PORT_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return "jdbc:mysql://localhost:3306";
    }

    /**
     * Identifies the password to use for the live and load databases
     * @return Password specified or the default of "ims"
     */
    private static String sqlPassword(){
        String result;
        try {
            result = getProperties().getProperty(SQL_PASSWORD_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return "ims";
    }
            
    /**
     * Identifies the user name to use for the live and load databases
     * @return User name specified or the default of "ims"
     */
    private static String sqlUser(){
        String result;
        try {
            result = getProperties().getProperty(SQL_USER_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return "ims";
    }

    /**
     * Identifies the database name to use for the live database
     * @return Database name specified or the default of "ims"
     */
    private static String sqlDatabase(){
        String result;
        try {
            result = getProperties().getProperty(SQL_DATABASE_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return "ims";
    }

    /**
     * Identifies the database name to use for the load database
     * @return Database name specified. Otherwise defaults to the live database.
     */
    private static String sqlLoadDatabase(){
        String result;
        try {
            result = getProperties().getProperty(LOAD_SQL_DATABASE_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return sqlDatabase();
    }

    /**
     * Identifies the database name to use for the test database.
     * <p>
     * Warning some unit test delete all values in the database so this should NEVER be the same as the live database.
     * @return Database name specified or the default of "imstest"
     */
    private static String sqlTestDatabase(){
        String result;
        try {
            result = getProperties().getProperty(TEST_SQL_DATABASE_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return "imstest";
    }

    /**
     * Identifies the password to use for the test databases
     * @return Password specified or defaults to live password
     */
    private static String testSqlPassword(){
        String result;
        try {
            result = getProperties().getProperty(TEST_SQL_PASSWORD_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return sqlPassword();
    }
            
    /**
     * Identifies the user name to use for the test databases
     * @return User name specified or defaults to live user name
     */
    private static String testSqlUser(){
        String result;
        try {
            result = getProperties().getProperty(TEST_SQL_USER_PROPERTY);
        } catch (IOException ex) {
            return ex.getMessage();
        }
        if (result != null) return result;
        return sqlUser();
    }

    /**
     * Returns all the properties found in the config file as well as those set during loading.
     * @return
     * @throws IOException 
     */
    private static Properties getProperties() throws IOException{
        if (properties == null){
            properties = new Properties();
            load();
        }
        return properties;
    }
    
    /** 
     * Loads the config file looks in various places but always stopping when it is found.
     * Any farther config files in other locations are then ignored.
     * <p>
     * Sets the CONFIG_FILE_PATH_PROPERTY and CONFIG_FILE_PATH_SOURCE_PROPERTY.
     * <p>
     * Search order is
     * <ul>
     * <li>@See loadByEnviromentVariable()
     * <li>@See loadDirectly()
     * <li>@loadFromResources()
     * <li>@loadFromSqlConfigs()
     * <li>loadFromSqlResources()
     * </ul>
     * @throws IOException 
     */
    private static void load() throws IOException{
        if (loadByEnviromentVariable()) return;
        if (loadDirectly()) return;
        if (loadFromConfigs()) return;
        if (loadFromParentConfigs()) return;
        properties.put(CONFIG_FILE_PATH_PROPERTY, NO_CONFIG_FILE) ;
        properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, NO_CONFIG_FILE);
    }
    
    /**
     * Looks for the config file in the directory set up the environment variable "OPS_IMS_CONFIG"
     * @return True if the config files was found. False if the environment variable "OPS_IMS_CONFIG" was unset.
     * @throws IOException Thrown if the environment variable is not null, 
     *    and the config file is not found as indicated, or could not be read.
     */
    private static boolean loadByEnviromentVariable() throws IOException {
        String envPath = System.getenv().get("OPS_IMS_CONFIG");
        if (envPath == null || envPath.isEmpty()) return false;
        File envDir = new File(envPath);
        if (!envDir.exists()){
            throw new FileNotFoundException ("Environment Variable OPS_IMS_CONFIG points to " + envPath + 
                    " but no directory found there");
        }
        if (envDir.isDirectory()){
            File envFile = new File(envDir, CONFIG_FILE_NAME);
            if (!envFile.exists()){
                throw new FileNotFoundException ("Environment Variable OPS_IMS_CONFIG points to " + envPath + 
                        " but no " + CONFIG_FILE_NAME + " file found there");
            }
            FileInputStream configs = new FileInputStream(envFile);
            properties.load(configs);
            properties.put(CONFIG_FILE_PATH_PROPERTY, envFile.getAbsolutePath());
            properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, "OPS_IMS_CONFIG Enviroment Variable");
            return true;
        } else {
            throw new FileNotFoundException ("Environment Variable OPS_IMS_CONFIG points to " + envPath + 
                    " but is not a directory");            
        }
    }

    /**
     * Looks for the config file in the directory set up the environment variable "OPS_IMS_CONFIG"
     * @return True if the config files was found. False if the environment variable "OPS_IMS_CONFIG" was unset.
     * @throws IOException Thrown if the environment variable is not null, 
     *    and the config file is not found as indicated, or could not be read.
     */
    private static boolean loadByCatalinaHomeConfigs() throws IOException {
        String catalinaHomePath = System.getenv().get("CATALINA_HOME");
        if (catalinaHomePath == null || catalinaHomePath.isEmpty()) return false;
        File catalineHomeDir = new File(catalinaHomePath);
        if (!catalineHomeDir.exists()){
            throw new FileNotFoundException ("Environment Variable CATALINA_HOME points to " + catalinaHomePath + 
                    " but no directory found there");
        }
        if (!catalineHomeDir.isDirectory()){
            throw new FileNotFoundException ("Environment Variable CATALINA_HOME points to " + catalinaHomePath + 
                    " but is not a directory");            
        }
        File envDir = new File (catalineHomeDir + "/conf/OPS-IMS");
        if (!envDir.exists()) return false; //No hard requirements that catalineHome has a /conf/OPS-IMS
        if (envDir.isDirectory()){
            File envFile = new File(catalineHomeDir, CONFIG_FILE_NAME);
            if (!catalineHomeDir.exists()){
                throw new FileNotFoundException ("Environment Variable CATALINA_HOME points to " + catalinaHomePath + 
                        " but subdirectory /conf/OPS-IMS has no " + CONFIG_FILE_NAME + " file.");
            }
            FileInputStream configs = new FileInputStream(catalineHomeDir);
            properties.load(configs);
            properties.put(CONFIG_FILE_PATH_PROPERTY, catalineHomeDir.getAbsolutePath());
            properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, "OPS_IMS_CONFIG Enviroment Variable");
            return true;
        } else {
            throw new FileNotFoundException ("Environment Variable CATALINA_HOME points to " + catalinaHomePath  + 
                    " but $CATALINA_HOME/conf/OPS-IMS is not a directory");            
        }
    }

    /**
     * Looks for the config file in the run directory.
     * @return True if the file was found, False if it was not found.
     * @throws IOException If there is an error reading the file.
     */
    private static boolean loadDirectly() throws IOException {
        File envFile = new File(CONFIG_FILE_NAME);
        if (!envFile.exists()) return false;
        FileInputStream configs = new FileInputStream(envFile);
        properties.load(configs);
        properties.put(CONFIG_FILE_PATH_PROPERTY, envFile.getAbsolutePath());
        properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, "From main Directory");
        return true;
    }

    /**
     * Looks for the config file in the conf/OPS-IMS sub directories of the run directory.
     * <p>
     * For tomcat conf would then be a sister directory of webapps.
     * @return True if the file was found, False if it was not found.
     * @throws IOException If there is an error reading the file.
     */
    private static boolean loadFromConfigs() throws IOException {
        File confFolder = new File ("conf/OPS-IMS");
        if (!confFolder.exists()) return false;
        if (!confFolder.isDirectory()){
            throw new IOException("Expected " + confFolder.getAbsolutePath() + " to be a directory");
        }
        File envFile = new File(confFolder, CONFIG_FILE_NAME);
        if (!envFile.exists()) return false;
        FileInputStream configs = new FileInputStream(envFile);
        properties.load(configs);
        properties.put(CONFIG_FILE_PATH_PROPERTY, envFile.getAbsolutePath());
        properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, "From conf/OPS-IMS");
        return true;
    }

    /**
     * Looks for the config file in the conf/OPS-IMS sub directories of the run directory.
     * <p>
     * For tomcat conf would then be a sister directory of webapps.
     * @return True if the file was found, False if it was not found.
     * @throws IOException If there is an error reading the file.
     */
    private static boolean loadFromParentConfigs() throws IOException {
        File confFolder = new File ("../conf/OPS-IMS");
        if (!confFolder.exists()) return false;
        if (!confFolder.isDirectory()){
            throw new IOException("Expected " + confFolder.getAbsolutePath() + " to be a directory");
        }
        File envFile = new File(confFolder, CONFIG_FILE_NAME);
        if (!envFile.exists()) return false;
        FileInputStream configs = new FileInputStream(envFile);
        properties.load(configs);
        properties.put(CONFIG_FILE_PATH_PROPERTY, envFile.getAbsolutePath());
        properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, "From ../conf/OPS-IMS");
        return true;
    }

    /** 
     * Outputs a list of all the properties to the PrintStream. 
     * @param out A PrintStream such as System out
     */
    public static void list(PrintStream out){
        try {
            getProperties().list(out);
        } catch (IOException ex) {
            out.print(ex);
        }
    }
      
}
