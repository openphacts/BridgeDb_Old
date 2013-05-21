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
package org.bridgedb.loader;

import java.io.File;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.bridgedb.uri.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.URI;

public class LinksetListener {
    
    private final UriListener uriListener;
    private boolean SYMETRIC = true; 
    
    public LinksetListener(UriListener uriListener){
        this.uriListener = uriListener;
    }
    
    static final Logger logger = Logger.getLogger(LinksetListener.class);
    
    public int parse(File file, String mappingSource, URI linkPredicate, String justification) throws BridgeDBException{
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        RdfParser parser = new RdfParser(handler);
        parser.parse(file);
        return handler.getMappingsetId();
    }
    
    public int parse(String uri, String mappingSource, URI linkPredicate, String justification) throws BridgeDBException{
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        RdfParser parser = new RdfParser(handler);
        parser.parse(uri);
        return handler.getMappingsetId();
    }

     public int parse(InputStream stream, String mappingSource, URI linkPredicate, String justification) throws BridgeDBException{
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        RdfParser parser = new RdfParser(handler);
        parser.parse(stream, mappingSource);
        return handler.getMappingsetId();
    }
 }
