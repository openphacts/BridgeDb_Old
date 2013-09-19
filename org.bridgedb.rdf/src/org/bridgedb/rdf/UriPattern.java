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
package org.bridgedb.rdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.RdfConstants;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Christian
 */
public class UriPattern extends RdfBase implements Comparable<UriPattern>{

    private final String prefix;
    private final String postfix;
    private final String code;
    private final boolean isDataSourceData;
    
    private static HashMap<String,UriPattern> byPattern = new HashMap<String,UriPattern>();
    private static HashMap<String,Set<UriPattern>> byCode = new HashMap<String,Set<UriPattern>>();
    
    private static HashSet<URI> expectedPredicates = new HashSet<URI>(Arrays.asList(new URI[] {
        BridgeDBConstants.HAS_POSTFIX_URI,
        BridgeDBConstants.HAS_PREFIX_URI,
        RdfConstants.TYPE_URI,
        BridgeDBConstants.HAS_DATA_SOURCE,
        BridgeDBConstants.IS_URI_PATTERN_OF
    }));
              
    private UriPattern(String pattern, String sysCode, boolean isDataSourceData) throws BridgeDBException{        
        System.out.println(pattern + " -> " + sysCode + " " + isDataSourceData);
        int pos = pattern.indexOf("$id");
        if (pos == -1) {
            throw new BridgeDBException("Pattern " + pattern + " does not have $id in it and is not known.");
        }
        prefix = pattern.substring(0, pos);
        postfix = pattern.substring(pos + 3);
        byPattern.put(pattern, this);
        code = sysCode;
        Set<UriPattern> patterns = byCode.get(code);
        if (patterns == null){
            patterns = new HashSet<UriPattern>();
        }
        patterns.add(this);
        byCode.put(code, patterns);
        this.isDataSourceData = isDataSourceData;
    }
    
     public String getPrefix(){
        return prefix;
    }
    
    public String getPostfix(){
        return postfix;
    }
    
    public boolean hasPostfix(){
        return !postfix.isEmpty();
    }
    
    public static void refreshUriPatterns() throws BridgeDBException{
        BridgeDBRdfHandler.init();
    }

    public static SortedSet<UriPattern> getUriPatterns() {
        return new TreeSet(byPattern.values());
    }
               
    public static UriPattern register(String pattern, String code, boolean isDataSourceData) throws BridgeDBException{
        if (pattern == null || pattern.isEmpty()){
            throw new BridgeDBException ("Illegal empty or null uriPattern: " + pattern);
        }
        if (code == null || code.isEmpty()){
            throw new BridgeDBException ("Illegal empty or null code: " + code);
        }
        UriPattern result = byPattern.get(pattern);
        if (result == null){
            return new UriPattern(pattern, code, isDataSourceData);
        }
        if (result.getCode().equals(code)){
            return result;
        }
        throw new BridgeDBException ("Unable to register " + result + " to code " + code +
                " as it is already regsistered to " + result.getCode());
    }

    public static UriPattern byPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()){
            return null;
        }
        return byPattern.get(pattern);
    }

    public static UriPattern existingByPattern(String pattern) throws BridgeDBException {
        if (pattern == null || pattern.isEmpty()){
            throw new BridgeDBException ("Illegal empty or null uriPattern: " + pattern);
        }
        UriPattern result = byPattern.get(pattern);
        if (result == null){
            throw new BridgeDBException ("No UriPattern known for: " + pattern);            
        }
        return result;
    }

    public final URI getResourceId(){
        return new URIImpl(getUriPattern());
    }
    
    public String getUriPattern() {
        if (postfix == null){
            return prefix + "$id";
        } else {
            return prefix + "$id" + postfix;
        }
    }

    public static void addAll(RepositoryConnection repositoryConnection) 
            throws IOException, RepositoryException, BridgeDBException {
        for (UriPattern pattern:getUriPatterns()){
            pattern.add(repositoryConnection);
        }
   }
    
    public void add(RepositoryConnection repositoryConnection) throws RepositoryException{
        URI id = getResourceId();
        repositoryConnection.add(id, RdfConstants.TYPE_URI, BridgeDBConstants.URI_PATTERN_URI);
        repositoryConnection.add(id, BridgeDBConstants.HAS_PREFIX_URI,  new LiteralImpl(prefix));
        if (!postfix.isEmpty()){
            repositoryConnection.add(id, BridgeDBConstants.HAS_POSTFIX_URI,  new LiteralImpl(postfix));
        }
    }        
    
    public static UriPattern readUriPattern(RepositoryConnection repositoryConnection, Resource uriPatternId, 
            String code, String xrefPrefix, boolean isDataSourceData) throws BridgeDBException, RepositoryException{
        //TODO handle the extra statements
        //checkStatements(repositoryConnection, uriPatternId);
        UriPattern pattern;      
        String prefix = getPossibleSingletonString(repositoryConnection, uriPatternId, BridgeDBConstants.HAS_PREFIX_URI);
        if (prefix == null){
            String uriPattern = uriPatternId.stringValue();
            if (xrefPrefix != null){
                uriPattern = uriPattern.replace("$id", xrefPrefix + "$id");
            }
            pattern = register(uriPattern, code, isDataSourceData);
        } else {
            String postfix = getPossibleSingletonString(repositoryConnection, uriPatternId, BridgeDBConstants.HAS_POSTFIX_URI);
            if (xrefPrefix != null){
                prefix = prefix + xrefPrefix;
            }
            if (postfix == null){
                pattern = register(prefix + "$id", code, isDataSourceData);
            } else {
                pattern = register(prefix + "$id" + postfix, code, isDataSourceData);
            }
        }
        //Add any ither stuff here
        return pattern;
    }

    @Override
    public String toString(){
        return getUriPattern();      
    }

    @Override
    public int compareTo(UriPattern other) {
        String thisString = this.getResourceId().stringValue().toLowerCase();
        thisString = thisString.replaceFirst("https://","http://");
        String otherString = other.getResourceId().stringValue().toLowerCase();
        otherString = otherString.replaceFirst("https://","http://");
        return thisString.compareTo(otherString);
     }

    public String getUri(String id) {
        return prefix + id + postfix;
    }

    public String getIdFromUri(String uri) throws BridgeDBException {
        if (!uri.startsWith(prefix)){
            throw new BridgeDBException("Uri " + uri + " does not match UriPattern " + this);
        }
        if (!uri.endsWith(postfix)){
            throw new BridgeDBException("Uri " + uri + " does not match UriPattern " + this);
        }
        return uri.substring(prefix.length(), uri.length() - postfix.length());
    }

    public static Set<UriPattern> byCode(String code){
        return byCode.get(code);
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the isDataSourceData
     */
    public boolean isDataSourceData() {
        return isDataSourceData;
    }
 }
