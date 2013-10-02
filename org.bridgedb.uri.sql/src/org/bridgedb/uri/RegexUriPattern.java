/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.uri;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.UriPatternType;
import org.bridgedb.rdf.pairs.RdfBasedCodeMapper;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author Christian
 */
public class RegexUriPattern {

    private final String prefix;
    private final String postfix;
    private final String sysCode;
    private final Pattern regex;
    
    private RegexUriPattern(String prefix, String postfix, String sysCode, Pattern regex) throws BridgeDBException{
        if (prefix == null || prefix.isEmpty()){
            throw new BridgeDBException ("Illegal prefixe " + prefix);
        }
        this.prefix = prefix;
        if (postfix != null){
            this.postfix = postfix;
        } else {
            this.postfix = "";
        }
        if (sysCode == null || sysCode.isEmpty()){
            throw new BridgeDBException ("Illegal sysCode " + sysCode);
        }
        this.sysCode = sysCode;
        this.regex = regex;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return the postfix
     */
    public String getPostfix() {
        return postfix;
    }

    /**
     * @return the sysCode
     */
    public String getSysCode() {
        return sysCode;
    }

    public Pattern getRegex() {
        return regex;
    }
    
    public String getUri(String id) {
        return prefix + id + postfix;
    }

    public static RegexUriPattern factory(String prefix, String postfix, String sysCode) throws BridgeDBException {
        return new RegexUriPattern(prefix, postfix, sysCode, null);
    }

    public static RegexUriPattern factory(String prefix, String postfix, String sysCode, Pattern regexPattern) throws BridgeDBException {
        return new RegexUriPattern(prefix, postfix, sysCode, regexPattern);
    }

    public static Collection<RegexUriPattern> getUriPatterns() throws BridgeDBException {
        HashSet<RegexUriPattern> results = new HashSet<RegexUriPattern>();
        for (UriPattern pattern:UriPattern.getUriPatterns()){
            for (String sysCode:pattern.getSysCodes()){
                Pattern regex = getRegex(pattern, sysCode);
                results.add(new RegexUriPattern(pattern.getPrefix(), pattern.getPostfix(), sysCode, regex));
            }
        }
        return results;
    }

    private static Pattern getRegex(UriPattern pattern, String sysCode){
        DataSource dataSource = DataSource.getExistingBySystemCode(sysCode);
        Pattern regex = DataSourcePatterns.getPatterns().get(dataSource);
        if (regex != null && pattern.getType() == UriPatternType.codeMapperPattern){
            String xrefPrefix = RdfBasedCodeMapper.getXrefPrefix(sysCode);
            String fullPattern = regex.pattern();
            if (fullPattern.startsWith(xrefPrefix)){
                String partPattern = fullPattern.substring(xrefPrefix.length());
                regex = Pattern.compile(partPattern);
            } else if (fullPattern.startsWith("^" + xrefPrefix)){
                String partPattern = "^" + fullPattern.substring(1 + xrefPrefix.length());
                regex = Pattern.compile(partPattern);
            }
        }
        return regex;
    }
    
    static RegexUriPattern byPattern(String pattern) throws BridgeDBException {
        //todo regex in pattern
        UriPattern uriPattern = UriPattern.existingByPattern(pattern);
        return byPattern(uriPattern);
    }
    
    static RegexUriPattern byPattern(UriPattern uriPattern) throws BridgeDBException {
        Set<String> possibles = uriPattern.getSysCodes();
        if (possibles.size() != 1){
            throw new BridgeDBException("Multiple DataSource known for " + uriPattern);
        }
        String sysCode = possibles.iterator().next();
        Pattern regex = getRegex(uriPattern, sysCode);
        return new RegexUriPattern (uriPattern.getPrefix(), uriPattern.getPostfix(), sysCode, regex);
    }



    
    
}
