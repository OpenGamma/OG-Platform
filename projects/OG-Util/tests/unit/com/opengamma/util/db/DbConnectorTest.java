/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class DbConnectorTest {

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_nulls() {
    new DbConnector(null, null, null, null, null, null);
  }

  //-------------------------------------------------------------------------
  public void test_basics() {
    BasicDataSource ds = new BasicDataSource();
    HSQLDbDialect dialect = HSQLDbDialect.INSTANCE;
    SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(ds);
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    TransactionTemplate transTemplate = new TransactionTemplate(transMgr, transDefn);
    DbConnector test = new DbConnector("Test", dialect, ds, jdbcTemplate, null, transTemplate);
    
    assertSame(ds, test.getDataSource());
    assertSame(dialect, test.getDialect());
    assertSame(jdbcTemplate, test.getJdbcTemplate());
    assertEquals(null, test.getHibernateSessionFactory());
    assertEquals(null, test.getHibernateTemplate());
    assertSame(transMgr, test.getTransactionManager());
    assertSame(transTemplate, test.getTransactionTemplate());
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    BasicDataSource ds = new BasicDataSource();
    HSQLDbDialect dialect = HSQLDbDialect.INSTANCE;
    SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(ds);
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    TransactionTemplate transTemplate = new TransactionTemplate(transMgr, transDefn);
    DbConnector test = new DbConnector("Test", dialect, ds, jdbcTemplate, null, transTemplate);
    
    assertEquals("DbConnector[Test]", test.toString());
  }

}
