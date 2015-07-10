/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.management.jmx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DatabaseMBean} class.
 */
@Test(groups = TestGroup.UNIT)
public class DatabaseMBeanTest {

  public void testDefaultConstruction() {
    final DataSource ds = Mockito.mock(DataSource.class);
    final DatabaseMBean.Local impl = new DatabaseMBean.Local("com.opengamma.util.db.management.jmx.DatabaseMBeanTest", ds);
    impl.setLocalJdbc("JDBC");
    impl.setUsername("username");
    final DatabaseMBean mbean = impl.mbean();
    assertSame(mbean.getDataSource(), ds);
    assertEquals(mbean.getDriver(), "com.opengamma.util.db.management.jmx.DatabaseMBeanTest");
    assertEquals(mbean.getLocalJdbc(), "JDBC");
    assertEquals(mbean.getUsername(), "username");
  }

}
