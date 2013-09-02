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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.bridgedb.Xref;

@XmlRootElement(name="XrefMappings")
public class XrefMapsBean {

    private Set<XrefMapBean> XrefMapBean;
    
    public XrefMapsBean(){
        XrefMapBean = new HashSet<XrefMapBean>();
    }

    public XrefMapsBean(Map<Xref, Set<Xref>>  mappings){
        XrefMapBean = new HashSet<XrefMapBean>();
        for (Xref source:mappings.keySet()){
            for (Xref target:mappings.get(source)){
                XrefMapBean.add(org.bridgedb.ws.bean.XrefMapBean.asBean(source, target));
            }
        }
    }
    
    public  Map<Xref, Set<Xref>> asMappings(){
        HashMap<Xref, Set<Xref>> results = new HashMap<Xref, Set<Xref>>();
        for (XrefMapBean bean:XrefMapBean){
            Xref source = bean.getSource().asXref();
            Set<Xref>targets = results.get(source);
            if (targets == null){
                targets = new HashSet<Xref>();
            }
            Xref target = bean.getTarget().asXref();
            targets.add(target);
            results.put(source, targets);
        }
        return results;
   }
    
    /**
     * @return the XrefMapBean
     */
    public Set<XrefMapBean> getXrefMapBean() {
        return XrefMapBean;
    }

    /**
     * @param XrefMapBean the XrefMapBean to set
     */
    public void setXrefMapBean(Set<XrefMapBean> XrefMapBean) {
        this.setXrefMapBean(XrefMapBean);
    }

}
