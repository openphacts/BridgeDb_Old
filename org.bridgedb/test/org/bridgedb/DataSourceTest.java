// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright      2012  Egon Willighagen <egonw@users.sf.net>
// Copyright      2012  OpenPhacts 
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

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the DataSource class
 *
 * @author Christian
 */
public class DataSourceTest {

	@Test
	public void testAsDataSource() {
		DataSource source = DataSource.register("X", "Affymetrix")
		    .asDataSource();
		Assert.assertNotNull(source);
	}

	@Test
	public void testBuilding() {
		DataSource source = DataSource.register("X", "Affymetrix").asDataSource();
		Assert.assertEquals("X", source.getSystemCode());
		Assert.assertEquals("Affymetrix", source.getFullName());
	}

    @Test
	public void testBuildingMainUrl() {
		DataSource source = DataSource.register("X", "Affymetrix")
		    .mainUrl("http://www.affymetrix.com")
		    .asDataSource();
		Assert.assertEquals("http://www.affymetrix.com", source.getMainUrl());
	}

    @Test (expected =  IllegalArgumentException.class)
	public void testChangeMainUrl() {
		DataSource source = DataSource.register("X", "Affymetrix")
		    .mainUrl("http://www.affymetrix.com")
		    .asDataSource();
		source = DataSource.register("X", "Affymetrix")
		    .mainUrl("http://www.affymetrix.com/A")
		    .asDataSource();
	}
    
	public void testBuildingType() {
		DataSource source = DataSource.register("X", "Affymetrix")
		    .type("metabolite")
		    .asDataSource();
		Assert.assertEquals("metabolite", source.getType());
		Assert.assertTrue(source.isMetabolite());
	}

    //    @Test (expected =  IllegalArgumentException.class)
	public void testChangeType() {
		DataSource source = DataSource.register("X", "Affymetrix")
		    .type("metabolite")
		    .asDataSource();
		Assert.assertEquals("metabolite", source.getType());
		Assert.assertTrue(source.isMetabolite());
		source = DataSource.register("X", "Affymetrix")
		    .type("probe")
		    .asDataSource();
		Assert.assertEquals("probe", source.getType());
		Assert.assertFalse(source.isMetabolite());
	}

 //  
 //TODO check if changing primary is a needed functionality   
	public void testBuildingPrimary() {
		DataSource source = DataSource.register("X", "Affymetrix")
		    .primary(false)
		    .asDataSource();
		Assert.assertFalse(source.isPrimary());
		source = DataSource.register("X", "Affymetrix")
			.primary(true)
			.asDataSource();
        Assert.assertTrue(source.isPrimary());
	}

	@Test
	public void testBuildingMetabolite() {
		DataSource source = DataSource.register("F", "MetaboLoci")
		    .type("metabolite")
		    .asDataSource();
		Assert.assertEquals("metabolite", source.getType());
		Assert.assertTrue(source.isMetabolite());
	}
    
    @Test
	public void testDeprecated() {
		DataSource source = DataSource.register("EnAg", "Ensembl Mosquito")
		    .deprecated(true).asDataSource();
		Assert.assertTrue(source.isDeprecated());
	}

    /**
	 * By default, all new data sources are not deprecated.
	 */
	@Test
	public void testDefaultNotDeprecated() {
		DataSource source = DataSource.register("F", "MetaboLoci")
		    .asDataSource();
		Assert.assertFalse(source.isDeprecated());
	}

    @Test
	public void testDeprecatedBy() {
		DataSource source = DataSource.register("EnAg", "Ensembl Mosquito")
		    .deprecatedBy(
		    	DataSource.register("En", "Ensembl").asDataSource()
		    ).asDataSource();
		Assert.assertTrue(source.isDeprecated());
		Assert.assertNotNull(source.isDeprecatedBy());
		Assert.assertEquals("En", source.isDeprecatedBy().getSystemCode());
	}

	@Test
	public void testDefaultNoDeprecatedBy() {
		DataSource source = DataSource.register("Cps", "PubChem-substance")
			.asDataSource();
		Assert.assertNull(source.isDeprecatedBy());
	}

	@Test
	public void testBuildingAlternative() {
		DataSource source = DataSource.register("F", "MetaboLoci")
		    .alternative("MetaboLoci Alternative")
		    .asDataSource();
		Assert.assertEquals("MetaboLoci Alternative", source.getAlternative());
	}
    
	@Test
	public void testBuildingDescription() {
		DataSource source = DataSource.register("F", "MetaboLoci")
		    .description("MetaboLoci description")
		    .asDataSource();
		Assert.assertEquals("MetaboLoci description", source.getDescription());
	}

}
