/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Test DbSource.
 */
public class DbSourceTest {

  //-------------------------------------------------------------------------
  @Test(expected=IllegalArgumentException.class)
  public void test_isWildcard() {
    new DbSource(null, null, null, null, null, null);
  }

  //-------------------------------------------------------------------------
  @Test
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
  @Test
  public void test_toString() {
    BasicDataSource ds = new BasicDataSource();
    HSQLDbHelper dialect = HSQLDbHelper.INSTANCE;
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    DbSource test = new DbSource("Test", ds, dialect, null, transDefn, transMgr);
    
    assertEquals("DbSource[Test]", test.toString());
  }

}
