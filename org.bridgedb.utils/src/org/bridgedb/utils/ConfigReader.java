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
package org.bridgedb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;

/**
 *
 * @author Christian
 */
public class ConfigReader {
    
    //Files
    public static final String CONFIG_FILE_NAME = "BridgeDb.properties";
    public static final String LOCAL_FILE_NAME = "local.properties";
    public static final String LOG_PROPERTIES_FILE = "log4j.properties";

    //Properties added by this class
    public static final String CONFIG_FILE_PATH_PROPERTY = "ConfigPath";
    public static final String CONFIG_FILE_PATH_SOURCE_PROPERTY = "ConfigPathSource";

    private InputStream inputStream;
    private String findMethod;
    private String foundAt;
    private String error = null;
    private Properties properties = null;
    
    protected static boolean useTest = false;
    private static boolean loggerSetup = false;
    private static ConfigReader propertyReader = null;
    
    private static final Logger logger = Logger.getLogger(ConfigReader.class);
    
    public static Properties getProperties() throws BridgeDBException{
        if (propertyReader == null){
            configureLogger();
            propertyReader = new ConfigReader(CONFIG_FILE_NAME);    
            propertyReader.readProperties();
            propertyReader.properties = addLocalProperties(propertyReader.properties);
        }
        return propertyReader.readProperties();
    }
  
    public static Properties getProperties(String fileName) throws BridgeDBException{
        configureLogger();
        ConfigReader configReader = new ConfigReader(fileName);            
        Properties original = configReader.readProperties();
        return addLocalProperties(original);
    }
    
    private static Properties addLocalProperties(Properties original) throws BridgeDBException{
        //Logger already configured
        ConfigReader localReader = new ConfigReader(LOCAL_FILE_NAME); 
        localReader.properties = new Properties();           
        try {
            localReader.properties.load(localReader.getInputStream());
            localReader.inputStream.close();
        } catch (IOException ex) {
            throw new BridgeDBException("Unexpected file not fond exception after file.exists returns true.", ex);
        }
        original.putAll(localReader.properties);
        return original;
    }

    public static InputStream getInputStream(String fileName) throws BridgeDBException{
        configureLogger();
        ConfigReader finder = new ConfigReader(fileName);
        return finder.getInputStream();
    }
        
    public static synchronized void configureLogger() throws BridgeDBException{
        if (!loggerSetup){
            ConfigReader finder;
            finder = new ConfigReader(LOG_PROPERTIES_FILE);
            Properties props = finder.readProperties();
            PropertyConfigurator.configure(props);
            logger.info("Logger configured from " + finder.foundAt + " by " + finder.findMethod);
            loggerSetup = true;
         }
    }
     
    public static void logToConsole() throws BridgeDBException{
        configureLogger();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
    }

    protected ConfigReader(){
    }
    
    private ConfigReader(String fileName) throws BridgeDBException{
        try {
            if (loadDirectly(fileName)) return;
            if (loadByEnviromentVariable(fileName)) return;
            if (loadByCatalinaHomeConfigs(fileName)) return;
            if (loadFromDirectory(fileName, "../conf/OPS-IMS")) return;
            if (getInputStreamWithClassLoader(fileName)) return;
            //if (loadFromDirectory(fileName, "../org.bridgedb.utils/resources")) return;
            //if (loadFromDirectory(fileName, "conf/OPS-IMS")) return;
            //if (loadFromDirectory(fileName, "../../BridgeDb/org.bridgedb.utils/resources")) return;
            //if (getInputStreamFromResource(fileName)) return;
            //if (getInputStreamFromJar(fileName)) return;
            throw new BridgeDBException("Unable to find " + fileName);
        } catch (IOException ex) {
            error = "Unexpected IOEXception after doing checks.";
            throw new BridgeDBException(error, ex);
        }
    }
    
    private InputStream getInputStream() throws BridgeDBException{
        if (error != null){
            throw new BridgeDBException(error);
        }
        if (inputStream == null){
            error = "InputStream already closed. Illegal attempt to use again.";         
            throw new BridgeDBException(error);
        }
        return inputStream;
    }
    
    private Properties readProperties() throws BridgeDBException{
        if (properties == null){
            properties = new Properties();           
            try {
                properties.load(getInputStream());
                properties.put(CONFIG_FILE_PATH_PROPERTY, foundAt);
                properties.put(CONFIG_FILE_PATH_SOURCE_PROPERTY, findMethod);
                inputStream.close();
                inputStream = null;
            } catch (IOException ex) {
                error = "Unexpected file not fond exception after file.exists returns true.";
                throw new BridgeDBException("Unexpected file not fond exception after file.exists returns true.", ex);
            }
        }
        return properties;
    }
    
    private boolean loadDirectly(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        }
        inputStream = new FileInputStream(file);
        findMethod = "Loaded from run Directory.";
        foundAt = file.getAbsolutePath();
        if (loggerSetup){
            logger.info("Loaded file " + fileName + " directly from " + foundAt);    
        }
        return true;
    }

    /**
     * Looks for the config file in the directory set up the environment variable "OPS_IMS_CONFIG"
     * @return True if the config files was found. False if the environment variable "OPS_IMS_CONFIG" was unset.
     * @throws IOException Thrown if the environment variable is not null, 
     *    and the config file is not found as indicated, or could not be read.
     */
    private boolean loadByEnviromentVariable(String fileName) throws BridgeDBException, FileNotFoundException{
        String envPath = System.getenv().get("OPS_IMS_CONFIG");
        if (envPath == null || envPath.isEmpty()) {
            logger.warn("No environment variable OPS_IMS_CONFIG found");
            return false;
        }
        File envDir = new File(envPath);
        if (!envDir.exists()){
            error = "Environment Variable OPS_IMS_CONFIG points to " + envPath + 
                    " but no directory found there";
            throw new BridgeDBException (error);
        }
        if (envDir.isDirectory()){
            File file = new File(envDir, fileName);
            if (!file.exists()){
                return false;
            }
            inputStream = new FileInputStream(file);
            findMethod = "Loaded from Environment Variable.";
            foundAt = file.getAbsolutePath();
            if (loggerSetup){
                logger.info("Loaded file " + fileName + " using OPS_IMS_CONFIG from " + foundAt);    
            }
            return true;
        } else {
            error = "Environment Variable OPS_IMS_CONFIG points to " + envPath + 
                    " but is not a directory";
            throw new BridgeDBException (error);
        }
    }
  
    /**
     * Looks for the config file in the directory set up the environment variable "OPS_IMS_CONFIG"
     * @return True if the config files was found. False if the environment variable "OPS_IMS_CONFIG" was unset.
     * @throws IOException Thrown if the environment variable is not null, 
     *    and the config file is not found as indicated, or could not be read.
     */
    private boolean loadByCatalinaHomeConfigs(String fileName) throws BridgeDBException, FileNotFoundException {
        String catalinaHomePath = System.getenv().get("CATALINA_HOME");
        if (catalinaHomePath == null || catalinaHomePath.isEmpty()) {
            logger.warn("No enviroment variable CATALINA_HOME found");
            return false;
        }
        File catalineHomeDir = new File(catalinaHomePath);
        if (!catalineHomeDir.exists()){
            error = "Environment Variable CATALINA_HOME points to " + catalinaHomePath + 
                    " but no directory found there";
            throw new BridgeDBException(error);
        }
        if (!catalineHomeDir.isDirectory()){
            error = "Environment Variable CATALINA_HOME points to " + catalinaHomePath + 
                    " but is not a directory";
            throw new BridgeDBException(error);
        }
        File envDir = new File (catalineHomeDir + "/conf/OPS-IMS");
        if (!envDir.exists()) return false; //No hard requirements that catalineHome has a /conf/OPS-IMS
        if (envDir.isDirectory()){
            File file = new File(envDir, fileName);
            if (!file.exists()){
                return false;
            }
            inputStream = new FileInputStream(file);
            findMethod = "Loaded from CATALINA_HOME configs.";
            foundAt = file.getAbsolutePath();
            if (loggerSetup){
                logger.info("Loaded file " + fileName + " using CATALINA_HOME from " + foundAt);    
            }
            return true;
        } else {
            error = "Environment Variable CATALINA_HOME points to " + catalinaHomePath  + 
                    " but $CATALINA_HOME/conf/OPS-IMS is not a directory";
            throw new BridgeDBException (error);
       }
    }
    
    /**
     * Looks for the config file in the conf/OPS-IMS sub directories of the run directory.
     * <p>
     * For tomcat conf would then be a sister directory of webapps.
     * @return True if the file was found, False if it was not found.
     * @throws IOException If there is an error reading the file.
     */
    private boolean loadFromDirectory(String fileName, String directoryName) throws FileNotFoundException {
        File directory = new File (directoryName);
        if (!directory.exists()) {
            logger.warn("No file directory found at: " + directoryName);
            return false;
        }
        if (!directory.isDirectory()){
            return false;
        }
        File file = new File(directory, fileName);
        if (!file.exists()) return false;
            if (!file.exists()){
                return false;
            }
            inputStream = new FileInputStream(file);
            findMethod = "Loaded from directory: " + directoryName;
            foundAt = file.getAbsolutePath();
            if (loggerSetup){
                logger.info("Loaded file " + fileName + " from " + foundAt);    
            }
            return true;
    }

    private boolean getInputStreamWithClassLoader(String fileName) throws FileNotFoundException{
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource(fileName);
        if (url != null){
            try {
                inputStream =  url.openStream();
            } catch (IOException ex) {
                if (loggerSetup){
                    logger.info("Error opeing url " + url , ex);
                    return false;
                }
            }
            findMethod = "Loaded with class loader";
            foundAt = url.getPath();
            if (loggerSetup){
                logger.info("Loaded " + url + " with class loader. ");    
            }
            return true;
        }
        if (loggerSetup){
            logger.info("Not found by class loader. ");    
        }
        return false;
    }

    private boolean getInputStreamFromResource(String name) throws FileNotFoundException{
        java.net.URL url = this.getClass().getResource(name);
        if (url != null){
            String fileName = url.getFile();
            File file = new File(fileName);
            if (!file.exists()){
                return false;
            }
            inputStream = new FileInputStream(file);
            findMethod = "Loaded from Resource URI";
            foundAt = file.getAbsolutePath();
            if (loggerSetup){
                logger.info("Loaded file " + fileName + " from Jar ");    
            }
            return true;
        }
        return false;
    }

    private boolean getInputStreamFromJar(String name) throws IOException{
        CodeSource src = this.getClass().getProtectionDomain().getCodeSource();
        URL jar = src.getLocation();
        ZipInputStream zip = new ZipInputStream( jar.openStream());
        ZipEntry ze = null;
        while( ( ze = zip.getNextEntry() ) != null ) {
            if (name.equals(ze.getName())){
                inputStream = zip;
                findMethod = "Loaded by unzipping jar";
                foundAt = "Inside jar file";
                if (loggerSetup){
                    logger.info("Loaded file " + name + " by unziiping the Jar ");    
                }
                return true;
            }
        }
        return false;
    }
        
    public static void useTest() {
        useTest = true;
    } 

    public static boolean inTestMode() {
        return useTest;
    }


}
