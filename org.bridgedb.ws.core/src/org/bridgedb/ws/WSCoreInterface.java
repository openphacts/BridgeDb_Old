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

import java.util.List;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.bean.CapabilitiesBean;
import org.bridgedb.ws.bean.DataSourceBean;
import org.bridgedb.ws.bean.DataSourcesBean;
import org.bridgedb.ws.bean.FreeSearchSupportedBean;
import org.bridgedb.ws.bean.MappingSupportedBean;
import org.bridgedb.ws.bean.PropertyBean;
import org.bridgedb.ws.bean.XrefsBean;
import org.bridgedb.ws.bean.XrefExistsBean;
import org.bridgedb.ws.bean.XrefMapsBean;

/**
 *
 * @author Christian
 */
public interface WSCoreInterface {

    XrefMapsBean mapID(List<String> id, List<String> scrCode, List<String> targetCodes) throws BridgeDBException;

    XrefExistsBean xrefExists(String id, String scrCode) throws BridgeDBException;

    XrefsBean freeSearch(String text, String limit) throws BridgeDBException;

    CapabilitiesBean getCapabilities();

    FreeSearchSupportedBean isFreeSearchSupported();

    DataSourcesBean getSupportedSrcDataSources() throws BridgeDBException;

    DataSourcesBean getSupportedTgtDataSources() throws BridgeDBException;

    MappingSupportedBean isMappingSupported( String sourceCode, String targetCode) throws BridgeDBException;

    PropertyBean getProperty(String key);

    List<PropertyBean> getKeys();
   
    /*DataSourceBean getDataSoucre(String code) throws BridgeDBException;



    CapabilitiesBean getCapabilities();

    public List<URLMappingBean> mapByURLs(List<String> sourceURL, List<String> linkSetId, List<String> targetNameSpace) 
            throws BridgeDBException;

    public URLExistsBean urlExists(String URL) throws BridgeDBException;

    public URLSearchBean URLSearch(String text, Integer limit) throws BridgeDBException;
*/

}
