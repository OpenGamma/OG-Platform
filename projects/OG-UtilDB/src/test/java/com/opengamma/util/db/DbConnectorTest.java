/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DbConnectorTest {

  //-------------------------------------------------------------------------
  @SuppressWarnings("resource")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_nulls() {
    new DbConnector(null, null, null, null, null, null);
  }

  //-------------------------------------------------------------------------
  public void test_basics() {
    BoneCPDataSource ds = new BoneCPDataSource();
    HSQLDbDialect dialect = HSQLDbDialect.INSTANCE;
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(ds);
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    TransactionTemplate transTemplate = new TransactionTemplate(transMgr, transDefn);
    DbConnector test = new DbConnector("Test", dialect, ds, jdbcTemplate, null, transTemplate);
    
    assertSame(ds, test.getDataSource());
    assertSame(dialect, test.getDialect());
    assertSame(jdbcTemplate, test.getJdbcTemplate());
    assertSame(jdbcTemplate.getJdbcOperations(), test.getJdbcOperations());
    assertEquals(null, test.getHibernateSessionFactory());
    assertEquals(null, test.getHibernateTemplate());
    assertSame(transMgr, test.getTransactionManager());
    assertSame(transTemplate, test.getTransactionTemplate());
    test.close();
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    BoneCPDataSource ds = new BoneCPDataSource();
    HSQLDbDialect dialect = HSQLDbDialect.INSTANCE;
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(ds);
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    DataSourceTransactionManager transMgr = new DataSourceTransactionManager();
    TransactionTemplate transTemplate = new TransactionTemplate(transMgr, transDefn);
    DbConnector test = new DbConnector("Test", dialect, ds, jdbcTemplate, null, transTemplate);
    
    assertEquals("DbConnector[Test]", test.toString());
    test.close();
  }

}
