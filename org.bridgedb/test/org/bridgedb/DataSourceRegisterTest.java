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

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the DataSource class
 *
 * @author Christian
 */
public class DataSourceRegisterTest {

    private static Set<String> NO_ALTERNATIVES = new HashSet<String>();
    
    @Test
    public void testBoth() throws IDMapperException{
        String sysCode = "DataSourceRegistryTest_Both";
        String fullName = "DataSourceRegistryTest_testBoth";
        DataSource ds = DataSource.register(sysCode, fullName).asDataSource();
        Assert.assertEquals(fullName, ds.getFullName());
        Assert.assertEquals(sysCode, ds.getSystemCode());
    }

    @Test
    public void testFullNameOnly() throws IDMapperException{
        String fullName = "DataSourceRegistryTest_testFullNameOnly";
        DataSource ds = DataSource.register(null, fullName).asDataSource();
        Assert.assertEquals(fullName, ds.getFullName());
        Assert.assertEquals(null, ds.getSystemCode());
    }
            
    @Test
    public void testSysCodeOnly() throws IDMapperException{
        String sysCode = "DataSourceRegistryTest_SysCodeOnly";
        DataSource ds = DataSource.register(sysCode, null).asDataSource();
        Assert.assertEquals(null, ds.getFullName());
        Assert.assertEquals(sysCode, ds.getSystemCode());
    }
    
    @Test (expected =  IllegalArgumentException.class)
    public void testSecondFullName() throws IDMapperException{
        System.out.println("SecondFullName");
        String sysCode = "DataSourceRegistryTest_SecondFullName";
        String fullName1 = "DataSourceRegistryTest_testSecondFullName1";
        String fullName2 = "DataSourceRegistryTest_testSecondFullName2";
        DataSource ds1 = DataSource.register(sysCode, fullName1).asDataSource();
        DataSource ds2 = DataSource.register(sysCode, fullName2).asDataSource();
    }

    @Test (expected =  IllegalArgumentException.class)
    public void testSecondNullFullName() throws IDMapperException{
        System.out.println("SecondNullFullName");
        String sysCode = "DataSourceRegistryTest_SecondNullFullName";
        String fullName1 = "DataSourceRegistryTest_SecondNullFullName";
        DataSource ds1 = DataSource.register(sysCode, fullName1).asDataSource();
        DataSource ds2 = DataSource.register(sysCode, null).asDataSource();
    }
    
    @Test (expected =  IllegalArgumentException.class)
    public void testNullThenFullName() throws IDMapperException{
        String sysCode = "DataSourceRegistryTest_NullThenFullName";
        String fullName2 = "DataSourceRegistryTest_testNullThenFullName";
        DataSource ds1 = DataSource.register(sysCode, null).asDataSource();
        DataSource ds2 = DataSource.register(sysCode, fullName2).asDataSource();
    }
    
    @Test (expected =  IllegalArgumentException.class)
    public void testSecondSysCode() throws IDMapperException{
        String sysCode1 = "DataSourceRegistryTest_SecondSysCode1";
        String sysCode2 = "DataSourceRegistryTest_SecondSysCode2";
        String fullName = "DataSourceRegistryTest_testFactorySecondSysCode";
        DataSource ds1 = DataSource.register(sysCode1, fullName).asDataSource();
        DataSource ds2 = DataSource.register(sysCode2, fullName).asDataSource();
    }

    @Test (expected =  IllegalArgumentException.class)
    public void testSecondNullSysCode() throws IDMapperException{
        String sysCode1 = "DataSourceRegistryTest_RegisterSecondNullSysCode1";
        String fullName = "DataSourceRegistryTest_testSecondNullSysCode";
        DataSource ds1 = DataSource.register(sysCode1, fullName).asDataSource();
        DataSource ds2 = DataSource.register(null, fullName).asDataSource();
    }

    @Test (expected =  IllegalArgumentException.class)
    public void testNullThenSysCode() throws IDMapperException{
        String sysCode2 = "DataSourceRegistryTest_NullThenSysCode";
        String fullName = "DataSourceRegistryTest_NullThenSysCode";
        DataSource ds1 = DataSource.register(null, fullName).asDataSource();
        DataSource ds2 = DataSource.register(sysCode2, fullName).asDataSource();
    }

    @Test 
    public void testFullNameNull() throws IDMapperException{
        System.out.println("FullNameNull");
        String sysCode = "DataSourceRegistryTest_FullNameNull";
        DataSource ds1 = DataSource.register(sysCode, null).asDataSource();
        DataSource ds2 = DataSource.register(sysCode, null).asDataSource();
        //Is this behaviour desirable????
        Assert.assertEquals(ds1, ds2);
        //Is this behaviour desirable????
        Assert.assertEquals(null, ds1.getFullName());
        Assert.assertEquals(sysCode, ds1.getSystemCode());
        Assert.assertEquals(null, ds2.getFullName());
        Assert.assertEquals(sysCode, ds2.getSystemCode());
    }
}
