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
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.bridgedb.uri.Lens;

/**
 *
 * @author Alasdair
 */
@XmlRootElement(name="Lenses")
public class LensesBean {

    private Set<LensBean> lenses;
    
    //Webservice constructor
    public LensesBean(){
        lenses = new HashSet<LensBean>();
    }

    public LensesBean(List<Lens> lenses, String contextPath) {
        this();
        for (Lens lens:lenses){
            this.lenses.add(new LensBean(lens, contextPath));
        }
    }

    /**
     * @return the lenses
     */
    public Set<LensBean> getLenses() {
        return lenses;
    }

    /**
     * @param lenses the lenses to set
     */
    public void setLenses(Set<LensBean> lenses) {
        this.lenses = lenses;
    }

    public boolean isEmpty() {
        return lenses.isEmpty();
    }

  
}
