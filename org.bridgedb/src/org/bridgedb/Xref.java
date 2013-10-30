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
import java.net.URLDecoder;

/**
 * Stores an id + {@link DataSource} combination, which represents
 * an unique gene product.
 * <p>
 * Immutable class, thread safe
 */
public class Xref implements Comparable<Xref> 
{	
	final private String id;
	final private DataSource ds;
	
	// String representation of this xref
	final private String rep;
	
	/**
	 * @param id the Id part of this Xref
	 * @param ds the DataSource part of this Xref.
	 */
	public Xref(String id, DataSource ds) {
		this.id = id;
		this.ds = ds;
		rep = (ds == null ? "" : (ds.getSystemCode() == null ? ds.getFullName() : ds.getSystemCode())) + ":" + id; 
	}
	
	/**
	 * @return the DataSource part of this Xref
	 */
	public DataSource getDataSource() { return ds; }
	
	/**
	 * @return the id part of this Xref
	 */
	public String getId() { return id; }
	
	/**
	 * @return short string representation for this Xref, for example En:ENSG000001 or X:1004_at
	 *   This string representation is not meant to be stored or parsed, it is there mostly for
	 *   debugging purposes.
	 */
	public String toString() { return rep;  }
	
	/**
	 * hashCode calculated from id and datasource combined.
	 * @return the hashCode
	 */
	public int hashCode() 
	{
		return rep.hashCode();
	}
	
	/**
	 * @return true if both the id and the datasource are equal.
	 * @param o Object to compare to
	 */
	public boolean equals(Object o) 
	{
		if (o == null) return false;
		if(!(o instanceof Xref)) return false;
		Xref ref = (Xref)o;
		return 
			(id == null ? ref.id == null : id.equals(ref.id)) && 
			(ds == null ? ref.ds == null : ds.equals(ref.ds));
	}
	
	/**
	 * Compares two Xrefs, asciibetically using string representation.
	 * @param idc Xref to compare to
	 * @return 0 if equal, positive number if higher, negative number if lower. 
	 */
	public int compareTo (Xref idc) 
	{
		return rep.compareTo(idc.rep);
	}
	
	/**
	 * Uses DataSource.getUrl() to create a valid URL for an online webpage describing this entity.
     * @deprecated As it will return just the id if no UriPattern is known
	 * @return url as a String.
	 */
	public String getUrl()
	{
		if (ds == null) return null;
		return ds.getUrl (id);
	}
	
	/**
	 * Uses DataSource.getKnownUrl to create a valid URL for an online webpage describing this entity.
     * 
     * @since Version 2.0.0
	 * @return url as a String, or null if no UriPattern is known.
	 */
	public String getKnownUrl()
	{
		if (ds == null) return null;
		return ds.getKnownUrl(id);
	}

    /**
	 * Uses DataSource.getURN() to create a global identifier, such as urn:miriam:uniprot:P12345. 
     * 
     * Depricated as this method is inconsistant as to the type of urn returned
     * @deprecated 
	 * @return the URN as string 
	 */
	public String getURN()
	{
		return ds.getURN (id);
	}
	
	/**
	 * Uses DataSource.getMiriamURN() to create a global identifier, such as urn:miriam:uniprot:P12345. 
     * 
 	 * @since Version 2.0.0
     * @return the URN as string  or null if no valid miriam urn is known
	 */
	public String getMiriamURN()
	{
		return ds.getMiriamURN (id);
	}
    
    /**
     * This method will convert a known Miraim Urn to an Xref
     * 
     * @since Version 2.0.0
     * @param urn
     * @return A Xref or null if the urn is not in Miriam Format or 
     */
	public static Xref fromMiriamUrn(String urn)
	{
		int pos = urn.lastIndexOf(":");
		if (pos < 0) return null;
		
		String base = urn.substring(0, pos);
		String id;
		try
		{
			id = URLDecoder.decode(urn.substring(pos + 1), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		
		DataSource ds = DataSource.getByMiriamBase(base);
		if (ds == null) return null;
		
		return new Xref (id, ds);
	}
    
    /**
     * Returns an xref based on this urn, even if it has to create a new DataSource to do so
     * 
     * This method could end up creating an previously none existing DataSource so is not recommended.
     * 
     * @param urn
     * @deprecated 
     * @return 
     */
    public static Xref fromUrn(String urn)
	{
		int pos = urn.lastIndexOf(":");
		if (pos < 0) return null;
		
		String base = urn.substring(0, pos);
		String id;
		try
		{
			id = URLDecoder.decode(urn.substring(pos + 1), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		
		DataSource ds = DataSource.getByUrnBase(base);
		if (ds == null) return null;
		
		return new Xref (id, ds);
	}

}