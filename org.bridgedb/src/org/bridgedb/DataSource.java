// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
// Copyright 2006-2009 BridgeDb developers
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
package org.bridgedb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
Contains information about a certain DataSource. This includes:
<ul>
<li>It's full name ("Ensembl")
<li>It's system code ("En")
<li>It's main url ("http://www.ensembl.org")
<li>Id-specific url's ("http://www.ensembl.org/Homo_sapiens/Gene/Summary?g=" + id)
</ul>
The DataSource class uses the extensible <code>enum</code> pattern.
You cannot instantiate DataSources directly, instead you have to use one of
the constants from the org.bridgedb.bio module such as BioDataSource.ENSEMBL, 
or the <code>getBySystemcode</code> or "getByFullname" methods.
These methods return a predefined DataSource object if it exists.
If a predefined DataSource for a requested SystemCode doesn't exists,
a new one springs to life automatically. This can be used 
when the user requests new, unknown data sources. If you call
<code>getBySystemCode</code> twice with the same argument, it is guaranteed
that you get the same return object. However, there is no way
to combine a new DataSource with a new FullName unless you use 
the "register" method.
<p>
This way any number of pre-defined DataSources can be used, 
but plugins can define new ones and you can
handle unknown data sources in the same 
way as predefined ones.
<p>
Definitions for common DataSources can be found in {@link org.bridgedb.bio.BioDataSource}.
*/
public final class DataSource
{
	private static Map<String, DataSource> bySysCode = new HashMap<String, DataSource>();
	private static Map<String, DataSource> byFullName = new HashMap<String, DataSource>();
	private static Set<DataSource> registry = new HashSet<DataSource>();
	private static Map<String, DataSource> byAlias = new HashMap<String, DataSource>();
	private static Map<String, DataSource> byMiriamBase = new HashMap<String, DataSource>();

	private final String sysCode;
	private final String fullName;
	private String mainUrl = null;
	private String prefix = null;
	private String postfix = null;
	private Object organism = null;
	private String idExample = null;
	private boolean isPrimary = true;
	private boolean isDeprecated = false;
	private DataSource isDeprecatedBy = null;
	private String type = UNKOWN;
	private String miriamBase = null;
    
    public static final String UNKOWN = "unknown";
    private static final String URN_PREFIX = "urn:miriam:";
    private static final String IDENTIFIERS_ORG_PREFIX = "http://identifiers.org/";
	
	/**
	 * Constructor is private, so that we don't
	 * get any standalone DataSources. 
	 * DataSources should be obtained from 
	 * {@link getByFullName} or {@link getBySystemCode}. Information about
	 * DataSources can be added with {@link register}
	 */
    private DataSource (String sysCode, String fullName) {
        this.sysCode = sysCode;
        this.fullName = fullName;
		if (isSuitableKey(sysCode)) {
            bySysCode.put(sysCode, this);
        }
		if (isSuitableKey(fullName)) {
            byFullName.put(fullName, this);
        }
    }
	
	/** 
	 * Turn id into url pointing to info page on the web, e.g. "http://www.ensembl.org/get?id=ENSG..."
     * 
     * Since version 2 this will return null if not pattern has been set.
	 * @param id identifier to use in url
	 * @return Url
	 */
	public String getUrl(String id)
	{
        if (prefix == null){
            return null;
        }
		return prefix + id + postfix;
	}
				
	/** 
	 * returns full name of DataSource e.g. "Ensembl". 
	 * May return null if only the system code is known. 
	 * Also used as identifier in GPML
	 * @return full name of DataSource 
	 */
	public String getFullName()
	{
		return fullName;
	}
	
	/** 
	 * returns GenMAPP SystemCode, e.g. "En". May return null,
	 * if only the full name is known.
	 * Also used as identifier in
	 * <ol> 
	 * <li>Gdb databases, 
	 * <li>Gex databases.
	 * <li>Imported data
	 * <li>the Mapp format.
	 * </ol> 
	 * We should try not to use the system code anywhere outside
	 * these 4 uses.
	 * @return systemcode, a short unique code.
	 */
	public String getSystemCode()
	{
		return sysCode;
	}
	
	/**
	 * Return the main Url for this datasource,
	 * that can be used to refer to the datasource in general.
	 * (e.g. http://www.ensembl.org/)
	 * 
	 * May return null in case the main url is unknown.
	 * @return main url
	 */
	public String getMainUrl()
	{	
		return mainUrl;
	}

	/**
	 * @return type of entity that this DataSource describes, for example
	 *   "metabolite", "gene", "protein", "interaction" or "probe" 
	 */
	public String getType()
	{
		return type;
	}
	
	/**
	 * Creates a global identifier. 
	 * It uses the MIRIAM data type list
	 * to create a MIRIAM URI like "urn:miriam:uniprot:P12345", 
	 * or if this DataSource is not included
	 * in the MIRIAM data types list, a bridgedb URI.
	 * @param id Id to generate URN from.
	 * @return the URN. 
	 */
	public String getURN(String id)
	{
        if (miriamBase == null){
            return null;
        }
		String idPart;
		try
		{
			idPart = URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException ex) { 
            idPart = id; 
        }
		return URN_PREFIX + miriamBase + ":" + idPart;
	}

    public String getIdentifiersOrgUri(String id) {
        if (miriamBase == null){
            return null;
        }
		String idPart;
		try
		{
			idPart = URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException ex) { 
            idPart = id; 
        }
		return this.IDENTIFIERS_ORG_PREFIX + miriamBase + "/" + idPart;
    }

	/**
	 * Uses builder pattern to set optional attributes for a DataSource. For example, this allows you to use the 
	 * following code:
	 * <pre>
	 * DataSource.register("X", "Affymetrix")
	 *     .mainUrl("http://www.affymetrix.com")
	 *     .type("probe")
	 *     .primary(false);
	 * </pre>
	 */
	public static final class Builder
	{
		private final DataSource current;
		
		/**
		 * Create a Builder for a DataSource. Note that an existing DataSource is
		 * modified rather than creating a new one.
		 * This constructor should only be called by the register method.
		 * @param current the DataSource to be modified
		 */
		private Builder(DataSource current)
		{
			this.current = current;
		}
		
		/**
		 * @return the DataSource under construction
		 */
		public DataSource asDataSource()
		{
			return current;
		}
		
		/**
		 * 
		 * @param urlPattern is a template for generating valid URL's for identifiers. 
		 * 	The pattern should contain the substring "$ID", which will be replaced by the actual identifier.
		 * @return the same Builder object so you can chain setters
		 */
		public Builder urlPattern (String urlPattern)
		{
			if (urlPattern == null || "".equals (urlPattern)) {
				return this;
			}
            String oldUrlPattern = current.getUrl("$id");
            if (oldUrlPattern != null){
                if (oldUrlPattern.equals(urlPattern)){
                    return this;
                }
                throw new IllegalArgumentException("DataSource " + current + " already has a URl Pattern of " 
                        + oldUrlPattern + " so unable to set to " + urlPattern);
            }
            int pos = urlPattern.indexOf("$id");
            if (pos == -1) {
                throw new IllegalArgumentException("Url maker pattern for " + current + "' should have $id in it");
            }
            current.prefix = urlPattern.substring(0, pos);
			current.postfix = urlPattern.substring(pos + 3);
			return this;
		}
		
		/**
         * Note: Since version 2 this method no longer allows changing a mainUrl once set.
		 * @param mainUrl url of homepage
		 * @return the same Builder object so you can chain setters
         * @throws IllegalArgumentException is a none null mainUrl is changed to a different value. 
		 */
		public Builder mainUrl (String mainUrl)
		{
            if (mainUrl == null || mainUrl.isEmpty()){
                return this;
            }
            if (current.mainUrl == null){
                current.mainUrl = mainUrl;
                return this;
            }
            if (current.mainUrl.equals(mainUrl)){
                return this;
            }
            throw new IllegalArgumentException("Illegal attempt to change mainUrl for " + current + " from " 
                    + current.mainUrl + " to " + mainUrl);
		}


		/**
         * Note: Unlike most builder methods this one does allow the idExample to be change even if already set.
         *     There may be good reasons why a specific example works better in a specific case then the original one set.
         *     However there is no known case that replies on flexibility to change..
		 * @param idExample an example id from this system
		 * @return the same Builder object so you can chain setters
		 */
		public Builder idExample (String idExample)
		{
			current.idExample = idExample;
			return this;
		}
		
		/**
         * Note: Unlike most builder methods this one does allow the idPrimary to be change even if already set.
         *     There may be good reasons why a specific DataSource's primary status is different in different applications.
         *     However there is no known case that replies on flexibility to change.
		 * @param isPrimary secondary id's such as EC numbers, Gene Ontology or vendor-specific systems occur in data or linkouts,
		 * 	but their use in pathways is discouraged
		 * @return the same Builder object so you can chain setters
		 */
		public Builder primary (boolean isPrimary)
		{
            //To change this method to check if previously set the type would have to be changed to Boolean and default to null
			current.isPrimary = isPrimary;
			return this;
		}
		
		/**
         * Since version 2 Once deprecated a DataSource can not be undeprecated .
		 * @param isDeprecated a boolean indicating this DataSource should no longer be used
		 * @return the same Builder object so you can chain setters
		 */
		public Builder deprecated(boolean isDeprecated)
		{
            if (!isDeprecated && current.isDeprecated){
                throw new IllegalArgumentException("Ileegal attempt to set depricated to false on DataSource " + current);
            }
			current.isDeprecated = isDeprecated;
			return this;
		}
		
		/**
		 * Sets the DataSource which should be used instead of this deprecated one. It 
		 * automatically sets <code>isDeprecated</code> to true.
		 * 
         * Since Version 2 this method no longer accepts a null DataSource
		 * @param sourceToUseInstead the {@link DataSource} that should be used instead of this
		 *                           deprecated one
		 * @return the same Builder object so you can chain setters
		 */
		public Builder deprecatedBy(DataSource sourceToUseInstead)
		{
            if (sourceToUseInstead == null){
                throw new IllegalArgumentException("Illegal call with null DataSource. "
                        + "Use deprecated(true) to set deprecate without setting the deprecatedBy DataSource.");
            }
			current.isDeprecated = true;
			current.isDeprecatedBy = sourceToUseInstead;
			return this;
		}
		
		/**
         * Since version 2 This method no longer allow changing the type of a DataSource once set.
		 * @param type the type of datasource, for example "protein", "gene", "metabolite" 
		 * @return the same Builder object so you can chain setters
		 */
		public Builder type (String type)
		{
            if (type == null || type.isEmpty()){
                return this;
            }
            if (current.type.equals(UNKOWN)){
                current.type = type;
                return this;
            }
            if (current.type.equals(type)){
                return this;
            }
            throw new IllegalArgumentException("Illegal attempt to change the type of DataSource " + current + " from "
                    + current.type + " to " + type);  
		}
		
		/**
         * Since version 2 This method no longer allows you to change the Organism of a DataSource once set.
         * 
		 * @param organism organism for which this system code is suitable, or null for any / not applicable
		 * @return the same Builder object so you can chain setters
		 */
		public Builder organism (Object organism)
		{
           if (organism == null ){
                return this;
            }
            if (current.organism == null){
                current.organism = organism;
                return this;
            }
            if (current.organism.equals(organism)){
                return this;
            }
            throw new IllegalArgumentException("Illegal attempt to change the organism of DataSource " + current + " from "
                    + current.organism + " to " + organism);  
		}
		
		/**
         * This adds a miriam base to the DataSource.
         * 
         * Note: Since version 2 
         *    The base must start with urn:miriam:.
         *    The urnBase and identifiersOrgBase methods share the same miriam base.
         *    It is no longer allowed to change a none null miriam Base
         * 
		 * @param base for urn generation, for example "urn:miriam:uniprot"
		 * @return the same Builder object so you can chain setters
		 */
		public Builder urnBase (String base)
		{
            if (base == null){
                return this;
            }
            base = base.trim();
            if (base.isEmpty()){
                return this;
            }            
            if (base.startsWith(URN_PREFIX)){
                return miriamBase(base.substring(URN_PREFIX.length()));
            } else {
                throw new IllegalArgumentException("UrnBase must start with " + URN_PREFIX + " so can not accept " + base);
            }
		}
	
		/**
        * Note: Since version 2 
         *    The base must start with urn:miriam:.
         *    The urnBase and identifiersOrgBase methods share the same miriam basee.
         *    It is no longer allowed to change a none null miriam Base
		 * @param base for identifiersOrg generation, for example "http://www.identifiers.org/uniprot"
		 * @return the same Builder object so you can chain setters
		 */
		public Builder identifiersOrgBase (String base)
		{
            if (base == null){
                return this;
            }
            base = base.trim();
            if (base.isEmpty()){
                return this;
            }            
            if (base.startsWith(IDENTIFIERS_ORG_PREFIX)){
                return miriamBase(base.substring(IDENTIFIERS_ORG_PREFIX.length()));
            } else {
                throw new IllegalArgumentException("identifiers.org base must start with " + IDENTIFIERS_ORG_PREFIX + " so can not accept " + base);
            }
			
		}
        
        private Builder miriamBase(String base){
            if (current.miriamBase == null){
                current.miriamBase = base;
                byMiriamBase.put(base, current);
            } else {
                if (!current.miriamBase.equals(base)){
                    throw new IllegalArgumentException("illegal attemp to change miriam base from " 
                            + current.miriamBase + " to " + base);
                }
            }           
			return this;            
        }
	}

	/** 
	 * Register a new DataSource with (optional) detailed information.
	 * This can be used by other modules to define new DataSources.
     * 
     * Note: Since version 2 this method is stricter. 
     * It will no longer allow an existing dataSource to have either its full name of sysCode changed.
     * 
	 * @param sysCode short unique code between 1-4 letters, originally used by GenMAPP
	 * @param fullName full name used in GPML.
	 * @return Builder that can be used for adding detailed information.
	 */
	public static Builder register(String sysCode, String fullName){
/*        if (!isSuitableKey(sysCode)) {
            throw new IllegalArgumentException ("Unsuitable sysCode " + sysCode);
        }
		if (!isSuitableKey(fullName)) {
            throw new IllegalArgumentException ("Unsuitable fullName " + fullName);
        }
*/        return findOrRegister(sysCode, fullName);
    }
            
    private static Builder findOrRegister(String sysCode, String fullName)
	{
 		DataSource current = null;
		if (fullName == null && sysCode == null) throw new NullPointerException();
		
		if (byFullName.containsKey(fullName))
		{
			current = byFullName.get(fullName);
            if (sysCode ==null){
                if (current.getSystemCode() != null){
                    throw new IllegalArgumentException ("System code does not match for DataSource " + fullName 
                            + " was " + current.getSystemCode() + " so it can not be changed to null.");
                }
            } else {
                if (!sysCode.equals(current.getSystemCode())){
                    throw new IllegalArgumentException ("System code does not match for DataSource " + fullName 
                            + " was " + current.getSystemCode() + " so it can not be changed to " + sysCode);
                }
                
            }
		}
		else if (bySysCode.containsKey(sysCode))
		{
            current = bySysCode.get(sysCode);
            if (fullName ==null){
                if (current.getFullName() != null){
                    throw new IllegalArgumentException ("Full name does not match for DataSource " + sysCode 
                            + " was " + current.getFullName() + " so it can not be changed to " + null);
                }
            } else {
                if (!fullName.equals(current.getFullName())){
                    throw new IllegalArgumentException ("Full name does not match for DataSource " + sysCode 
                            + " was " + current.getFullName() + " so it can not be changed to " + fullName);
                }
                
            }
		}
		else
		{
			current = new DataSource (sysCode, fullName);
			registry.add (current);
		}
		
		return new Builder(current);
	}
	
	public void registerAlias(String alias)
	{
		byAlias.put (alias, this);
	}
	
	/**
	 * Helper method to determine if a String is allowed as key for bySysCode and byFullname hashes.
	 * Null values and empty strings are not allowed.
	 * @param key key to check.
	 * @return true if the key is allowed
	 */
	private static boolean isSuitableKey(String key)
	{
		return !(key == null || "".equals(key));
	}
	
	
	/** 
	 * @param systemCode short unique code to query for
	 * @return pre-existing DataSource object by system code, 
	 * 	if it exists, or creates a new one. 
     * @deprecated 
	 */
	public static DataSource getBySystemCode(String systemCode)
	{
		if (!bySysCode.containsKey(systemCode) && isSuitableKey(systemCode))
		{
			findOrRegister (systemCode, null);
		}
		return bySysCode.get(systemCode);
	}
	
	/** 
	 * @param systemCode short unique code to query for
	 * @return pre-existing DataSource object by system code if it exists
     * @throws IllegalArgumentException if no DataSource is known with this systemCode
	 */
	public static DataSource getExistingBySystemCode(String systemCode)
	{
		if (bySysCode.containsKey(systemCode)){
    		return bySysCode.get(systemCode);
		}
        throw new IllegalArgumentException("No DataSource known for " + systemCode);
	}

    /** 
	 * returns pre-existing DataSource object by 
	 * full name, if it exists, 
	 * or creates a new one. 
	 * @param fullName full name to query for
	 * @return DataSource
     * @deprecated 
	 */
	public static DataSource getByFullName(String fullName)
	{
		if (!byFullName.containsKey(fullName) && isSuitableKey(fullName))
		{
			findOrRegister (null, fullName);
		}
		return byFullName.get(fullName);
	}
	
	/** 
	 * returns pre-existing DataSource object 
	 * @param fullName full name to query for
	 * @return DataSource
     * @throws IllegalArgumentException if no DataSource is known with this systemCode
	 */
	public static DataSource getExistingByFullName(String fullName)
	{
		if (byFullName.containsKey(fullName)){
    		return byFullName.get(fullName);
        }
        throw new IllegalArgumentException ("No DataSource known for " + fullName);
	}

    public static DataSource getByAlias(String alias)
	{
		return byAlias.get(alias);
	}

	/**
		get all registered datasoures as a set.
		@return set of all registered DataSources
	*/ 
	static public Set<DataSource> getDataSources()
	{
		return registry;
	}
	
	/**
	 * returns a filtered subset of available datasources.
	 * @param primary Filter for specified primary-ness. If null, don't filter on primary-ness.
	 * @param metabolite Filter for specified metabolite-ness. If null, don't filter on metabolite-ness.
	 * @param o Filter for specified organism. If null, don't filter on organism.
	 * @return filtered set.
	 */
	static public Set<DataSource> getFilteredSet (Boolean primary, Boolean metabolite, Object o)
	{
		final Set<DataSource> result = new HashSet<DataSource>();
		for (DataSource ds : registry)
		{
			if (
					(primary == null || ds.isPrimary() == primary) &&
					(metabolite == null || ds.isMetabolite() == metabolite) &&
					(o == null || ds.organism == null || o == ds.organism))
			{
				result.add (ds);
			}
		}
		return result;
	}
	
	/**
	 * Get a list of all non-null full names.
	 * <p>
	 * Warning: the ordering of this list is undefined.
	 * Two subsequent calls may give different results.
	 * @return List of full names
	 */
	static public List<String> getFullNames()
	{
		final List<String> result = new ArrayList<String>();
		result.addAll (byFullName.keySet());
		return result;
	}
	/**
	 * The string representation of a DataSource is equal to
	 * it's full name. (e.g. "Ensembl")
	 * @return String representation
	 */
	public String toString()
	{
		return fullName;
	}
	
	/**
	 * @return example Xref, mostly for testing purposes
	 */
	public Xref getExample ()
	{
		return new Xref (idExample, this);
	}
	
	/**
	 * @return if this is a primary DataSource or not. Primary DataSources 
	 * are preferred when annotating models.
	 * 
	 * A DataSource is primary if it is not of type probe, 
	 * so that means e.g. Affymetrix or Agilent probes are not primary. All
	 * gene, protein and metabolite identifiers are primary.
	 */
	public boolean isPrimary()
	{
		return isPrimary;
	}
	
	/**
	 * A DataSource is deprecated if it is replaced by another data source
	 * which should be used instead. Even if this DataSource is deprecated,
	 * it does not imply it says what it is deprecated by.
	 * 
	 * @return true if this DataSource is deprecated
	 * @see         
	 */
	public boolean isDeprecated()
	{
		return isDeprecated;
	}
	
	/**
	 * Returns the DataSource that should be used instead if this DataSource
	 * is deprecated. This method may return null even if this DataSource is
	 * deprecated.
	 * 
	 * @return if defined, the DataSource that should be used instead of this one
	 */
	public DataSource isDeprecatedBy()
	{
		return isDeprecatedBy;
	}

	/**
	 * @return if this DataSource describes metabolites or not.
	 */
	public boolean isMetabolite()
	{
		return type.equals ("metabolite");
	}

	/**
	 * @return Organism that this DataSource describes, or null if multiple / not applicable.
	 */
	public Object getOrganism()
	{
		return organism;
	}

	/**
     * Since version 2.0 this method will return null if no DataSource is known
	 * @param base the base urn, which must start with "urn:miriam:". It it isn't, null is returned.
	 * @returns the DataSource for a given urn base, or null if the base is invalid or unknown.
	 */
	public static DataSource getByUrnBase(String base)
	{
		if (base == null || !base.startsWith (URN_PREFIX))
		{
			return null;
		}
		DataSource current = null;
		
        String key = base.substring(URN_PREFIX.length());
        return byMiriamBase.get(key);
	}

	/**
     * Since version 2.0 this method will return null if no DataSource is known
	 * @param base the base urn, which must start with "http://identifiers.org/". It it isn't, null is returned.
	 * @returns the DataSource for a given base, or null if the base is invalid or unknown.
	 */
    public static DataSource getByIdentiferOrgBase(String base) {
		if (base == null || !base.startsWith (IDENTIFIERS_ORG_PREFIX))
		{
			return null;
		}
		DataSource current = null;
		
        String key = base.substring(IDENTIFIERS_ORG_PREFIX.length());
        return byMiriamBase.get(key);
    }
	

}
