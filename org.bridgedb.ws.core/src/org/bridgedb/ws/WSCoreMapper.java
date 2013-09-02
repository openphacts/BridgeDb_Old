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
package org.bridgedb.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperCapabilities;
import org.bridgedb.Xref;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.bean.CapabilitiesBean;
import org.bridgedb.ws.bean.DataSourcesBean;
import org.bridgedb.ws.bean.PropertiesBean;
import org.bridgedb.ws.bean.PropertyBean;
import org.bridgedb.ws.bean.XrefMapsBean;
import org.bridgedb.ws.bean.XrefsBean;

/**
 *
 * @author Christian
 */
public class WSCoreMapper implements IDMapper, IDMapperCapabilities {

    WSCoreInterface webService;
    
    public WSCoreMapper(WSCoreInterface webService){
        this.webService = webService;
    }
    
    //**** IDMApper functions *****
    @Override
    public Map<Xref, Set<Xref>> mapID(Collection<Xref> srcXrefs, DataSource... tgtDataSources) throws BridgeDBException {
        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<String> codes = new ArrayList<String>();
        ArrayList<String> targetCodes = new ArrayList<String>();
        for (Xref srcXref:srcXrefs){
            if (srcXref.getId() != null && srcXref.getDataSource() != null){
                ids.add(srcXref.getId());
                codes.add(srcXref.getDataSource().getSystemCode());
            }
        }
        for (int i = 0 ; i < tgtDataSources.length; i++){
            targetCodes.add(tgtDataSources[i].getSystemCode());
        }
        if (codes.isEmpty()) return new HashMap<Xref, Set<Xref>>(); //No valid srcrefs so return empty set
        XrefMapsBean  beans = webService.mapID(ids, codes, targetCodes);
        return beans.asMappings();
    }

    @Override
    public Set<Xref> mapID(Xref ref, DataSource... tgtDataSources) throws BridgeDBException {
        if (ref.getId() == null || ref.getDataSource() == null) return new HashSet<Xref>();
        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<String> codes = new ArrayList<String>();
        ids.add(ref.getId());
        codes.add(ref.getDataSource().getSystemCode());
        ArrayList<String> targetCodes = new ArrayList<String>();
        for (int i = 0 ; i < tgtDataSources.length; i++){
            targetCodes.add(tgtDataSources[i].getSystemCode());
        }
        XrefMapsBean  beans = webService.mapID(ids, codes, targetCodes);
        return beans.getTargetXrefs();
    }

    @Override
    public boolean xrefExists(Xref xref) throws BridgeDBException {
        if (xref.getId() == null) return false;
        if (xref.getDataSource() == null) return false;
        String id = xref.getId();
        String code = xref.getDataSource().getSystemCode();
        return webService.xrefExists(id,code).exists();
    }

    @Override
    public Set<Xref> freeSearch(String text, int limit) throws BridgeDBException {
        XrefsBean beans = webService.freeSearch(text, "" + limit);
        return beans.asXrefs();
    }

    @Override
    public IDMapperCapabilities getCapabilities() {
        CapabilitiesBean bean = webService.getCapabilities();
        return bean.asIDMapperCapabilities();
    }

    private boolean isConnected = true;
    // In the case of DataCollection, there is no need to discard associated resources.
    
    @Override
    /** {@inheritDoc} */
    public void close() throws BridgeDBException { 
        isConnected = false; 
    }
 
    @Override
    /** {@inheritDoc} */
    public boolean isConnected() { 
        if (isConnected) {
            try{
                webService.isFreeSearchSupported();
                return true; 
            } catch (Exception ex) {
                return false;
            }
        } 
        return false;
    }

    @Override
    public boolean isFreeSearchSupported() {
        return webService.isFreeSearchSupported().isFreeSearchSupported();
    }

    @Override
    public Set<DataSource> getSupportedSrcDataSources() throws BridgeDBException {
        DataSourcesBean beans = webService.getSupportedSrcDataSources();
        return beans.getDataSources();
    }

    @Override
    public Set<DataSource> getSupportedTgtDataSources() throws BridgeDBException {
        DataSourcesBean beans = webService.getSupportedTgtDataSources();
        return beans.getDataSources();
    }

    @Override
    public boolean isMappingSupported(DataSource src, DataSource tgt) throws BridgeDBException {
        return webService.isMappingSupported(src.getSystemCode(), tgt.getSystemCode()).isMappingSupported();
    }

    @Override
    public String getProperty(String key) {
        PropertyBean bean = webService.getProperty(key);
        if (bean == null) return null;
        return bean.getValue();
    }

    @Override
    public Set<String> getKeys() {
        PropertiesBean beans = webService.getKeys();
        return beans.getKeys();
    }

}
