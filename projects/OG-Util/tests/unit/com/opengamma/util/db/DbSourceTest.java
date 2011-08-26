/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.Test;

/**
 * Test DbSource.
 */
@Test
public class DbSourceTest {

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isWildcard() {
    new DbSource(null, null, null, null, null, null);
  }

  //-------------------------------------------------------------------------
  public void test_basics() {
    BasicDataSource ds = new BasicDataSource();
    HSQLDbHelper dialect = HSQLDbHelper.INSTANCE;
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    DbSource test = new DbSource("Test", ds, dialect, null, transDefn, transMgr);
    
    assertSame(ds, test.getDataSource());
    assertSame(dialect, test.getDialect());
    assertNotNull(test.getJdbcTemplate());
    assertEquals(null, test.getHibernateSessionFactory());
    assertEquals(null, test.getHibernateTemplate());
    assertSame(transDefn, test.getTransactionDefinition());
    assertSame(transMgr, test.getTransactionManager());
    assertNotNull(test.getTransactionTemplate());
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    BasicDataSource ds = new BasicDataSource();
    HSQLDbHelper dialect = HSQLDbHelper.INSTANCE;
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    DbSource test = new DbSource("Test", ds, dialect, null, transDefn, transMgr);
    
    assertEquals("DbSource[Test]", test.toString());
  }

}
