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
import javax.ws.rs.core.Response;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.bean.DataSourceUriPatternBean;
import org.bridgedb.ws.bean.MappingBean;
import org.bridgedb.ws.bean.MappingSetInfoBean;
import org.bridgedb.ws.bean.MappingsBySetBean;
import org.bridgedb.ws.bean.OverallStatisticsBean;
import org.bridgedb.ws.bean.UriExistsBean;
import org.bridgedb.ws.bean.UriSearchBean;
import org.bridgedb.ws.bean.XrefBean;

/**
 *
 * @author Christian
 */
public interface WSUriInterface extends WSCoreInterface{

    public Response map(String id, String scrCode, String uri, String lensUri, List<String> targetCodes, 
            String graph, List<String> targetUriPattern) throws BridgeDBException;

    public Response mapBySet(List<String> uri, String lensUri, String graph, List<String> targetUriPattern) 
            throws BridgeDBException;

    public UriExistsBean UriExists(String Uri) throws BridgeDBException;

    public UriSearchBean UriSearch(String text, String limitString) throws BridgeDBException;

    public XrefBean toXref(String Uri) throws BridgeDBException;

    //public List<Mapping> getSampleMappings() throws BridgeDBException;

    public OverallStatisticsBean getOverallStatistics(String lensUri) throws BridgeDBException;

    public List<MappingSetInfoBean> getMappingSetInfos(String sourceSysCode, String targetSysCode, String lensUri) 
            throws BridgeDBException;

    public MappingSetInfoBean getMappingSetInfo(String mappingSetId) throws BridgeDBException;

    public DataSourceUriPatternBean getDataSource(String dataSource) throws BridgeDBException;
    
    //public ValidationBean validateString(String info, String mimeTypee, String storeType, String validationType, 
    //        String includeWarnings) throws BridgeDBException;

    public String getSqlCompatVersion() throws BridgeDBException;
     
 }
