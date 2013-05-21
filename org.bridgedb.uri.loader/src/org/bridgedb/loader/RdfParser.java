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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;

public class RdfParser {
    
    private final RDFHandler handler;
    
    public RdfParser(RDFHandler handler){
        this.handler = handler;
    }
    
    static final Logger logger = Logger.getLogger(RdfParser.class);
    
     
    public void parse(File file) throws BridgeDBException{
        String uri = fileToURI(file);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (IOException ex) {
            throw new BridgeDBException("Unable to open File as a stream.",ex);
        }
        parse(inputStream, uri);
    }
    
    public void parse(String uri) throws BridgeDBException {
        InputStream stream = getInputStream(uri);
        parse(stream, uri);
    }

    public void parse(InputStream stream, String mappingSource) throws BridgeDBException {
        logger.info("Parsing: " + mappingSource);
        try {
            RDFParser parser = new TurtleParser();
            parser.setRDFHandler(handler);
            parser.setParseErrorListener(new LinksetParserErrorListener());
            parser.setVerifyData(true);
            parser.parse (stream, mappingSource);
        } catch (IOException ex) {
            throw new BridgeDBException("Error reading " + mappingSource + " " + ex.getMessage(), ex);
        } catch (OpenRDFException ex) {
            throw new BridgeDBException("Error parsing " + mappingSource + " " + ex.getMessage(), ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                throw new BridgeDBException("Error closing inputStream ", ex);
            }
        }
    }

    public InputStream getInputStream(String uri) throws BridgeDBException {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException ex) {
            throw new BridgeDBException ("Unable to convert String to Uri:" + uri, ex);
        }
        InputStream inputStream;
        try {
            return url.openStream();
        } catch (IOException ex) {
            throw new BridgeDBException ("Unable to convert String to Uri:" + uri, ex);
        }
    }

    public static String fileToURI(File file) throws BridgeDBException{
        String uri;
        try {
            return file.toURI().toURL().toExternalForm();
        } catch (MalformedURLException ex) {
            throw new BridgeDBException("Unable to convert file to URI", ex);
        }
    }

 }
