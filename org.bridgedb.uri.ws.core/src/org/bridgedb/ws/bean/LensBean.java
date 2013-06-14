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
package org.bridgedb.ws.bean;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.bridgedb.uri.Lens;

/**
 *
 * @author Alasdair
 */
@XmlRootElement(name="Lens")
public class LensBean {

	private String uri;
    private String name;
    private String createdBy;
    private String createdOn;
    private String description;
    private Set<String> justification;
    
    //Webservice constructor
    public LensBean(){
    }

    public static Lens asLens(LensBean bean){
        String id = bean.getUri().substring(bean.getUri().indexOf("/"));
        return new Lens(id, bean.getName(), bean.getCreatedOn(), bean.getCreatedBy(), 
                bean.getDescription(), bean.getJustification());
    }

    public static LensBean asBean(Lens lens, String contextPath) {
        LensBean bean = new LensBean();
    	bean.uri = lens.toUri(contextPath);
        bean.name = lens.getName();
        bean.createdBy = lens.getCreatedBy();
        bean.createdOn = lens.getCreatedOn();
        bean.description = lens.getDescription();
        bean.justification = new HashSet<String>(lens.getJustifications());
        return bean;
    }
    
    public String toString(){
           return  "Lens URI: " + this.getUri() + 
        		   " Name: " + this.getName() +
        		   " Created By: " + this.getCreatedBy() +
        		   " Created On: " + this.getCreatedOn() +
        		   " Justifications: " + this.getJustification();
    }

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the createdOn
	 */
	public String getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the justification
	 */
	public Set<String> getJustification() {
		return justification;
	}

	/**
	 * @param justification the justification to set
	 */
	public void setJustification(Set<String> justification) {
		this.justification = justification;
	}

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
}
