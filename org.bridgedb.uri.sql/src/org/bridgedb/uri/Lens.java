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
import java.util.HashSet;
import java.util.List;
import org.apache.log4j.Logger;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Alasdair and Christian
 */
public class Lens {

	private final String id;
    private final String name;
    private final String createdBy;
    private final String createdOn;
    private final List<String> justification;
    
    private final static HashMap<String,Lens> register = new HashMap<String,Lens>();
    private static int nextNumber = 1;
    
    private static final String ID_PREFIX = "L";
    private static final String URI_PREFIX = "/Lens/";
    
    public static final String DEFAULT_LENS_NAME = "Default";
    public static final String TEST_LENS_NAME = "Test";
    public static final String ALL_LENS_NAME = "All";
    
    static final Logger logger = Logger.getLogger(Lens.class);

   public Lens(String id, String name, String createdOn, String createdBy, Collection<String> justifications) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.justification = new  ArrayList<String>(justifications);
     }
 
    public static Lens factory(String name, String createdOn, String createdBy, Collection<String> justifications) throws BridgeDBException {
        init();
        return create(name, createdOn, createdBy, justifications);
    }
    
    private static Lens create(String name, String createdOn, String createdBy, Collection<String> justifications) throws BridgeDBException {
        Lens result = new Lens("L" + nextNumber, name, createdOn, createdBy, justifications);
        nextNumber++;
        logger.info("Register " + result);
        register.put(result.id, result);
        return result;
    }
    
    public static Lens byId(String id) throws BridgeDBException{
        if (id.contains(URI_PREFIX)){
            id = id.substring(id.indexOf(URI_PREFIX)+URI_PREFIX.length());
        }
        if (id.equals(getAllLens())){
            return getFullAllLens();
        }
        Lens result = register.get(id);
        if (result == null){
            throw new BridgeDBException("No Lens known with Id " + id);
        }
        return result;
    }
    
    public static List<String> getJustificationsbyId(String id) throws BridgeDBException{
        Lens lens = byId(id);
        return lens.getJustification();
    }
    
    @Override
    public String toString(){
           return  "Lens Id: " + this.getId() + 
        		   " Name: " + this.getName() +
        		   " Created By: " + this.getCreatedBy() +
        		   " Created On: " + this.getCreatedOn() +
        		   " Justifications: " + this.getJustification();
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
    
    public static String getTestLens() throws BridgeDBException{
        return ID_PREFIX + 2;
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
    
    private static Lens getFullAllLens() throws BridgeDBException {
        String createdOn = new Date().toString();
        String createdBy = "https://wiki.openphacts.org/index.php/User:Christian";
        HashSet<String> justifications = new HashSet<String>(); 
        List<Lens> allLens = getLens();
        for (Lens lens:allLens){
            justifications.addAll(lens.getJustification());
        }
        return new Lens(getAllLens(), ALL_LENS_NAME, createdOn, createdBy, justifications);
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
            return "http::" + URI_PREFIX + getId();
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
	public List<String> getJustification() {
		return justification;
	}

    public static String getDefaultJustifictaionString() throws BridgeDBException{
       return "http://www.w3.org/2000/01/rdf-schema#isDefinedBy"; 
    }
    
    public static String getTestJustifictaion() throws BridgeDBException{
        return "http://www.bridgedb.org/test#testJustification";
    }

    public static void init() throws BridgeDBException {
        logger.info("init");
        if (register.isEmpty()){
            String createdOn = new Date().toString();
            String createdBy = "https://wiki.openphacts.org/index.php/User:Christian";
            List<String> justifications = new ArrayList<String>(); 
            justifications.add(getDefaultJustifictaionString());
            justifications.add("http://semanticscience.org/resource/CHEMINF_000059");
            justifications.add("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Accession_Number");
            Lens lens = create(DEFAULT_LENS_NAME, createdOn, createdBy,  justifications);        
            if (!lens.getId().equals(Lens.getDefaultLens())){
                throw new BridgeDBException("Incorrect Default Lens URI created. Created " + lens.getId() + " but should have been "
                    + Lens.getDefaultLens());
            }
        
           justifications.clear();
           justifications.add(getTestJustifictaion());
           lens = create(TEST_LENS_NAME, createdOn, createdBy,  justifications);        
            if (!lens.getId().equals(Lens.getTestLens())){
               throw new BridgeDBException("Incorrect Test Lens URI created. Created " + lens.getId() + " but should have been "
                    + Lens.getDefaultLens());
           }
        } else{
            logger.info("init skipped");
        }
        logger.info(register.values());
    }

    public static List<Lens> getLens() throws BridgeDBException {
        init();
        return new ArrayList<Lens> (register.values());
    }

}
