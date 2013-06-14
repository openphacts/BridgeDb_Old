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
package org.bridgedb.uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;

/**
 *
 * @author Alasdair and Christian
 */
public class Lens {

	private final String id;
    private final String name;
    private String createdBy;
    private String createdOn;
    private String description;
    private final List<String> justifications;
    
    private final static HashMap<String,Lens> register = new HashMap<String,Lens>();
    private static int nextNumber = -0; 
    
    private static final String ID_PREFIX = "L";
    public static final String METHOD_NAME = "Lens";
    public static final String URI_PREFIX = "/" + METHOD_NAME + "/";
    private static final String PROPERTY_PREFIX = "lens";
    
    private static final String CREATED_BY = "createdBy";
    private static final String CREATED_ON = "createdOn";
    private static final String DESCRIPTION = "description";
    private static final String JUSTIFICATION = "justification";
    
    private static final String DEFAULT_LENS_NAME = "Default";
    private static final String TEST_LENS_NAME = "Test";
    private static final String ALL_LENS_NAME = "All";
    
    static final Logger logger = Logger.getLogger(Lens.class);

    /**
     * This methods should only be called by WS Clients as it Does not register the Lens 
     * 
     * Use factory method instead.
     * 
     * @param id
     * @param name
     * @param createdOn
     * @param createdBy
     * @param justifications 
     */
    public Lens(String id, String name, String createdOn, String createdBy, String description, Collection<String> justifications) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.description = description;
        this.justifications = new  ArrayList<String>(justifications);
    }

    private Lens(String name) {
        this.id = "L" + nextNumber;
        nextNumber++;
        this.name = name;
        this.justifications = new  ArrayList<String>();
        register.put(this.id, this);
        logger.info("Register " + this);
     }
 
    public static Lens factory(String name, String createdOn, String createdBy, String description, Collection<String> justifications) throws BridgeDBException {
        init();
        Lens result = new Lens("L" + nextNumber, name, createdOn, createdBy, description, justifications);
        nextNumber++;
        logger.info("Register " + result);
        register.put(result.id, result);
        return result;
    }
    
    public static Lens byId(String id) throws BridgeDBException{
        if (id.contains(URI_PREFIX)){
            id = id.substring(id.indexOf(URI_PREFIX)+URI_PREFIX.length());
        }
        Lens result = register.get(id);
        if (result == null){
            if (id.equals(TEST_LENS_NAME)){
                return  testLens();
            } else { 
                throw new BridgeDBException("No Lens known with Id " + id);
            }
        }
        return result;
    }
    
    private static Lens byName(String name){
        for (Lens lens:register.values()){
            if (lens.getName().equals(name)){
                return lens;
            }
        }
        return new Lens(name);
    }
    
    public static List<String> getJustificationsbyId(String id) throws BridgeDBException{
        Lens lens = byId(id);
        return lens.getJustifications();
    }
    
    @Override
    public String toString(){
           return  "Lens Id: " + this.getId() + 
        		   " Name: " + this.getName() +
        		   " Created By: " + this.getCreatedBy() +
        		   " Created On: " + this.getCreatedOn() +
                   " Description: " + this.getDescription() + 
        		   " Justifications: " + this.getJustifications();
    }
    
    public static int getNumberOfLenses(){
        return register.size();
    }

    /**
     * The Default lens is the one that should be used whenever lensUri is null.
     * <p>
     * The suggestion behaviour is that the default will the mappings that 
     *   are generally considered to apply in most situations, much as the Mappings in Version 1
     * This is not to say that these will only be Owl:sameAs mappings (as almost none are.)
     * <p>
     * However the default should not return mappings in catagories such as broader than, narrower than, 
     *    or where only the first half of the inch Strings match.
     * @return the DefaultUri as a String
     * @throws BridgeDBException 
     */
    public static String getDefaultLens() throws BridgeDBException{
        return ID_PREFIX + 1;
    }
    
    private static Lens testLens() {
        Lens testLens = register.get(TEST_LENS_NAME);
        if (testLens == null){
           testLens = byName(TEST_LENS_NAME); 
           testLens.addJustification(getTestJustifictaion());
           testLens.setDescription(ID_PREFIX);
        }
        return testLens;
    }
    
    public static String getTestLens() throws BridgeDBException{
        testLens();
        return TEST_LENS_NAME;
    }
    
    /**
     * The lens used to indicate that all mappings should be returned.
     * <p>
     * @return A lens that asks for all mappings to be returned.
     * @throws BridgeDBException 
     */
    public static String getAllLens() throws BridgeDBException{
        return ID_PREFIX + 0;
    }
    

	/**
	 * @return the Id
	 */
	public String getId() {
		return id;
	}
    
    public String toUri(String contextPath){
        if (contextPath != null){
            return contextPath + URI_PREFIX + getId();
        } else {
            return "#" + URI_PREFIX + getId();
        }
    }
    
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the createdOn
	 */
	public String getCreatedOn() {
		return createdOn;
	}

	/**
	 * @return the justification
	 */
	public List<String> getJustifications() {
		return justifications;
	}

    public static String getDefaultJustifictaionString() {
       return "http://www.w3.org/2000/01/rdf-schema#isDefinedBy"; 
    }
    
    public static String getTestJustifictaion() {
        return "http://www.bridgedb.org/test#testJustification";
    }

    private static void initAllLens() throws BridgeDBException {
    }

    public static void init() throws BridgeDBException {
        logger.info("init");
        if (register.isEmpty()){
            System.out.println("init called");
            //Make sure all and default is always LENS 0 and 1
            Lens all = byName(ALL_LENS_NAME);
            Lens defaultLens = byName(DEFAULT_LENS_NAME);
            Properties properties = ConfigReader.getProperties();
            Set<String> keys = properties.stringPropertyNames();
            for (String key:keys){
                System.out.println(key);
                if (key.startsWith(PROPERTY_PREFIX)){
                    String[] parts = key.split("\\.");
                    Lens lens = byName(parts[1]);
                    if (parts[2].equals(CREATED_BY)){
                        lens.setCreatedBy(properties.getProperty(key));
                    } else if (parts[2].equals(CREATED_ON)){
                        lens.setCreatedOn(properties.getProperty(key));
                    } else if (parts[2].equals(DESCRIPTION)){
                        lens.setDescription(properties.getProperty(key));
                    } else if (parts[2].equals(JUSTIFICATION)){
                        lens.addJustification(properties.getProperty(key));
                    } else {
                        logger.error("Found unexpected property " + key);
                    }
                     System.out.println(lens);
                }
            }
            all.setCreatedOn(new Date().toString());
            for (Lens lens:getLens()){
                all.addJustifications(lens.getJustifications());
            }
            if (all.description == null || all.description.isEmpty()){
                all.setDescription("Lens which includes all justfications.");
            }
            System.out.println(all);
            byId(Lens.getDefaultLens());
            if (defaultLens.getJustifications().isEmpty()){
                defaultLens.addJustifications(all.getJustifications());
            }
        }
     }

    public static void init(SQLUriMapper mapper) throws BridgeDBException {
        init();      
        Lens all = byId(Lens.getAllLens());
        Collection<String> justifications = mapper.getJustifications();
        for (String justification:justifications){
            all.addJustification(justification);
        }
        Lens defaultLens =  byId(Lens.getDefaultLens());
        if (defaultLens.getJustifications().isEmpty()){
            defaultLens.addJustifications(all.getJustifications());
        }
        if (defaultLens.description == null || defaultLens.description.isEmpty()){
            defaultLens.setDescription("Lens which includes the default justfications.");
        }
   }

    public static List<Lens> getLens() throws BridgeDBException {
        init();
        return new ArrayList<Lens> (register.values());
    }

    private void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void addJustification(String justification) {
        if (!this.justifications.contains(justification)){
            this.justifications.add(justification);
        }
    }

    private void addJustifications(Collection<String> justifications) {
        for (String justification:justifications){
            addJustification(justification);
        }
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
