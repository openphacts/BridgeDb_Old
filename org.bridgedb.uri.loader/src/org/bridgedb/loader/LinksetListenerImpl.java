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
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;
import org.bridgedb.uri.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;

public class LinksetListenerImpl implements LinksetListener{
    
    private final UriListener uriListener;
    private boolean SYMETRIC = true; 
    
    public LinksetListenerImpl(UriListener uriListener){
        this.uriListener = uriListener;
    }
    
    static final Logger logger = Logger.getLogger(LinksetListenerImpl.class);
    
    @Override
    public int parse(File file, URI linkPredicate, String justification) throws BridgeDBException{
        String mappingSource;
        try {
            mappingSource = file.toURI().toURL().toExternalForm();
        } catch (MalformedURLException ex) {
            throw new BridgeDBException("Unable to convert file to URI", ex);
        }
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        return parse(handler, file, mappingSource);
    }
    
    private int parse (LinksetHandler handler, File file, String baseURI) throws BridgeDBException  {
        if (!file.isFile()){
            throw new BridgeDBException (file.getAbsolutePath() + " is not a file");
        }
        logger.info("Parsing file:\n\t" + file.getAbsolutePath());
        FileReader reader = null;
        try {
            RDFParser parser = new TurtleParser();
            parser.setRDFHandler(handler);
            parser.setParseErrorListener(new LinksetParserErrorListener());
            parser.setVerifyData(true);
            reader = new FileReader(file);
            parser.parse (reader, baseURI);
            return handler.getMappingsetId();
        } catch (IOException ex) {
            throw new BridgeDBException("Error reading file " + 
            		file.getAbsolutePath() + " " + ex.getMessage(), ex);
        } catch (OpenRDFException ex) {
            throw new BridgeDBException("Error parsing file " + 
            		file.getAbsolutePath()+ " " + ex.getMessage(), ex);
        } finally {
            try {
                if (reader != null){
                    reader.close();
                }
            } catch (IOException ex) {
                throw new BridgeDBException("Error closing Reader ", ex);
            }
        }
    }

 }
